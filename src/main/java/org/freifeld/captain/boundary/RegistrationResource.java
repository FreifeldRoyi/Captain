package org.freifeld.captain.boundary;

import org.apache.curator.x.discovery.ServiceInstance;
import org.freifeld.captain.controller.ZookeeperNegotiator;
import org.freifeld.captain.controller.configuration.ConfigVariable;
import org.freifeld.captain.entity.ServiceData;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author royif
 * @since 07/09/17.
 */
@Stateless
@Path("v1/registrations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegistrationResource
{
	@Resource
	private ManagedExecutorService mes;

	@EJB
	private ZookeeperNegotiator zookeeper;

	@Inject
	@ConfigVariable("DISCOVERY_SERVICE_NAME")
	private String discoveryServiceName;

	@GET
	public void getRegistrations(@Suspended AsyncResponse response)
	{
		this.bulkhead(response, () -> this.zookeeper.getAllServices());
	}

	@GET
	@Path("{serviceName}")
	public void getChildren(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName)
	{
		this.bulkhead(response, () -> this.zookeeper.getChildrenFor(serviceName).stream().map(this::toJson).collect(Collectors.toList()));
	}

	@PUT
	@Path("{serviceName}")
	public void registerService(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName)
	{
		this.bulkhead(response, () ->
		{
			if (discoveryServiceName.equals(serviceName))
			{
				return Response.status(Response.Status.BAD_REQUEST).header("X-REASON", "Reserved Name for service discovery").build();
			}
			return Optional.ofNullable(this.zookeeper.register(serviceName, true)).map(instance -> Response.ok(this.toJson(instance)).build()).orElse(Response.noContent().build());
		});
	}

	@POST
	@Path("{serviceName}/{id}")
	public void keepAlive(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName, @PathParam("id") String id)
	{
		this.bulkhead(response, () ->
		{
			if (discoveryServiceName.equals(serviceName))
			{
				return Response.status(Response.Status.BAD_REQUEST).header("X-REASON", "Cannot update this service since it is reserved for service discovery").build();
			}
			return Optional.ofNullable(this.zookeeper.update(serviceName, id)).map(instance -> Response.ok(this.toJson(instance)).build()).orElse(Response.status(Response.Status.NOT_FOUND).build());
		});
	}

	@DELETE
	@Path("{serviceName}/{id}")
	public void unregisterServiceInstance(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName, @PathParam("id") String id)
	{
		this.bulkhead(response, () -> this.zookeeper.unregister(serviceName, id) ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build());
	}

	private JsonObject toJson(ServiceInstance<ServiceData> instance)
	{
		return Json.createObjectBuilder()
				.add("serviceName", instance.getName())
				.add("instanceId", instance.getId())
				.add("registrationTime", Instant.ofEpochMilli(instance.getRegistrationTimeUTC()).toString())
				.add("heartbeat", Instant.ofEpochMilli(instance.getPayload().getHeartbeat()).toString())
				.add("timedConnection", instance.getPayload().isTimedConnection())
				.build();
	}

	private <T> void bulkhead(AsyncResponse asyncResponse, Supplier<T> supplier)
	{
		asyncResponse.setTimeout(1, TimeUnit.SECONDS);
		asyncResponse.setTimeoutHandler(response -> response.resume(Response.status(Response.Status.REQUEST_TIMEOUT).build()));
		CompletableFuture.supplyAsync(supplier, this.mes).thenAccept(asyncResponse::resume);
	}
}

package org.freifeld.captain.boundary;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.freifeld.captain.controller.ZookeeperNegotiator;
import org.freifeld.captain.controller.configuration.ConfigVariable;
import org.freifeld.captain.entity.InstanceData;

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
	private ZookeeperNegotiator zookeeperNegotiator;

	@Inject
	@ConfigVariable("DISCOVERY_SERVICE_NAME")
	private String discoveryServiceName;

	@GET
	public void getRegistrations(@Suspended AsyncResponse response)
	{
		this.bulkhead(response, () -> {
			JsonObjectBuilder allServices = Json.createObjectBuilder();
			this.zookeeperNegotiator.getAllServices().forEach(s -> {
				JsonArrayBuilder serviceArray = Json.createArrayBuilder();
				this.zookeeperNegotiator.getChildrenFor(s).stream().map(this::toJson).forEach(serviceArray::add);
				allServices.add(s, serviceArray);
			});
			return Response.ok(allServices.build()).build();
		});
	}

	@GET
	@Path("{serviceName}")
	public void discover(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName)
	{
		this.bulkhead(response, () ->
		{
			Response toReturn;
			ServiceInstance<InstanceData> instance = this.zookeeperNegotiator.discoverInstance(serviceName);
			if (instance == null)
			{
				toReturn = Response.status(Response.Status.NOT_FOUND).build();
			}
			else
			{
				toReturn = Response.ok(this.toJson(instance)).build();
			}
			return toReturn;
		});
	}

	@POST
	@Path("{serviceName}")
	public void registerService(@Suspended AsyncResponse response, @Context UriInfo uriInfo, @PathParam("serviceName") String serviceName)
	{
		this.bulkhead(response, () ->
		{
			if (discoveryServiceName.equals(serviceName))
			{
				return Response.status(Response.Status.BAD_REQUEST).header("X-REASON", "Reserved Name for service discovery").build();
			}
			return Optional.ofNullable(this.zookeeperNegotiator.register(serviceName, true))
					.map(instance -> Response.created(URI.create(uriInfo.getRequestUri().toString() + "/" + instance.getId())).entity(this.toJson(instance)).build())
					.orElse(Response.noContent().build());
		});
	}

	@PUT
	@Path("{serviceName}/{id}")
	public void keepAlive(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName, @PathParam("id") String id)
	{
		this.bulkhead(response, () ->
		{
			if (discoveryServiceName.equals(serviceName))
			{
				return Response.status(Response.Status.BAD_REQUEST).header("X-REASON", "Cannot update this service since it is reserved for service discovery").build();
			}
			return Optional.ofNullable(this.zookeeperNegotiator.update(serviceName, id)).map(instance -> Response.ok(this.toJson(instance)).build())
					.orElse(Response.status(Response.Status.NOT_FOUND).build());
		});
	}

	@DELETE
	@Path("{serviceName}/{id}")
	public void unregisterServiceInstance(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName, @PathParam("id") String id)
	{
		this.bulkhead(response, () -> this.zookeeperNegotiator.unregisterInstance(serviceName, id) ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build());
	}

	private JsonObject toJson(ServiceInstance<InstanceData> instance)
	{
		return Json.createObjectBuilder()
				.add("serviceName", instance.getName())
				.add("instanceId", instance.getId())
				.add("registrationTime", Instant.ofEpochMilli(instance.getRegistrationTimeUTC()).toString())
				.add("heartbeat", Instant.ofEpochMilli(instance.getPayload().getHeartbeat()).toString())
				.add("timedConnection", instance.getPayload().isTimedConnection())
				.add("port", Optional.ofNullable(instance.getPort()).map(String::valueOf).orElse(JsonValue.NULL.toString()))
				.add("address", Optional.ofNullable(instance.getAddress()).map(String::valueOf).orElse(JsonValue.NULL.toString()))
				.add("sslPort", Optional.ofNullable(instance.getSslPort()).map(String::valueOf).orElse(JsonValue.NULL.toString()))
				.add("uriSpec", Optional.ofNullable(instance.getUriSpec()).map(UriSpec::build).orElse(JsonValue.NULL.toString()))
				.build();
	}

	private <T> void bulkhead(AsyncResponse asyncResponse, Supplier<T> supplier)
	{
		asyncResponse.setTimeout(1, TimeUnit.SECONDS);
		asyncResponse.setTimeoutHandler(response -> response.resume(Response.status(Response.Status.REQUEST_TIMEOUT).build()));
		CompletableFuture.supplyAsync(supplier, this.mes).thenAccept(asyncResponse::resume);
	}
}

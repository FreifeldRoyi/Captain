package org.freifeld.captain.boundary;

import org.freifeld.captain.controller.ZookeeperNegotiator;
import org.freifeld.captain.controller.exception.ZookeeperException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author royif
 * @since 07/09/17.
 */
@Path("v1/registrations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegistrationResource
{
	@Resource
	ManagedExecutorService mes;

	@EJB
	ZookeeperNegotiator zookeeperNegotiator;

	@GET
	public void getRegistrations(@Suspended AsyncResponse response)
	{
		this.bulkhead(response, () -> this.zookeeperNegotiator.getAllServices());
	}

	@GET
	@Path("{serviceName}")
	public void getChildren(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName)
	{
		this.bulkhead(response, () ->
		{
			List<JsonObject> toReturn = this.zookeeperNegotiator.getChildrenFor(serviceName).stream()
					.map(instance -> Json.createObjectBuilder()
							.add("id", instance.getId())
							.add("name", instance.getName())
							.add("registrationTime", Instant.ofEpochMilli(instance.getRegistrationTimeUTC()).toString())
							.build()).collect(Collectors.toList());
			return toReturn.isEmpty() ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(toReturn).build();
		});
	}

	@POST
	@Path("{serviceName}")
	public void registerService(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName) throws ZookeeperException
	{
		this.bulkhead(response, () -> Optional.ofNullable(this.zookeeperNegotiator.register(serviceName))
				.map(id -> Response.ok().entity(id).build())
				.orElse(Response.noContent().build()));
	}

	@DELETE
	@Path("{serviceName}/{id}")
	public void unregisterServiceInstance(@Suspended AsyncResponse response, @PathParam("serviceName") String serviceName, @PathParam("id") String id)
	{
		this.bulkhead(response, () -> this.zookeeperNegotiator.unregister(serviceName, id) ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build());
	}

	private <T> void bulkhead(AsyncResponse response, Supplier<T> supplier)
	{
		this.handleResponse(response);
		CompletableFuture.supplyAsync(supplier, this.mes).thenAccept(response::resume);
	}

	private void handleResponse(AsyncResponse asyncResponse)
	{
		asyncResponse.setTimeout(1, TimeUnit.SECONDS);
		asyncResponse.setTimeoutHandler(response -> response.resume(Response.status(Response.Status.REQUEST_TIMEOUT).build()));
	}
}

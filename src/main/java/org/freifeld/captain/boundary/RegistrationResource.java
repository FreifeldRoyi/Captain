package org.freifeld.captain.boundary;

import org.freifeld.captain.controller.ZookeeperNegotiator;
import org.freifeld.captain.controller.exception.ZookeeperException;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * @author royif
 * @since 07/09/17.
 */
@Path("registrations")
public class RegistrationResource
{

	@EJB
	ZookeeperNegotiator zookeeperNegotiator;

	@GET
	@Path("/services/{serviceName}")
	public Response getService(@PathParam("serviceName") String serviceName) throws ZookeeperException
	{
		String uri = this.zookeeperNegotiator.getUri(serviceName);
		return uri != null ? Response.ok().entity(uri).build() : Response.noContent().build();
	}

	@POST
	@Path("/services/{serviceName}")
	public Response registerService(@PathParam("serviceName") String serviceName) throws ZookeeperException
	{
		String data = this.zookeeperNegotiator.registerService(serviceName);
		return Response.ok().entity(data).build();
	}

	@DELETE
	@Path("/services/{serviceName}")
	public Response unregisterService(@PathParam("serviceName") String serviceName) throws ZookeeperException
	{
		this.zookeeperNegotiator.unregisterService(serviceName);
		return Response.ok().build();
	}

}

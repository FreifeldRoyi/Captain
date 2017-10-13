package org.freifeld.captain.controller.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * @author royif
 * @since 10/09/17.
 */
public class ZookeeperException extends WebApplicationException
{
	public ZookeeperException()
	{
		this("Something went wrong");
	}

	public ZookeeperException(String message)
	{
		super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("X-REASON", message).build());
	}
}

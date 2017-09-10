package org.freifeld.captain.controller.exception;

/**
 * @author royif
 * @since 10/09/17.
 */
public class ZookeeperException extends RuntimeException
{
	public ZookeeperException()
	{
		super();
	}

	public ZookeeperException(String message)
	{
		super(message);
	}

	public ZookeeperException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ZookeeperException(Throwable cause)
	{
		super(cause);
	}

	protected ZookeeperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

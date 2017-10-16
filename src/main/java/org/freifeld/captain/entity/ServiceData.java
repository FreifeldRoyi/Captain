package org.freifeld.captain.entity;

import java.time.Instant;

/**
 * @author royif
 * @since 16/10/17.
 */
public class ServiceData
{
	private long heartbeat;
	private boolean timedConnection;

	public ServiceData()
	{
		this(true);
	}

	public ServiceData(ServiceData other)
	{
		this(other.timedConnection);
	}

	public ServiceData(boolean timedConnection)
	{
		this.timedConnection = timedConnection;
		this.heartbeat = Instant.now().toEpochMilli();
	}

	public long getHeartbeat()
	{
		return this.heartbeat;
	}

	public void setHeartbeat(long heartbeat)
	{
		this.heartbeat = heartbeat;
	}

	public boolean isTimedConnection()
	{
		return this.timedConnection;
	}
}

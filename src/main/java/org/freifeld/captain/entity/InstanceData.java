package org.freifeld.captain.entity;

import java.time.Instant;

/**
 * @author royif
 * @since 16/10/17.
 */
public class InstanceData
{
	private String serviceName;
	private long heartbeat;
	private boolean timedConnection;

	public InstanceData(InstanceData other)
	{
		this(other.serviceName, other.timedConnection);
	}

	public InstanceData(String serviceName, boolean timedConnection)
	{

		this.serviceName = serviceName;
		this.timedConnection = timedConnection;
		this.heartbeat = Instant.now().toEpochMilli();
	}

	public String getServiceName()
	{
		return serviceName;
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

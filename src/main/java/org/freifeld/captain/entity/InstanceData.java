package org.freifeld.captain.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

/**
 * @author royif
 * @since 16/10/17.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceData
{
	private long heartbeat;
	private boolean timedConnection;

	public InstanceData()
	{
	}

	public InstanceData(InstanceData other)
	{
		this(other.timedConnection);
	}

	public InstanceData(boolean timedConnection)
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

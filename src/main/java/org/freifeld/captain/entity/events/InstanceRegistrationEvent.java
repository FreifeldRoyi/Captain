package org.freifeld.captain.entity.events;

import org.freifeld.captain.entity.ServiceData;

/**
 * @author royif
 * @since 23/11/17.
 */
public class InstanceRegistrationEvent
{
	private ServiceData serviceData;

	public InstanceRegistrationEvent(ServiceData serviceData)
	{
		this.serviceData = serviceData;
	}

	public ServiceData getServiceData()
	{
		return this.serviceData;
	}
}

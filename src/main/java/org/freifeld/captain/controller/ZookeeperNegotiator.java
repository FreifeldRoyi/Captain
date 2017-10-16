package org.freifeld.captain.controller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.freifeld.captain.entity.ServiceData;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

/**
 * @author royif
 * @since 06/09/17.
 */
@Stateless
public class ZookeeperNegotiator
{
	@Inject
	private CuratorFramework curatorFramework;

	@Inject
	private ServiceDiscovery<ServiceData> serviceDiscovery;

	public ServiceInstance<ServiceData> register(String serviceName, boolean timed)
	{
		ServiceInstance<ServiceData> toReturn = null;
		try
		{
			ServiceInstance<ServiceData> instance = ServiceInstance.<ServiceData>builder()
					.name(serviceName)
					.payload(new ServiceData(timed))
					.build();
			this.serviceDiscovery.registerService(instance);
			toReturn = instance;
			//TODO log that a new service instance was registered
		}
		catch (Exception e)
		{
			//TODO logs
			e.printStackTrace();
		}
		return toReturn;
	}

	public boolean unregister(String serviceName, String id)
	{
		boolean toReturn = false;
		try
		{
			ServiceInstance<ServiceData> instance = this.serviceDiscovery.queryForInstance(serviceName, id);
			toReturn = this.unregister(instance);
		}
		catch (Exception e)
		{
			//TODO logs
			e.printStackTrace();
		}
		return toReturn;
	}

	public boolean unregister(ServiceInstance<ServiceData> instance)
	{
		boolean toReturn = false;
		try
		{
			if (instance != null)
			{
				//TODO log that instance was unregistered
				this.serviceDiscovery.unregisterService(instance);
				toReturn = true;
			}
			else
			{
				//TODO log that nothing was found
			}
		}
		catch (Exception e)
		{
			//TODO logs
			e.printStackTrace();
		}
		return toReturn;
	}

	public ServiceInstance<ServiceData> update(String name, String id)
	{
		ServiceInstance<ServiceData> toReturn = null;
		try
		{
			ServiceInstance<ServiceData> instance = this.serviceDiscovery.queryForInstance(name, id);
			if (instance != null)
			{
				ServiceInstance<ServiceData> updatedInstance = ServiceInstance.<ServiceData>builder()
						.payload(new ServiceData(instance.getPayload()))
						.name(instance.getName())
						.id(instance.getId())
						.serviceType(instance.getServiceType())
						.registrationTimeUTC(instance.getRegistrationTimeUTC())
						//.port(instance.getPort())
						//.sslPort(instance.getSslPort())
						.address(instance.getAddress())
						.enabled(instance.isEnabled())
						.uriSpec(instance.getUriSpec())
						.build();
				this.serviceDiscovery.updateService(updatedInstance);
				toReturn = updatedInstance;
			}
		}
		catch (Exception e)
		{
			//TODO logs
			e.printStackTrace();
		}

		return toReturn;
	}

	public Collection<ServiceInstance<ServiceData>> getChildrenFor(String serviceName)
	{
		Collection<ServiceInstance<ServiceData>> toReturn = Collections.emptyList();
		try
		{
			toReturn = this.serviceDiscovery.queryForInstances(serviceName);
		}
		catch (Exception e)
		{
			//TODO logs
			e.printStackTrace();
		}
		return toReturn;
	}

	public Collection<String> getAllServices()
	{
		Collection<String> toReturn = Collections.emptyList();
		try
		{
			toReturn = this.serviceDiscovery.queryForNames();
		}
		catch (Exception e)
		{
			//TODO logs
			e.printStackTrace();
		}

		return toReturn;
	}
}

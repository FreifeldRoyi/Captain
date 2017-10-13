package org.freifeld.captain.controller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.Instant;
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
	private ServiceDiscovery<Long> serviceDiscovery;

	public ServiceInstance<Long> register(String serviceName)
	{
		ServiceInstance<Long> toReturn = null;
		try
		{
			ServiceInstance<Long> instance = ServiceInstance.<Long>builder()
					.name(serviceName)
					.payload(Instant.now().toEpochMilli())
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
			ServiceInstance<Long> instance = this.serviceDiscovery.queryForInstance(serviceName, id);
			toReturn = this.unregister(instance);
		}
		catch (Exception e)
		{
			//TODO logs
			e.printStackTrace();
		}
		return toReturn;
	}

	public boolean unregister(ServiceInstance<Long> instance)
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

	public ServiceInstance<Long> update(String name, String id)
	{
		ServiceInstance<Long> toReturn = null;
		try
		{
			ServiceInstance<Long> instance = this.serviceDiscovery.queryForInstance(name, id);
			if (instance != null)
			{
				ServiceInstance<Long> updatedInstance = ServiceInstance.<Long>builder()
						.payload(Instant.now().toEpochMilli())
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

	public Collection<ServiceInstance<Long>> getChildrenFor(String serviceName)
	{
		Collection<ServiceInstance<Long>> toReturn = Collections.emptyList();
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

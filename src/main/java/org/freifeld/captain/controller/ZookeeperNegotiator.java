package org.freifeld.captain.controller;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.freifeld.captain.entity.InstanceData;
import org.freifeld.captain.entity.ServiceData;
import org.freifeld.captain.entity.events.InstanceRegistrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * @author royif
 * @since 06/09/17.
 */
@Stateless
public class ZookeeperNegotiator
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperNegotiator.class);

	@Inject
	private ServiceDiscovery<InstanceData> serviceDiscovery;

	@Inject
	private ConcurrentMap<String, ServiceProvider<InstanceData>> providers;

	@Inject
	private Event<InstanceRegistrationEvent> registrationEvent;

	public ServiceInstance<InstanceData> register(String serviceName, boolean timed)
	{
		ServiceInstance<InstanceData> toReturn = null;
		try
		{
			ServiceInstance<InstanceData> instance = ServiceInstance.<InstanceData>builder()
					.name(serviceName)
					.payload(new InstanceData(serviceName, timed))
					.build();
			this.serviceDiscovery.registerService(instance);
			toReturn = instance;
			this.registrationEvent.fire(new InstanceRegistrationEvent(new ServiceData(serviceName))); //TODO this is horrible. use some factory
			LOGGER.info("A new service instance was registered {}/{}", instance.getName(), instance.getId());
		}
		catch (Exception e)
		{
			LOGGER.error("An Exception has occurred while trying to register a new service instance", e);
		}
		return toReturn;
	}

	//TODO should think of a better return value then a boolean
	public boolean unregisterInstance(String serviceName, String id)
	{
		boolean toReturn = false;
		try
		{
			ServiceInstance<InstanceData> instance = this.serviceDiscovery.queryForInstance(serviceName, id);
			if (instance != null)
			{
				toReturn = this.unregisterInstance(instance);
			}
			else
			{
				LOGGER.warn("Service instance {}/{} was not found", serviceName, id);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("An Exception has occurred while trying to unregister a new service instance {}/{}", serviceName, id, e);
		}
		return toReturn;
	}

	/**
	 * Unregisters a ServiceInstance
	 *
	 * @param instance - non null service instance
	 * @return true if the service was unregistered, false otherwise
	 */
	//TODO should think of a better return value then a boolean
	public boolean unregisterInstance(ServiceInstance<InstanceData> instance)
	{
		boolean toReturn = false;
		try
		{
			this.serviceDiscovery.unregisterService(instance);
			toReturn = true;
			LOGGER.info("Successfully unregistered a service instance {}/{}", instance.getName(), instance.getId());
		}
		catch (Exception e)
		{
			LOGGER.error("An Exception has occurred while trying to unregister a service instance", e);
		}
		return toReturn;
	}

	public ServiceInstance<InstanceData> update(String name, String id)
	{
		ServiceInstance<InstanceData> toReturn = null;
		try
		{
			ServiceInstance<InstanceData> instance = this.serviceDiscovery.queryForInstance(name, id);
			if (instance != null)
			{
				ServiceInstance<InstanceData> updatedInstance = ServiceInstance.<InstanceData>builder()
						.payload(new InstanceData(instance.getPayload()))
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
			LOGGER.error("Failed to update a service instance {}/{}", name, id, e);
		}

		return toReturn;
	}

	public ServiceInstance<InstanceData> discoverInstance(String serviceName)
	{
		return Optional.ofNullable(this.providers.get(serviceName)).map(p ->
		{
			try
			{
				return p.getInstance();
			}
			catch (Exception e)
			{
				LOGGER.error("Exception while trying to fetch an instance for {}", serviceName, e);
				return null;
			}
		}).orElseGet(() ->
		{
			LOGGER.info("No instance for service {}", serviceName);
			return null;
		});
	}

	public Collection<ServiceInstance<InstanceData>> getChildrenFor(String serviceName)
	{
		Collection<ServiceInstance<InstanceData>> toReturn = Collections.emptyList();
		try
		{
			toReturn = this.serviceDiscovery.queryForInstances(serviceName);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to retrieve child nodes for {}", serviceName, e);
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
			LOGGER.error("Failed to retrieve services", e);
		}

		return toReturn;
	}
}

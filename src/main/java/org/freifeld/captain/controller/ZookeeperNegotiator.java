package org.freifeld.captain.controller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.zookeeper.data.Stat;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.freifeld.captain.controller.ZookeeperConstants.DISCOVERY_BUCKET;
import static org.freifeld.captain.controller.ZookeeperConstants.ZOOKEEPER_SEPARATOR;

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

	/**
	 * @param serviceName the name of the service to register
	 * @return the id of the newly registered service
	 */
	public String register(String serviceName)
	{
		String toReturn = null;
		String basePath = DISCOVERY_BUCKET + ZOOKEEPER_SEPARATOR + serviceName;
		try
		{
			Stat stat = this.curatorFramework.checkExists().forPath(basePath);
			long instanceId;
			if (stat != null)
			{
				instanceId = stat.getNumChildren();
			}
			else
			{
				instanceId = 1;
				this.curatorFramework.createContainers(basePath);
				//TODO log that a new unknown service was created
			}
			ServiceInstance<Long> instance = ServiceInstance.<Long>builder()
					.name(serviceName)
					.payload(instanceId)
					.build();
			this.serviceDiscovery.registerService(instance);
			toReturn = instance.getId();
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

package org.freifeld.captain.controller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.freifeld.captain.controller.configuration.ConfigVariable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.time.Instant;

import static org.freifeld.captain.controller.ZookeeperConstants.DISCOVERY_BUCKET;

/**
 * @author royif
 * @since 11/10/17.
 */
@Startup
@Singleton
public class Initializer
{
	@Inject
	@ConfigVariable("DISCOVERY_SERVICE_NAME")
	private String discoveryServiceName;

	@Inject
	@ConfigVariable("ZOOKEEPER_ADDRESS")
	private String zookeeperAddress;

	@Inject
	@ConfigVariable("ZOOKEEPER_CONNECTION_RETRY_MS")
	private int zookeeperConnectionRetryMs;

	private CuratorFramework curatorFramework;

	private ServiceDiscovery<Long> serviceDiscovery;

	private ServiceInstance<Long> thisInstance;

	@PostConstruct
	private void init()
	{
		this.startCuratorFramework();
		this.startServiceDiscovery();
	}

	private void startCuratorFramework()
	{
		this.curatorFramework = CuratorFrameworkFactory.newClient(this.zookeeperAddress, new RetryForever(this.zookeeperConnectionRetryMs));
		this.curatorFramework.start();
	}

	private void startServiceDiscovery()
	{
		try
		{
			this.thisInstance = ServiceInstance.<Long>builder()
					.name(this.discoveryServiceName)
					.payload(Instant.now().toEpochMilli())
					.build();

			this.serviceDiscovery = ServiceDiscoveryBuilder.builder(Long.class)
					.client(this.curatorFramework)
					.basePath(DISCOVERY_BUCKET)
					.thisInstance(this.thisInstance)
					.build();

			this.serviceDiscovery.start();
		}
		catch (Exception e)
		{
			//TODO logs
			e.printStackTrace();
		}
	}

	@PreDestroy
	public void destroy()
	{
		this.curatorFramework.close();
	}

	@Produces
	public CuratorFramework curatorFramework()
	{
		return this.curatorFramework;
	}

	@Produces
	public ServiceDiscovery<Long> serviceDiscovery()
	{
		return this.serviceDiscovery;
	}
}

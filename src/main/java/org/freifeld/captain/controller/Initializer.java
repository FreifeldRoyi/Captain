package org.freifeld.captain.controller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.freifeld.captain.controller.configuration.ConfigVariable;
import org.freifeld.captain.entity.InstanceData;
import org.freifeld.captain.entity.ServiceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.freifeld.captain.controller.ZookeeperConstants.buildServiceBucket;

/**
 * @author royif
 * @since 11/10/17.
 */
@Startup
@Singleton
public class Initializer
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

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

	private ServiceDiscovery<InstanceData> serviceDiscovery;

	private ServiceInstance<InstanceData> thisInstance;

	private ConcurrentMap<String, ServiceProvider<InstanceData>> providers;

	private TreeCache serviceCache;

	@PostConstruct
	private void init()
	{
		LOGGER.info("Initializing Captain");
		this.startCuratorFramework();
		this.startServiceDiscovery();
		this.startServiceCache();
		this.startServiceProviders();
		LOGGER.info("Captain {} - Initialization complete", this.thisInstance.getId());
	}

	private void startCuratorFramework()
	{
		LOGGER.info("Initializing Apache Curator Framework");
		this.curatorFramework = CuratorFrameworkFactory.newClient(this.zookeeperAddress, new RetryForever(this.zookeeperConnectionRetryMs));
		this.curatorFramework.start();
	}

	private void startServiceDiscovery()
	{
		LOGGER.info("Initializing Service Discovery");
		try
		{
			this.thisInstance = ServiceInstance.<InstanceData>builder()
					.name(this.discoveryServiceName)
					.payload(new InstanceData(false))
					.build();

			this.serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceData.class)
					.client(this.curatorFramework)
					.basePath(buildServiceBucket())
					.thisInstance(this.thisInstance)
					.build();
			this.serviceDiscovery.start();
			LOGGER.info("Captain {} - Initialized service discovery module", this.thisInstance.getId());
		}
		catch (Exception e)
		{
			LOGGER.error("Service Discovery modules could not be initialized properly", e);
		}
	}

	private void startServiceCache()
	{
		LOGGER.info("Captain {} - Initializing Service Cache", this.thisInstance.getId());
		try
		{
			this.serviceCache = TreeCache.newBuilder(this.curatorFramework, buildServiceBucket())
					.setMaxDepth(1)
					.build();
			this.serviceCache.start();
			/*
			 * TODO should add change listeners
			 * NOTE that once this tree is active changes will occur even when startServiceProviders has not finished
			 * 	A listener should be added with some sort of action queue and a handler thread and use addServiceProvider
			 */
		}
		catch (Exception e)
		{
			LOGGER.error("Service Cache could not be initialized properly", e);
		}
	}

	private void startServiceProviders()
	{
		LOGGER.info("Captain {} - Initializing Service Providers", this.thisInstance.getId());
		this.providers = new ConcurrentHashMap<>();
		this.serviceCache.getCurrentChildren(buildServiceBucket()).forEach((serviceName, childData) ->
		{
			String data = new String(childData.getData());
			Jsonb jsonb = JsonbBuilder.create();
			ServiceData serviceData = null;
			try
			{
				serviceData = jsonb.fromJson(data, ServiceData.class);
			}
			catch (JsonbException | NoSuchElementException e)
			{
				LOGGER.warn("Problem parsing ServiceData for {}", data, e);
				//					this.curatorFramework.setData().forPath(serviceName, InstanceProviderStrategy.RANDOM.name().getBytes());
				serviceData = new ServiceData(serviceName);
			}

			this.addServiceProvider(serviceData);
		});
	}

	public void addServiceProvider(@Observes ServiceData serviceData)
	{
		ServiceProvider<InstanceData> provider = this.serviceDiscovery.serviceProviderBuilder()
				.serviceName(serviceData.getServiceName())
				.providerStrategy(serviceData.getProviderStrategy().createProviderStrategy())
				//				.downInstancePolicy() TODO should implement this shit...
				.build();
		try
		{
			provider.start();
			ServiceProvider<InstanceData> previousVal = this.providers.putIfAbsent(serviceData.getServiceName(), provider);
			if (previousVal == null)
			{
				LOGGER.info("Captain {} - Set Service \"{}\" provider strategy to {}", this.thisInstance.getId(), serviceData.getServiceName(), serviceData.getProviderStrategy().name());
			}
		}
		catch (Exception e)
		{
			LOGGER.error("An Exception has occurred while trying to start a provider for {}", serviceData.getServiceName(), e);
		}
	}

	@PreDestroy
	public void destroy()
	{
		String instanceId = this.thisInstance.getId();
		LOGGER.info("Captain {} - Destroying Service Discovery modules", instanceId);
		CloseableUtils.closeQuietly(this.serviceCache);
		this.providers.values().forEach(CloseableUtils::closeQuietly);
		CloseableUtils.closeQuietly(this.serviceDiscovery);
		CloseableUtils.closeQuietly(this.curatorFramework);
		LOGGER.info("Captain {} - Self destruction complete", instanceId);
	}

	@Produces
	public ServiceDiscovery<InstanceData> serviceDiscovery()
	{
		return this.serviceDiscovery;
	}

	@Produces
	public ConcurrentMap<String, ServiceProvider<InstanceData>> providers()
	{
		return this.providers;
	}
}

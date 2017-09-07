package org.freifeld.captain.controller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.freifeld.captain.controller.configuration.ConfigVariable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * @author royif
 * @since 06/09/17.
 */
@Singleton
public class ZookeeperNegotiator
{
	@Inject
	@ConfigVariable("ZOOKEEPER_ADDRESS")
	public String zookeeperHost;

	@Inject
	@ConfigVariable("ZOOKEEPER_CONNECTION_RETRY_MS")
	public int zookeeperConnectionRetryMs;

	private CuratorFramework curatorFramework;

	@PostConstruct
	public void init()
	{
		this.curatorFramework = CuratorFrameworkFactory.newClient(this.zookeeperHost, new RetryForever(this.zookeeperConnectionRetryMs));
		this.curatorFramework.start();
	}

	@PreDestroy
	public void destroy()
	{
		this.curatorFramework.close();
	}

	public void registerService()
	{
		
	}

	public void removeConfig()
	{

	}
}

package org.freifeld.captain.controller;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.data.Stat;
import org.freifeld.captain.controller.configuration.ConfigVariable;
import org.freifeld.captain.controller.exception.ZookeeperException;

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
	private static final String ZOOKEEPER_SEPARATOR = "/";
	private static final String DISCOVERY_BUCKET = ZOOKEEPER_SEPARATOR + "services";

	@Inject
	@ConfigVariable(value = "ZOOKEEPER_ADDRESS")
	private String zookeeperHost;

	@Inject
	@ConfigVariable(value = "ZOOKEEPER_CONNECTION_RETRY_MS")
	private int zookeeperConnectionRetryMs;

	private CuratorFramework curatorFramework;
	private TreeCache cache;

	@PostConstruct
	public void init() throws Exception
	{
		this.curatorFramework = CuratorFrameworkFactory.newClient(this.zookeeperHost, new RetryForever(this.zookeeperConnectionRetryMs));
		this.curatorFramework.start();

		this.createZookeeperTree();
		this.cache.start();
	}

	@PreDestroy
	public void destroy()
	{
		this.cache.close();
		this.curatorFramework.close();
	}

	private void createZookeeperTree() throws ZookeeperException
	{
		try
		{
			String s = this.curatorFramework.create().orSetData().forPath(DISCOVERY_BUCKET);

			System.out.println(s); //TODO
		}
		catch (Exception e)
		{
			throw new ZookeeperException("Could not create initial zookeeper tree", e);
		}
	}

	public String getUri(String serviceName) throws ZookeeperException
	{
		String toReturn = null;
		try
		{
			Stat stat = this.curatorFramework.checkExists().forPath(DISCOVERY_BUCKET + ZOOKEEPER_SEPARATOR + serviceName);
			if (stat != null)
			{
				byte[] bytes = this.curatorFramework.getData().forPath(DISCOVERY_BUCKET + ZOOKEEPER_SEPARATOR + serviceName);
				toReturn = new String(bytes);
			}
		}
		catch (Exception e)
		{
			throw new ZookeeperException("Could not get Uri for " + serviceName, e);
		}

		return toReturn;
	}
}

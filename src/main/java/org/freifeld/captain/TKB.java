package org.freifeld.captain;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;

/**
 * @author royif
 * @since 06/09/17.
 */
public class TKB
{
	public static void main(String[] args)
	{
		CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("localhost:2181", new RetryForever(1000));
		curatorFramework.start();

		String path = "/freifeld/testing/this/cxcc/sdg";

		try
		{
			if (curatorFramework.checkExists().forPath(path) == null)
			{
				String val = curatorFramework.create().creatingParentsIfNeeded().forPath(path);
				System.out.println(val);
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		curatorFramework.close();
	}
}

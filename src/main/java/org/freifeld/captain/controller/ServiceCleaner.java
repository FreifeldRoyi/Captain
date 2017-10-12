package org.freifeld.captain.controller;

import javax.ejb.*;

/**
 * @author royif
 * @since 11/10/17.
 */
@Singleton
@Startup
@DependsOn("Initializer")
public class ServiceCleaner
{
	@EJB
	ZookeeperNegotiator zookeeperNegotiator;

	@Schedule(hour = "*", minute = "*", second = "*/20")
	public void cleanServices()
	{
		System.out.println("cleaning");
	}
}

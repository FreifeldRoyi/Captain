package org.freifeld.captain.controller;

import org.freifeld.captain.controller.configuration.ConfigVariable;

import javax.ejb.*;
import javax.inject.Inject;
import java.time.Instant;
import java.util.Collection;

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
	private ZookeeperNegotiator zookeeperNegotiator;

	@Inject
	@ConfigVariable("DISCOVERY_SERVICE_NAME")
	private String discoveryServiceName;

	@Inject
	@ConfigVariable("HEARTBEAT_THRESHOLD")
	private int heartbeatThreshold;

	@Schedule(hour = "*", minute = "*", second = "*/30")
	public void cleanServices()
	{
		Instant now = Instant.now();
		this.zookeeperNegotiator.getAllServices().stream()
				.map(s -> this.zookeeperNegotiator.getChildrenFor(s))
				.flatMap(Collection::stream)
				.filter(instance -> !discoveryServiceName.equals(instance.getName()))
				.filter(instance -> now.minusMillis(instance.getPayload()).toEpochMilli() > this.heartbeatThreshold)
				.forEach(instance -> {
					//TODO logs cleaning :instance
					this.zookeeperNegotiator.unregister(instance);
				});
	}
}

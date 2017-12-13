package org.freifeld.captain.controller.metrics;

import io.prometheus.client.Counter;
import org.freifeld.captain.controller.configuration.ConfigVariable;
import org.freifeld.captain.entity.events.Metric;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

/**
 * @author royif
 */
@ApplicationScoped
public class MetricsObserver
{
	private static final String COUNTER_POSTFIX = "_http_requests_total";

	@Inject
	@ConfigVariable("DISCOVERY_SERVICE_NAME")
	private String discoveryServiceName;

	private Counter requestCounter;

	@PostConstruct
	public void init()
	{
		this.requestCounter = Counter.build(this.discoveryServiceName + COUNTER_POSTFIX, "Counts the number of total requests to this instance")
				.labelNames("httpMethod", "uri", "service", "instance", "eventType")
				.register();
	}

	public void listen(@ObservesAsync Metric metric)
	{
		this.requestCounter.labels(metric.getHttpMethod(), metric.getUri(), metric.getService(), metric.getInstance(), metric.getEventType().name()).inc();
	}
}

package org.freifeld.captain.entity;

import org.apache.curator.x.discovery.ProviderStrategy;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;

import java.util.function.Supplier;

/**
 * @author royif
 * @since 13/11/17.
 */
public enum InstanceProviderStrategy
{
	ROUND_ROBIN(RoundRobinStrategy::new),
	RANDOM(RandomStrategy::new),
	STICKY(RandomStrategy::new); //TODO should implement

	private final Supplier<ProviderStrategy<InstanceData>> supplier;

	InstanceProviderStrategy(Supplier<ProviderStrategy<InstanceData>> supplier)
	{
		this.supplier = supplier;
	}

	public ProviderStrategy<InstanceData> createProviderStrategy()
	{
		return supplier.get();
	}
}

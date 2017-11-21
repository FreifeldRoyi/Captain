package org.freifeld.captain.entity;

/**
 * @author royif
 * @since 13/11/17.
 */
public class ServiceData
{
	private String serviceName;
	private InstanceProviderStrategy providerStrategy;

	public ServiceData()
	{
		this(null);
	}

	/**
	 * Constructs a ServiceData object with {@link InstanceProviderStrategy#RANDOM} as the default provider strategy
	 *
	 * @param serviceName - the service name
	 */
	public ServiceData(String serviceName)
	{
		this(serviceName, InstanceProviderStrategy.RANDOM);
	}

	public ServiceData(String serviceName, InstanceProviderStrategy providerStrategy)
	{
		this.serviceName = serviceName;
		this.providerStrategy = providerStrategy;
	}

	public String getServiceName()
	{
		return this.serviceName;
	}

	public InstanceProviderStrategy getProviderStrategy()
	{
		return this.providerStrategy;
	}

	@Override
	public String toString()
	{
		return String.format("{serviceName : %s , providerStrategy : %s}", this.serviceName, this.providerStrategy);
	}
}

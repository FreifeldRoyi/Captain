package org.freifeld.captain.controller.configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * @author royif
 * @since 25/08/17.
 */
@ApplicationScoped
public class Configuration
{
	@Produces
	@ConfigVariable
	public String getStringEnv(InjectionPoint ip)
	{
		ConfigVariable metaData = ip.getAnnotated().getAnnotation(ConfigVariable.class);
		return System.getenv().get(metaData.value());
	}

	@Produces
	@ConfigVariable
	public int getIntEnv(InjectionPoint ip)
	{
		String env = this.getStringEnv(ip);
		return Integer.parseInt(env);
	}
}

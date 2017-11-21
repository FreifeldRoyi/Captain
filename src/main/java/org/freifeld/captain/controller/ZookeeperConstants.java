package org.freifeld.captain.controller;

import java.util.Arrays;

/**
 * @author royif
 * @since 11/10/17.
 */
public class ZookeeperConstants
{

	static final String ZOOKEEPER_SEPARATOR = "/";
	static final String DISCOVERY_BUCKET = ZOOKEEPER_SEPARATOR + "ServiceDiscovery";

	public static String buildServiceBucket(String... path)
	{
		return Arrays.stream(path).reduce((s, s2) -> s + ZOOKEEPER_SEPARATOR + s2).map(s -> DISCOVERY_BUCKET + ZOOKEEPER_SEPARATOR + s).orElse(DISCOVERY_BUCKET);
	}

	/**
	 * Extracts the service name from a full Zookeeper path including the
	 * {@link #DISCOVERY_BUCKET} & {@link #ZOOKEEPER_SEPARATOR}
	 *
	 * @param path - non null full Zookeeper node path
	 * @return an Optional containing the extracted service name or an empty
	 * optional if no serviceName has been found
	 */
	public static String extractServiceName(String path)
	{
		String toReturn = null;
		String pathPrefix = DISCOVERY_BUCKET + ZOOKEEPER_SEPARATOR;
		if (path.startsWith(pathPrefix))
		{
			String possibleServiceName = path.substring(pathPrefix.length());
			if (!possibleServiceName.equals(""))
			{
				toReturn = possibleServiceName;
			}
		}

		return toReturn;
	}
}

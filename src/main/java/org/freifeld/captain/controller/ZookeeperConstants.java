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
}

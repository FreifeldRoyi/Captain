package org.freifeld.captain.controller.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.websocket.Session;
import java.util.*;

/**
 * @author royif
 * @since 17/10/17.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class SessionHandler
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);

	//TODO need to test what is faster - container locking or ConcurrentHashMap
	private Map<String, Set<Session>> sessions;

	@PostConstruct
	public void setup()
	{
		this.sessions = new HashMap<>();
		LOGGER.info("Initialized WebSocket session handler");
	}

	@Lock(LockType.WRITE)
	public boolean removeSession(String serviceName, Session session)
	{
		Set<Session> set = this.sessions.getOrDefault(serviceName, Collections.emptySet());
		return set.remove(session);
	}

	@Lock(LockType.WRITE)
	public boolean addSession(String serviceName, Session session)
	{
		Set<Session> set = this.sessions.getOrDefault(serviceName, new HashSet<>());
		this.sessions.putIfAbsent(serviceName, set);
		return set.add(session);
	}

	@Lock(LockType.READ)
	public Optional<Session> getSession(String serviceName, String id)
	{
		Set<Session> set = this.sessions.getOrDefault(serviceName, Collections.emptySet());
		return set.stream().filter(s -> id.equals(s.getId())).findFirst();
	}
}

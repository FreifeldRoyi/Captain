package org.freifeld.captain.boundary;

import org.apache.curator.x.discovery.ServiceInstance;
import org.freifeld.captain.controller.ZookeeperNegotiator;
import org.freifeld.captain.controller.websocket.SessionHandler;
import org.freifeld.captain.entity.InstanceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * @author royif
 * @since 16/10/17.
 */
@ServerEndpoint(value = "/v1/registrations/{serviceName}")
public class RegistrationEndpoint
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationEndpoint.class);

	private ServiceInstance<InstanceData> instance;

	@EJB
	private SessionHandler sessionHandler;

	@EJB
	private ZookeeperNegotiator zookeeper;

	@OnOpen
	public void register(@PathParam("serviceName") String serviceName, Session session) throws IOException
	{
		this.instance = zookeeper.register(serviceName, false);
		session.getBasicRemote().sendText(instance.getId());
		this.sessionHandler.addSession(serviceName, session);
		LOGGER.info("Successfully registered service instance {}/{} on websocket session {}", this.instance.getName(), this.instance.getId(), session.getId());
	}

	@OnClose
	public void unregister(@PathParam("serviceName") String serviceName, Session session) throws IOException
	{
		if (this.instance != null)
		{
			this.zookeeper.unregister(this.instance);
			LOGGER.info("Successfully unregistered service instance {}/{} on websocket session {}", this.instance.getName(), this.instance.getId(), session.getId());
		}
		else
		{
			LOGGER.info("Service {} on websocket session {} was already unregistered", serviceName, session.getId());
		}
		this.sessionHandler.removeSession(serviceName, session);
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException
	{
		//TODO implement - stuff to consider
		session.getBasicRemote().sendText("Echo for " + this.instance.getName() + "/" + this.instance.getId() + ": " + message);
	}

	@OnError
	public void onError(Throwable throwable, Session session) throws IOException
	{
		//TODO implement
		LOGGER.error("Exception on websocket session {}", session.getId(), throwable);
	}
}

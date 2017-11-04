package org.freifeld.captain.boundary;

import org.apache.curator.x.discovery.ServiceInstance;
import org.freifeld.captain.controller.ZookeeperNegotiator;
import org.freifeld.captain.controller.websocket.SessionHandler;
import org.freifeld.captain.entity.ServiceData;

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
	private ServiceInstance<ServiceData> instance;

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
	}

	@OnClose
	public void unregister(@PathParam("serviceName") String serviceName, Session session) throws IOException
	{
		if (this.instance != null)
		{
			this.zookeeper.unregister(this.instance);
			System.out.println("Unregistered " + this.instance.getName() + "/" + this.instance.getId());
		}
		else
		{
			//TODO logs
			System.out.println("Instance was not initialized so we're cool =)");
		}
		this.sessionHandler.removeSession(serviceName, session);
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException
	{
		//TODO implement - stuff to consider
		System.out.println("watching " + message);
		session.getBasicRemote().sendText("Echo for " + this.instance.getName() + "/" + this.instance.getId() + ": " + message);
	}

	@OnError
	public void onError(Throwable throwable, Session session) throws IOException
	{
		//TODO logs
		//TODO implement
		System.out.println("WS ERROR!!");
		throwable.printStackTrace();
	}
}

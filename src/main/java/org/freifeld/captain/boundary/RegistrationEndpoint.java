package org.freifeld.captain.boundary;

import org.apache.curator.x.discovery.ServiceInstance;
import org.freifeld.captain.controller.ZookeeperNegotiator;
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
@ServerEndpoint(value = "/registrations/{serviceName}")
public class RegistrationEndpoint
{
	private ServiceInstance<ServiceData> instance;

	@EJB
	private ZookeeperNegotiator zookeeper;

	@OnOpen
	public void register(@PathParam("serviceName") String serviceName, Session session) throws IOException
	{
		this.instance = zookeeper.register(serviceName, false);
		session.getBasicRemote().sendText(instance.getId());
	}

	@OnClose
	public void unregister(Session session) throws IOException
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
	}

	@OnMessage
	public void watch(String message, Session session) throws IOException
	{
		//TODO implement
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

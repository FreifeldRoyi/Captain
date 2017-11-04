package org.freifeld.captain.boundary;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author royif
 * @since 18/10/17.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Path("/v1/sse/registrations")
public class RegistrationSSEResource
{
	private Sse sse;

	private ConcurrentMap<String, SseBroadcaster> sseBroadcasters;

	@PostConstruct
	public void init()
	{
		this.sseBroadcasters = new ConcurrentHashMap<>();
	}

	@GET
	@Path("{serviceName}")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public void register(@PathParam("serviceName") String serviceName, @Context Sse sse, @Context SseEventSink eventSink)
	{
		this.sse = sse;
		SseBroadcaster broadcaster = Optional.ofNullable(this.sseBroadcasters.get(serviceName)).orElseGet(() ->
		{
			System.out.println("HERE 222@@@");
			SseBroadcaster toReturn = this.sse.newBroadcaster();
			toReturn.onClose(sink ->
			{
				System.out.println("CLOSING sink");
				this.onClose(sink);
			}); //TODO NOT WORKING
			toReturn.onError((eventSink1, throwable) ->
			{
				System.out.println("sink error");
				throwable.printStackTrace();
			}); //TODO NOT WORKING
			return toReturn;
		});
		broadcaster.register(eventSink);
		this.sseBroadcasters.putIfAbsent(serviceName, broadcaster);
	}

	public void onClose(SseEventSink eventSink)
	{
		System.out.println("closing a sink");
	}

	@GET
	@Path("broadcasters")
	public JsonObject broadcasters()
	{
		JsonObjectBuilder objBuilder = Json.createObjectBuilder();
		JsonArrayBuilder builder = Json.createArrayBuilder();
		this.sseBroadcasters.keySet().forEach(builder::add);
		objBuilder.add("size", this.sseBroadcasters.size());
		objBuilder.add("all", builder);
		return objBuilder.build();
	}

	@Schedule(hour = "*", minute = "*", second = "*/3")
	public void scheduled()
	{
		this.sseBroadcasters.forEach((serviceName, broadcaster) ->
		{
			OutboundSseEvent event = this.sse.newEventBuilder().comment("this is a comment").data("This is my data " + serviceName).id("some id").name("This is a name").build();
			System.out.println(String.format("Sending to %s: %s", serviceName, event.toString()));
			broadcaster.broadcast(event);
		});
	}

}

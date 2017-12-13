package org.freifeld.captain.entity.events;

/**
 *
 * @author royif
 */
public class Metric
{
	private String httpMethod;
	private String uri;
	private String service;
	private String instance;
	private MetricEventType eventType;

	private Metric()
	{
	}

	private Metric(Builder builder)
	{
		this.httpMethod = builder.httpMethod;
		this.uri = builder.uri;
		this.service = builder.service;
		this.instance = builder.instance;
		this.eventType = builder.eventType;
	}

	public String getHttpMethod()
	{
		return this.httpMethod;
	}

	public String getUri()
	{
		return this.uri;
	}

	public String getService()
	{
		return this.service;
	}

	public String getInstance()
	{
		return this.instance;
	}
	
	public MetricEventType getEventType()
	{
		return this.eventType;
	}

	public static Builder builder() {
		return new Builder();
	}

	public enum MetricEventType
	{
		BEGIN,
		END,
		TIMEOUT,
		NONE;
	}
	
	public static class Builder
	{
		private String httpMethod;
		private String uri;
		private String service;
		private String instance;
		private MetricEventType eventType;

		private Builder()
		{
			this("", "", "/", "", MetricEventType.NONE);
		}

		private Builder(String httpMethod, String uri, String service, String instance, MetricEventType eventType)
		{
			this.httpMethod = httpMethod;
			this.uri = uri;
			this.service = service;
			this.instance = instance;
			this.eventType = eventType;
		}
		
		public Metric build()
		{
			return new Metric(this);
		}

		public Builder httpMethod(String httpMethod)
		{
			this.httpMethod = httpMethod;
			return this;
		}

		public Builder uri(String uri)
		{
			this.uri = uri;
			return this;
		}

		public Builder service(String service)
		{
			this.service = service;
			return this;
		}

		public Builder instance(String instance)
		{
			this.instance = instance;
			return this;
		}

		public Builder eventType(MetricEventType eventType)
		{
			this.eventType = eventType;
			return this;
		}
	}
}

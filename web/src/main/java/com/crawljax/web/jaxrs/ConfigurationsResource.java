package com.crawljax.web.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.crawljax.web.model.Configuration;
import com.crawljax.web.model.Configurations;
import com.crawljax.web.model.Plugin;
import com.crawljax.web.model.Plugins;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/rest/configurations")
public class ConfigurationsResource {

	private final Configurations configurations;
	private final Plugins plugins;

	@Inject
	ConfigurationsResource(Configurations configurations, Plugins plugins) {
		this.configurations = configurations;
		this.plugins = plugins;
	}

	@GET
	public Response getConfigurations() {
		return Response.ok(configurations.getConfigList()).build();
	}

	@POST
	public Response addConfiguration(Configuration config) {
		config = configurations.add(config);
		return Response.ok(config).build();
	}

	@GET
	@Path("/new/{id}")
	public Response getNewConfiguration(@PathParam("id") String id) {
		Configuration config = new Configuration();
		if(id != null && !id.isEmpty()) {
			config = configurations.getCopyOf(id);
		}
		return Response.ok(config).build();
	}

	@GET
	@Path("/new")
	public Response getNewConfiguration() {
		Configuration config = new Configuration();
		return Response.ok(config).build();
	}

	@GET
	@Path("{id}")
	public Response getConfiguration(@PathParam("id") String id) {
		Response r;
		Configuration config = configurations.findByID(id);
		if (config != null)
			r = Response.ok(config).build();
		else
			r = Response.serverError().build();
		return r;
	}

	@PUT
	@Path("{id}")
	public Response updateConfiguration(Configuration config) {
		config = configurations.update(config);
		return Response.ok(config).build();
	}

	@DELETE
	@Path("{id}")
	public Response removeConfiguration(Configuration config) {
		config = configurations.remove(config);
		return Response.ok(config).build();
	}
}

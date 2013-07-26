package com.crawljax.web.jaxrs;

import java.io.IOException;

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

import sun.misc.BASE64Decoder;

import com.crawljax.web.model.Plugin;
import com.crawljax.web.model.Plugins;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.multipart.FormDataParam;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/rest/plugins")
public class PluginsResource {

	private final Plugins plugins;

	@Inject
	PluginsResource(Plugins plugins) {
		this.plugins = plugins;
	}

	@GET
	public Response getPlugins() {
		return Response.ok(plugins.getPluginList()).build();
	}

	@PUT
	public Response refresh() {
		return Response.ok(plugins.reloadFromDisk()).build();
	}

	@GET
	@Path("{id}")
	public Response getPlugin(@PathParam("id") String id) {
		Response r;
		Plugin config = plugins.findByID(id);
		if (config != null)
			r = Response.ok(config).build();
		else
			r = Response.serverError().build();
		return r;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addPlugin(@FormDataParam("name") String name,
	        @FormDataParam("file") String file) {
		String content = file.substring(file.indexOf(',') + 1);
		BASE64Decoder decoder = new BASE64Decoder();
		Plugin plugin = new Plugin();
		try {
			byte[] decodedBytes = decoder.decodeBuffer(content);
			plugin = plugins.add(name, decodedBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Response.ok(plugin).build();
	}

	@DELETE
	@Path("{id}")
	public Response removePlugin(Plugin plugin) {
		plugin = plugins.remove(plugin);
		return Response.ok(plugin).build();
	}
}

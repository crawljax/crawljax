package com.crawljax.web.jaxrs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

import com.crawljax.web.LogWebSocketServlet;
import com.crawljax.web.exception.CrawljaxWebException;
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
		return Response.ok(plugins.reloadAll()).build();
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
	        @FormDataParam("file") String file,
			@FormDataParam("url") String url) {
		Plugin plugin = null;
		boolean error = false;
		if(file != null) {
			String content = file.substring(file.indexOf(',') + 1);
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				byte[] decodedBytes = decoder.decodeBuffer(content);
				try {
					plugin = plugins.add(name, decodedBytes);
				} catch (CrawljaxWebException e) {
					LogWebSocketServlet.sendToAll("message-error-" + e.getMessage());
					error = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(url != null) {
			try {
				URL urlObject = new URL(url);
				plugin = plugins.add(name, urlObject);
			} catch (MalformedURLException e) {
				LogWebSocketServlet.sendToAll("message-error-Invalid URL");
				error = true;
			} catch (CrawljaxWebException e) {
				LogWebSocketServlet.sendToAll("message-error-" + e.getMessage());
				error = true;
			}
		}
		if(!error) {
			LogWebSocketServlet.sendToAll("message-success-Plugin Loaded");
		}
		return Response.ok(plugin).build();
	}

	@DELETE
	@Path("{id}")
	public Response removePlugin(Plugin plugin) {
		try {
			plugin = plugins.remove(plugin);
		} catch (CrawljaxWebException e) {
			LogWebSocketServlet.sendToAll("message-error-" + e.getMessage());
		}
		return Response.ok(plugin).build();
	}
}

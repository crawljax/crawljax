package com.crawljax.web.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class MainResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String helloWorld() {
		return "Hellow JaxRS!";
	}
}

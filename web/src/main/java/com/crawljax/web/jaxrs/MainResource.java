package com.crawljax.web.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/rest/configurations")
public class MainResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getConfigurations() {
		return "{\"data\": [" +
    		    "{\"id\": 1, \"status\": \"Idle\", \"name\": \"Some Config\", \"site\": \"http://www.google.com\", \"lastCrawl\": \"1/12/2013 4:23 PM\", \"lastDuration\": \"13 min 26 sec\"},"+
    		    "{\"id\": 2, \"status\": \"Idle\", \"name\": \"Other Config\", \"site\": \"http://www.yahoo.com\", \"lastCrawl\": \"1/23/2013 10:37 AM\", \"lastDuration\": \"9 min 48 sec\"},"+
    		    "{\"id\": 3, \"status\": \"Idle\", \"name\": \"Another Config\", \"site\": \"http://www.msn.com\", \"lastCrawl\": \"1/27/2013 7:37 AM\", \"lastDuration\": \"27 min 3 sec\"}"+
    		    "]}";
	}
}

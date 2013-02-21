package com.crawljax.web.jaxrs;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.crawljax.web.model.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/rest/history")
public class CrawlHistoryResource {

	private final CrawlRecords crawlRecords;
	
	@Inject
	CrawlHistoryResource(CrawlRecords crawlRecords) {
        this.crawlRecords = crawlRecords;
    }
	
	@GET
	public Response getHistory(@QueryParam("config") String configId ) {
		List<CrawlRecord> list;
		if (configId == null) list = crawlRecords.getCrawlList();
		else list = crawlRecords.getCrawlListByConfigID(configId);
		return Response.ok(list).build();
	}
	
	@POST
	public Response addCrawlRecord(CrawlRecord record){
		record = crawlRecords.add(record);
		return Response.ok(record).build();
	}
	
	@GET
	@Path("{id}")
	public Response getCrawlRecord(@PathParam("id") int id) {
		Response r;
		CrawlRecord record = crawlRecords.findByID(id);
		if (record != null) r = Response.ok(record).build();
		else r = Response.serverError().build();
		return r;
	}
}

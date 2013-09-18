package com.crawljax.web.jaxrs;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.crawljax.web.fs.WorkDirManager;
import com.crawljax.web.model.CrawlRecord;
import com.crawljax.web.model.CrawlRecords;
import com.crawljax.web.runner.CrawlRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/rest/history")
public class CrawlHistoryResource {

	private final CrawlRecords crawlRecords;
	private final WorkDirManager workDirManager;
	private final CrawlRunner runner;

	@Inject
	CrawlHistoryResource(CrawlRecords crawlRecords, WorkDirManager workDirManager,
	        CrawlRunner runner) {
		this.crawlRecords = crawlRecords;
		this.workDirManager = workDirManager;
		this.runner = runner;
	}

	@GET
	public Response getHistory(@QueryParam("config") String configId,
	        @QueryParam("active") Boolean active) {
		List<CrawlRecord> list;
		if (configId != null)
			list = crawlRecords.getCrawlListByConfigID(configId);
		else if (active != null && active)
			list = crawlRecords.getActiveCrawlList();
		else
			list = crawlRecords.getCrawlList();
		return Response.ok(list).build();
	}

	@POST
	public Response addCrawlRecord(String configId) {
		CrawlRecord record = crawlRecords.add(configId);
		runner.queue(record);
		return Response.ok(record).build();
	}

	@GET
	@Path("{id}")
	public Response getCrawlRecord(@PathParam("id") int id) {
		Response r;
		CrawlRecord record = crawlRecords.findByID(id);
		if (record != null)
			r = Response.ok(record).build();
		else
			r = Response.serverError().build();
		return r;
	}

}

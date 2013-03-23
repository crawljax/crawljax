package com.crawljax.test;

import static org.junit.Assert.*;
import java.net.URL;
import org.eclipse.jetty.util.resource.Resource;
import java.io.IOException;
import com.crawljax.core.CrawljaxException;

import org.junit.Test;

public class WebServerTest {
	private URL site;
	private WebServer server;
	
	private void setup() throws Exception {
		site = BaseCrawler.class.getResource("/site");
		try {
			server = new WebServer(Resource.newResource(site));
		} catch (IOException e) {
			throw new CrawljaxException("Could not load resource", e);
		}
		server.start();
	}

	@Test
	public void testSiteUrl() throws Exception {
		setup();
		assertEquals(server.getSiteUrl(), site);
	}
	
	@Test
	public void testPort() throws Exception {
		setup();
		assertEquals(server.getPort(), 0);
	}

}

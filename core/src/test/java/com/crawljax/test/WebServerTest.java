package com.crawljax.test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;

import java.net.URL;
import org.eclipse.jetty.util.resource.Resource;
import java.io.IOException;
import com.crawljax.core.CrawljaxException;


import org.junit.Test;

public class WebServerTest {
	private URL site;
	private WebServer server;
	private String siteString;
	
	static final int MAX_PORT = 65535;
	static final int MIN_PORT = 0;
	
	@Before
	public void setup() throws Exception {
		site = BaseCrawler.class.getResource("/site");
		try {
			server = new WebServer(Resource.newResource(site));
		} catch (IOException e) {
			throw new CrawljaxException("Could not load resource", e);
		}
		server.start();
	}
	
	@After
	public void stopServer() {
		server.stop();
	}

	@Test
	public void testSiteUrl() throws Exception {
		site = new URL("http", "localhost", server.getPort(), "/");
		assertEquals(site.getPort(), server.getSiteUrl().getPort());
		assertTrue(site.getPath().equals(server.getSiteUrl().getPath()));
	}
	
	@Test
	public void testPort() throws Exception {
		setup();
		assertTrue(server.getPort() >= MIN_PORT);
		assertTrue(server.getPort() <= MAX_PORT);
	}

}

package com.crawljax.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import com.crawljax.core.CrawljaxException;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebServerTest {
	private URI site;
	private WebServer server;

	private static final int MAX_PORT = 65535;
	private static final int MIN_PORT = 0;

	@Before
	public void setup() throws Exception {
		site = BaseCrawler.class.getResource("/site").toURI();
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
		site = URI.create("http://localhost:" + server.getPort());
		assertEquals(site.getPort(), server.getSiteUrl().getPort());
		assertTrue(site.getPath().equals(server.getSiteUrl().getPath()));
	}

	@Test
	public void testPort() throws Exception {
		assertThat(server.getPort(), is(lessThanOrEqualTo(MAX_PORT)));
		assertThat(server.getPort(), is(greaterThanOrEqualTo(MIN_PORT)));
	}

}

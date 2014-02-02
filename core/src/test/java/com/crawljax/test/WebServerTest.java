package com.crawljax.test;


import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
		}
		catch (IOException e) {
			throw new CrawljaxException("Could not load resource", e);
		}
		server.start();
	}

	@After
	public void stopServer() {
		server.stop();
	}

	@Test
	public void testPort() throws Exception {
		assertThat(server.getPort(), is(lessThanOrEqualTo(MAX_PORT)));
		assertThat(server.getPort(), is(greaterThanOrEqualTo(MIN_PORT)));
	}

}

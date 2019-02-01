package com.crawljax.test;

import com.crawljax.core.CrawljaxException;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WebServerTest {
	private WebServer server;

	private static final int MAX_PORT = 65535;
	private static final int MIN_PORT = 0;

	@Before
	public void setup() throws Exception {
		URI site = BaseCrawler.class.getResource("/site").toURI();
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
	public void testPort() {
		assertThat(server.getPort(), is(lessThanOrEqualTo(MAX_PORT)));
		assertThat(server.getPort(), is(greaterThanOrEqualTo(MIN_PORT)));
	}

}

package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class PassBasicHttpAuthTest {

	private static final String USERNAME = "test";
	private static final String PASSWORD = "test#&";
	private Server server;
	private int port;

	@Before
	public void setup() throws Exception {
		server = new Server(0);
		ResourceHandler handler = new ResourceHandler();
		handler.setBaseResource(Resource.newClassPathResource("/site"));

		ConstraintSecurityHandler csh = newSecurityHandler(handler);

		server.setHandler(csh);
		server.start();

		this.port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();

	}

	private ConstraintSecurityHandler newSecurityHandler(ResourceHandler handler) {
		HashLoginService login = new HashLoginService();
		login.putUser(USERNAME, Credential.getCredential(PASSWORD), new String[] { "user" });

		Constraint constraint = new Constraint();
		constraint.setName(Constraint.__BASIC_AUTH);
		constraint.setRoles(new String[] { "user" });
		constraint.setAuthenticate(true);

		ConstraintMapping cm = new ConstraintMapping();
		cm.setConstraint(constraint);
		cm.setPathSpec("/*");

		ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
		csh.setAuthenticator(new BasicAuthenticator());
		csh.addConstraintMapping(cm);
		csh.setLoginService(login);
		csh.setHandler(handler);
		return csh;
	}

	@Test
	public void testDontClickUnderXPath() throws Exception {
		String url = "http://localhost:" + port + "/infinite.html";
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(url);
		builder.setMaximumStates(3);
		builder.setBasicAuth(USERNAME, PASSWORD);
		CrawlSession session = new CrawljaxRunner(builder.build()).call();

		assertThat(session.getStateFlowGraph(), hasStates(3));
	}

	@After
	public void shutDown() throws Exception {
		server.stop();
	}

}

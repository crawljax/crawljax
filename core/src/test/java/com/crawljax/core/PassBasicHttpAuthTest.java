package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.MatcherAssert.assertThat;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnBrowserCreatedPlugin;
import com.crawljax.test.BrowserTest;
import java.net.URI;
import java.util.function.Predicate;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.HasAuthentication;
import org.openqa.selenium.UsernameAndPassword;

@Category(BrowserTest.class)
public class PassBasicHttpAuthTest {

    private static final String USERNAME = "test";
    private static final String PASSWORD = "test#&";
    private static final String USER_ROLE = "user";

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
        login.setConfig(
                PassBasicHttpAuthTest.class.getResource("/realm.properties").getPath());

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[] {USER_ROLE});
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
    public void testProvidedCredentialsAreUsedInBasicAuth() {
        String url = "http://localhost:" + port + "/infinite.html";
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(url);
        builder.setMaximumStates(3);
        builder.setBasicAuth(USERNAME, PASSWORD);
        builder.setBrowserConfig(new BrowserConfiguration(BrowserProvider.getBrowserType()));
        CrawlSession session = new CrawljaxRunner(builder.build()).call();

        assertThat(session.getStateFlowGraph(), hasStates(3));
    }

    @Test
    public void testRegisterCredentialsWithBiDi() {
        String host = "localhost";
        String url = "http://" + host + ":" + port + "/infinite.html";
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(url);

        builder.addPlugin((OnBrowserCreatedPlugin) browser -> {
            Predicate<URI> uriPredicate = uri -> uri.getHost().contains(host);
            ((HasAuthentication) browser.getWebDriver())
                    .register(uriPredicate, UsernameAndPassword.of(USERNAME, PASSWORD));
        });

        builder.setMaximumStates(3);
        builder.setBrowserConfig(new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_HEADLESS, 1));
        CrawlSession session = new CrawljaxRunner(builder.build()).call();

        assertThat(session.getStateFlowGraph(), hasStates(3));
    }

    @After
    public void shutDown() throws Exception {
        server.stop();
    }
}

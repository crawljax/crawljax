package com.crawljax.core.plugin;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.html.dom.HTMLAnchorElementImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.google.common.collect.Maps;

/**
 * Test cases to test the running and correct functioning of the plugins. Used to address issue #26
 */
@Category(BrowserTest.class)
@Ignore("Temporary ignored. Will be fixed in a different branch.")
public class PluginsTest {

	private static final Logger LOG = LoggerFactory.getLogger(PluginsTest.class);
	private static CrawljaxController controller;
	private static CrawljaxConfiguration config;

	private static Map<Class<? extends Plugin>, Long> pluginTimes = Maps.newHashMap();

	/**
	 * Register the time the given plugin type is run for the first time.
	 * 
	 * @param p
	 *            the plugin type
	 */
	private static void registerPlugin(Class<? extends Plugin> p) {

		if (pluginTimes.get(p) == null) {
			LOG.debug("Register: " + p);
			pluginTimes.put(p, System.currentTimeMillis());
		}
	}

	private static void checkCrawlSession(CrawlSession session) {
		assertNotNull(session);
		assertNotNull(session.getBrowser());
		assertNotNull(session.getCrawljaxConfiguration());
		assertNotNull(session.getCrawlPaths());
		assertNotNull(session.getCurrentState());
		assertNotNull(session.getCurrentCrawlPath());
		assertNotNull(session.getInitialState());
		assertNotNull(session.getStateFlowGraph());
	}

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site/crawler");

	@BeforeClass
	public static void setup() throws ConfigurationException {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder();

		builder.crawlRules().clickDefaultElements();

		/**
		 * Add a sample Invariant for testing the OnInvariantViolation plugin
		 */
		builder.crawlRules().addInvariant("Never contain Final state S8",
		        new NotRegexCondition("Final state S2"));

		/**
		 * Add a empty proxy from running the ProxyConfigurationPlugin
		 */
		// config.setProxyConfiguration(new ProxyConfiguration());

		builder.addPlugin(new OnNewStatePlugin() {
			@Override
			public void onNewState(CrawlSession session) {
				registerPlugin(OnNewStatePlugin.class);
				checkCrawlSession(session);
				StateVertex cs = session.getCurrentState();
				if (!cs.getName().equals("index")) {
					assertTrue("currentState and indexState are never the same",
					        !cs.equals(session.getInitialState()));
				}
			}
		});

		builder.addPlugin(new GuidedCrawlingPlugin() {

			@Override
			public void guidedCrawling(StateVertex currentState, CrawljaxController controller,
			        CrawlSession session, List<Eventable> exactEventPaths,
			        StateMachine stateMachine) {
				registerPlugin(GuidedCrawlingPlugin.class);
				checkCrawlSession(session);
				assertTrue("exactEventPaths is the same as the session path", session
				        .getCurrentCrawlPath().equals(exactEventPaths));
			}
		});

		builder.addPlugin(new OnBrowserCreatedPlugin() {

			@Override
			public void onBrowserCreated(EmbeddedBrowser newBrowser) {
				registerPlugin(OnBrowserCreatedPlugin.class);
				assertNotNull(newBrowser);
			}
		});

		builder.addPlugin(new OnInvariantViolationPlugin() {

			@Override
			public void onInvariantViolation(Invariant invariant, CrawlSession session) {
				registerPlugin(OnInvariantViolationPlugin.class);
				checkCrawlSession(session);
				assertNotNull(invariant);

			}
		});

		builder.addPlugin(new OnUrlLoadPlugin() {

			@Override
			public void onUrlLoad(EmbeddedBrowser browser) {
				registerPlugin(OnUrlLoadPlugin.class);
				assertNotNull(browser);
			}
		});

		builder.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				registerPlugin(PostCrawlingPlugin.class);
				checkCrawlSession(session);
			}
		});

		builder.addPlugin(new PreCrawlingPlugin() {

			@Override
			public void preCrawling(EmbeddedBrowser browser) {
				registerPlugin(PreCrawlingPlugin.class);
				assertNotNull(browser);
			}
		});

		builder.addPlugin(new PreStateCrawlingPlugin() {

			@Override
			public void preStateCrawling(CrawlSession session,
			        List<CandidateElement> candidateElements) {
				registerPlugin(PreStateCrawlingPlugin.class);
				assertNotNull(candidateElements);
				checkCrawlSession(session);
				assertTrue("There are always more than 0 candidates",
				        candidateElements.size() > 0);

				if (session.getCurrentState().getName().equals("state8")) {
					/**
					 * Add to miss invocation for the OnFireEventFaild plugin.
					 */
					// This is a bit ugly; but hey it works, and is checked above..
					CandidateElement candidate = candidateElements.get(0);
					HTMLAnchorElementImpl impl = (HTMLAnchorElementImpl) candidate.getElement();
					impl.setName("fail");
					impl.setId("eventually");
					impl.setHref("will");
					impl.setTextContent("This");
					candidate.getIdentification().setValue("/HTML[1]/BODY[1]/FAILED[1]/A[1]");
				}
			}
		});

		builder.addPlugin(new OnRevisitStatePlugin() {

			@Override
			public void onRevisitState(CrawlSession session, StateVertex currentState) {
				registerPlugin(OnRevisitStatePlugin.class);
				checkCrawlSession(session);
				assertNotNull(currentState);
			}
		});

		config = builder.build();
		controller = new CrawljaxController(config);
	}

	@Test
	public void testPluginsExecution() throws ConfigurationException, CrawljaxException {
		try {

			controller.run();
			assertThat(config.getPlugins(), hasSize(pluginTimes.size()));

			// Can not test the relation OnBrowserCreatedPlugin vs. PreCrawlingPlugin
			// assertTrue(pluginTimes.get(OnBrowserCreatedPlugin.class)
			// == pluginTimes.get(PreCrawlingPlugin.class));
			assertOrder(PreCrawlingPlugin.class, OnUrlLoadPlugin.class);
			assertOrder(OnUrlLoadPlugin.class, OnNewStatePlugin.class);
			assertOrder(OnNewStatePlugin.class, PreStateCrawlingPlugin.class);
			assertOrder(PreStateCrawlingPlugin.class, GuidedCrawlingPlugin.class);
			assertOrder(GuidedCrawlingPlugin.class, OnRevisitStatePlugin.class);
			assertOrder(OnRevisitStatePlugin.class, OnInvariantViolationPlugin.class);

		} finally {
			controller.terminate(true);
		}
	}

	public void assertOrder(Class<? extends Plugin> first, Class<? extends Plugin> last) {
		assertThat(first.getSimpleName() + " should come before " + last.getSimpleName(),
		        pluginTimes.get(first), lessThan(pluginTimes
		                .get(last)));
	}

	@AfterClass
	public static void cleanUp() {
		CrawljaxPluginsUtil.loadPlugins(null);
	}

}

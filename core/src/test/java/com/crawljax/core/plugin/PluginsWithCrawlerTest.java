package com.crawljax.core.plugin;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.html.dom.HTMLAnchorElementImpl;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.google.common.collect.ImmutableSet;

/**
 * Test cases to test the running and correct functioning of the plugins. Used to address issue #26
 */
@Category(BrowserTest.class)
@Ignore("Will be fixed in other branch")
public class PluginsWithCrawlerTest {

	private static CrawljaxController controller;
	private static CrawljaxConfiguration config;
	private static String listAsString;

	private static List<Class<? extends Plugin>> plugins = new BlockingArrayQueue<>();

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

		builder.addPlugin(new ProxyServerPlugin() {

			@Override
			public void proxyServer(ProxyConfiguration config) {
				plugins.add(ProxyServerPlugin.class);
			}
		});

		builder.addPlugin(new OnNewStatePlugin() {
			@Override
			public void onNewState(CrawlSession session) {
				plugins.add(OnNewStatePlugin.class);
				checkCrawlSession(session);
				StateVertex cs = session.getCurrentState();
				if (!cs.getName().equals("index")) {
					assertTrue("currentState and indexState are never the same",
					        !cs.equals(session.getInitialState()));
				}
			}
		});

		builder.addPlugin(new DomChangeNotifierPlugin() {

			@Override
			public boolean isDomChanged(String domBefore, Eventable e, String domAfter,
			        EmbeddedBrowser browser) {

				plugins.add(DomChangeNotifierPlugin.class);
				return !domAfter.equals(domBefore);

			}

		});

		builder.addPlugin(new OnBrowserCreatedPlugin() {

			@Override
			public void onBrowserCreated(EmbeddedBrowser newBrowser) {
				plugins.add(OnBrowserCreatedPlugin.class);
				assertNotNull(newBrowser);
			}
		});

		builder.addPlugin(new OnInvariantViolationPlugin() {

			@Override
			public void onInvariantViolation(Invariant invariant, CrawlSession session) {
				plugins.add(OnInvariantViolationPlugin.class);
				checkCrawlSession(session);
				assertNotNull(invariant);

			}
		});

		builder.addPlugin(new OnUrlLoadPlugin() {

			@Override
			public void onUrlLoad(EmbeddedBrowser browser) {
				plugins.add(OnUrlLoadPlugin.class);
				assertNotNull(browser);
			}
		});

		builder.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session) {
				plugins.add(PostCrawlingPlugin.class);
				checkCrawlSession(session);
			}
		});

		builder.addPlugin(new PreCrawlingPlugin() {

			@Override
			public void preCrawling(EmbeddedBrowser browser) {
				plugins.add(PreCrawlingPlugin.class);
				assertNotNull(browser);
			}
		});

		builder.addPlugin(new PreStateCrawlingPlugin() {

			@Override
			public void preStateCrawling(CrawlSession session,
			        List<CandidateElement> candidateElements) {
				plugins.add(PreStateCrawlingPlugin.class);
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
				plugins.add(OnRevisitStatePlugin.class);
				checkCrawlSession(session);
				assertNotNull(currentState);
			}
		});

		config = builder.build();
		controller = new CrawljaxController(config);
		controller.run();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < plugins.size(); i++) {
			Class<? extends Plugin> plugin = plugins.get(i);
			sb.append('\n').append(i).append(' ').append(plugin.getSimpleName());
		}
		listAsString = sb.toString();
	}

	@Test
	public void whenCrawlStartsInitialPluginsAreRun() {
		assertThat(plugins.get(0), typeCompatibleWith(ProxyServerPlugin.class));
		assertThat(plugins.get(1), typeCompatibleWith(PreCrawlingPlugin.class));
		assertThat(plugins.get(2), typeCompatibleWith(OnBrowserCreatedPlugin.class));
		assertThat(plugins.get(3), typeCompatibleWith(OnUrlLoadPlugin.class));
	}

	@Test
	public void whenCrawlFinishesTheLastPluginIsTheOverviewPlugin() {
		assertThat(plugins.get(plugins.size() - 1), typeCompatibleWith(PostCrawlingPlugin.class));
	}

	@Test
	public void verifyOnUrlLoadFollowers() {
		pluginsIsFollowedBy(OnUrlLoadPlugin.class, ImmutableSet.of(
		        OnInvariantViolationPlugin.class, OnNewStatePlugin.class,
		        DomChangeNotifierPlugin.class, OnRevisitStatePlugin.class,
		        PostCrawlingPlugin.class));
	}

	public void pluginsIsFollowedBy(Class<? extends Plugin> suspect,
	        Iterable<Class<? extends Plugin>> followedBy) {
		for (int index : indexesOf(suspect)) {
			Class<? extends Plugin> follower = plugins.get(index + 1);
			assertThat(suspect + " @index=" + index + " was followed by " + follower
			        + listAsString, followedBy, IsCollectionContaining.hasItem(follower));
		}
	}

	private Set<Integer> indexesOf(Class<? extends Plugin> clasz) {
		Set<Integer> indexes = new HashSet<>();
		for (int i = 0; i < plugins.size(); i++) {
			if (plugins.get(i).isAssignableFrom(clasz)) {
				indexes.add(i);
			}
		}
		return indexes;
	}

	@Test
	public void verifyOnNewStateFollowers() {
		pluginsIsFollowedBy(OnNewStatePlugin.class,
		        ImmutableSet.<Class<? extends Plugin>> of(PreStateCrawlingPlugin.class));
	}

	@Test
	public void verifyPreStateCrawlingFollowers() {
		pluginsIsFollowedBy(PreStateCrawlingPlugin.class,
		        ImmutableSet.of(DomChangeNotifierPlugin.class, OnFireEventFailedPlugin.class));
	}

	@Test
	public void onRevisitStatesFollowers() {
		pluginsIsFollowedBy(OnRevisitStatePlugin.class, ImmutableSet.of(
		        OnFireEventFailedPlugin.class, DomChangeNotifierPlugin.class,
		        OnInvariantViolationPlugin.class, OnNewStatePlugin.class));
	}

	@Test
	public void verifyOnDomChangedFollowers() {
		pluginsIsFollowedBy(DomChangeNotifierPlugin.class, ImmutableSet.of(
		        OnFireEventFailedPlugin.class, DomChangeNotifierPlugin.class,
		        OnInvariantViolationPlugin.class, OnNewStatePlugin.class,
		        PostCrawlingPlugin.class));
	}

	@Test
	public void startAndEndPluginsAreOnlyRunOnce() {
		assertThat(orrurencesOf(ProxyServerPlugin.class), is(1));
		assertThat(orrurencesOf(PreCrawlingPlugin.class), is(1));
		assertThat(orrurencesOf(PostCrawlingPlugin.class), is(1));
	}

	@Test
	public void domStatesChangesAreEqualToNumberOfStatesAfterIndex() {
		int numberOfStates = controller.getSession().getStateFlowGraph().getAllStates().size();
		int newStatesAfterIndexPage = numberOfStates - 1;
		assertThat(orrurencesOf(DomChangeNotifierPlugin.class), is(newStatesAfterIndexPage));
	}

	@Test
	public void newStatePluginCallsAreEqualToNumberOfStates() {
		int numberOfStates = controller.getSession().getStateFlowGraph().getAllStates().size();
		assertThat(orrurencesOf(OnNewStatePlugin.class), is(numberOfStates));
	}

	private int orrurencesOf(Class<? extends Plugin> clasz) {
		int count = 0;
		for (Class<? extends Plugin> plugin : plugins) {
			if (plugin.isAssignableFrom(clasz)) {
				count++;
			}
		}
		return count;
	}
}

package com.crawljax.core.plugin;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.condition.NotRegexCondition;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.google.common.collect.ImmutableSet;
import org.apache.html.dom.HTMLAnchorElementImpl;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ErrorCollector;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.junit.Assert.*;

/**
 * Test cases to test the running and correct functioning of the plugins. Used to address issue #26
 */
@Category(BrowserTest.class)
public class PluginsWithCrawlerTest {

	private static String listAsString;

	private static List<Class<? extends Plugin>> plugins = new BlockingArrayQueue<>();

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site");

	@ClassRule
	public static final ErrorCollector ERRORS = new ErrorCollector();

	private static CrawlSession session;

	@BeforeClass
	public static void setup() {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("/crawler/");

		builder.crawlRules().clickDefaultElements();

		/*
		 * Add a sample Invariant for testing the OnInvariantViolation plugin
		 */
		builder.crawlRules().addInvariant("Never contain Final state S8",
				new NotRegexCondition("Final state S2"));

		builder.addPlugin((PreCrawlingPlugin) config -> plugins.add(PreCrawlingPlugin.class));

		builder.addPlugin((OnNewStatePlugin) (context, state) -> {
			plugins.add(OnNewStatePlugin.class);

			if (!state.getName().equals("index")) {
				assertTrue("currentState and indexState are never the same",
						!state.equals(context.getSession().getInitialState()));
			}
		});

		builder.addPlugin((OnBrowserCreatedPlugin) newBrowser -> {
			plugins.add(OnBrowserCreatedPlugin.class);
			assertNotNull(newBrowser);
		});

		builder.addPlugin((OnInvariantViolationPlugin) (invariant, context) -> {
			plugins.add(OnInvariantViolationPlugin.class);

			assertNotNull(invariant);

		});

		builder.addPlugin((OnUrlLoadPlugin) browser -> {
			plugins.add(OnUrlLoadPlugin.class);
			assertNotNull(browser);
		});

		builder.addPlugin(
				(PostCrawlingPlugin) (session, status) -> plugins.add(PostCrawlingPlugin.class));

		builder.addPlugin((PreStateCrawlingPlugin) (session, candidateElements, state) -> {
			plugins.add(PreStateCrawlingPlugin.class);
			try {
				assertNotNull(candidateElements);

			} catch (AssertionError e) {
				ERRORS.addError(e);
			}

			if (state.getName().equals("state8")) {
				/*
				 * Add to miss invocation for the OnFireEventFailed plugin.
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
		});

		builder.addPlugin((OnRevisitStatePlugin) (session, currentState) -> {
			plugins.add(OnRevisitStatePlugin.class);

			assertNotNull(currentState);
		});

		builder.setBrowserConfig(new BrowserConfiguration(BrowserProvider.getBrowserType()));

		CrawljaxConfiguration config = builder.build();

		CrawljaxRunner controller = new CrawljaxRunner(config);
		session = controller.call();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < plugins.size(); i++) {
			Class<? extends Plugin> plugin = plugins.get(i);
			sb.append('\n').append(i).append(' ').append(plugin.getSimpleName());
		}
		listAsString = sb.toString();
	}

	@Test
	public void whenCrawlStartsInitialPluginsAreRun() {
		assertThat(plugins.get(0), typeCompatibleWith(PreCrawlingPlugin.class));
		assertThat(plugins.get(1), typeCompatibleWith(OnBrowserCreatedPlugin.class));
		assertThat(plugins.get(2), typeCompatibleWith(OnUrlLoadPlugin.class));
	}

	@Test
	public void whenCrawlFinishesTheLastPluginIsTheOverviewPlugin() {
		assertThat(plugins.get(plugins.size() - 1), typeCompatibleWith(PostCrawlingPlugin.class));
	}

	@Test
	public void verifyOnUrlLoadFollowers() {
		afterFirstPluginsIsFollowedBy(OnUrlLoadPlugin.class, ImmutableSet.of(
				OnInvariantViolationPlugin.class, OnNewStatePlugin.class, OnRevisitStatePlugin.class,
				PostCrawlingPlugin.class));
	}

	private void afterFirstPluginsIsFollowedBy(Class<OnUrlLoadPlugin> suspect,
			Iterable<Class<? extends Plugin>> followedBy) {
		List<Integer> indexes = indexesOf(suspect);
		if (indexes.size() > 0) {
			indexes.remove(0);
		}
		for (int index : indexes) {
			Class<? extends Plugin> follower = plugins.get(index + 1);
			assertThat(suspect + " @index=" + index + " was followed by " + follower
					+ listAsString, followedBy, IsCollectionContaining.hasItem(follower));
		}
	}

	public void pluginsIsFollowedBy(Class<? extends Plugin> suspect,
			Iterable<Class<? extends Plugin>> followedBy) {
		for (int index : indexesOf(suspect)) {
			Class<? extends Plugin> follower = plugins.get(index + 1);
			assertThat(suspect + " @index=" + index + " was followed by " + follower
					+ listAsString, followedBy, IsCollectionContaining.hasItem(follower));
		}
	}

	private List<Integer> indexesOf(Class<? extends Plugin> clazz) {
		List<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < plugins.size(); i++) {
			if (plugins.get(i).isAssignableFrom(clazz)) {
				indexes.add(i);
			}
		}
		return indexes;
	}

	@Test
	public void verifyOnNewStateFollowers() {
		pluginsIsFollowedBy(OnNewStatePlugin.class,
				ImmutableSet.of(PreStateCrawlingPlugin.class, OnUrlLoadPlugin.class,
						PostCrawlingPlugin.class));
	}

	@Test
	public void verifyPreStateCrawlingFollowers() {
		pluginsIsFollowedBy(PreStateCrawlingPlugin.class,
				ImmutableSet.of(OnFireEventFailedPlugin.class,
						OnInvariantViolationPlugin.class, OnNewStatePlugin.class,
						OnUrlLoadPlugin.class));
	}

	@Test
	public void onRevisitStatesFollowers() {
		pluginsIsFollowedBy(OnRevisitStatePlugin.class, ImmutableSet.of(
				OnFireEventFailedPlugin.class,
				OnInvariantViolationPlugin.class, OnNewStatePlugin.class));
	}

	@Test
	public void startAndEndPluginsAreOnlyRunOnce() {
		// assertThat(occurrencesOf(ProxyServerPlugin.class), is(1));
		assertThat(occurrencesOf(PreCrawlingPlugin.class), is(1));
		assertThat(occurrencesOf(PostCrawlingPlugin.class), is(1));
	}

	@Test
	public void newStatePluginCallsAreEqualToNumberOfStates() {
		int numberOfStates = session.getStateFlowGraph().getAllStates().size();
		assertThat(occurrencesOf(OnNewStatePlugin.class), is(numberOfStates));
	}

	private int occurrencesOf(Class<? extends Plugin> clazz) {
		int count = 0;
		for (Class<? extends Plugin> plugin : plugins) {
			if (plugin.isAssignableFrom(clazz)) {
				count++;
			}
		}
		return count;
	}
}

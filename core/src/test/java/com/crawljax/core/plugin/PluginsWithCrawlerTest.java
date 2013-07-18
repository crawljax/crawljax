package com.crawljax.core.plugin;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.object.IsCompatibleType.typeCompatibleWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.html.dom.HTMLAnchorElementImpl;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ErrorCollector;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Test cases to test the running and correct functioning of the plugins. Used to address issue #26
 */
@Category(BrowserTest.class)
public class PluginsWithCrawlerTest {

	private static CrawljaxRunner controller;
	private static CrawljaxConfiguration config;
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

		/**
		 * Add a sample Invariant for testing the OnInvariantViolation plugin
		 */
		builder.crawlRules().addInvariant("Never contain Final state S8",
		        new NotRegexCondition("Final state S2"));

		builder.addPlugin(new PreCrawlingPlugin() {

			@Override
			public void preCrawling(CrawljaxConfiguration config) {
				plugins.add(PreCrawlingPlugin.class);

			}
		});

		builder.addPlugin(new OnNewStatePlugin() {
			@Override
			public void onNewState(CrawlerContext context, StateVertex state) {
				plugins.add(OnNewStatePlugin.class);

				if (!state.getName().equals("index")) {
					assertTrue("currentState and indexState are never the same",
					        !state.equals(context.getSession().getInitialState()));
				}
			}
		});

		builder.addPlugin(new DomChangeNotifierPlugin() {

			@Override
			public boolean isDomChanged(CrawlerContext context, String domBefore, Eventable e,
			        String domAfter) {

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
			public void onInvariantViolation(Invariant invariant, CrawlerContext context) {
				plugins.add(OnInvariantViolationPlugin.class);

				assertNotNull(invariant);

			}
		});

		builder.addPlugin(new OnUrlLoadPlugin() {

			@Override
			public void onUrlLoad(CrawlerContext browser) {
				plugins.add(OnUrlLoadPlugin.class);
				assertNotNull(browser);
			}
		});

		builder.addPlugin(new PostCrawlingPlugin() {

			@Override
			public void postCrawling(CrawlSession session, ExitStatus status) {
				plugins.add(PostCrawlingPlugin.class);

			}
		});

		builder.addPlugin(new PreStateCrawlingPlugin() {

			@Override
			public void preStateCrawling(CrawlerContext session,
			        ImmutableList<CandidateElement> candidateElements, StateVertex state) {
				plugins.add(PreStateCrawlingPlugin.class);
				try {
					assertNotNull(candidateElements);

				} catch (AssertionError e) {
					ERRORS.addError(e);
				}

				if (state.getName().equals("state8")) {
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
			public void onRevisitState(CrawlerContext session, StateVertex currentState) {
				plugins.add(OnRevisitStatePlugin.class);

				assertNotNull(currentState);
			}
		});

		config = builder.build();
		controller = new CrawljaxRunner(config);
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
		        OnInvariantViolationPlugin.class, OnNewStatePlugin.class,
		        DomChangeNotifierPlugin.class, OnRevisitStatePlugin.class,
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

	private List<Integer> indexesOf(Class<? extends Plugin> clasz) {
		List<Integer> indexes = new ArrayList<>();
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
		        ImmutableSet.of(PreStateCrawlingPlugin.class, OnUrlLoadPlugin.class,
		                PostCrawlingPlugin.class));
	}

	@Test
	public void verifyPreStateCrawlingFollowers() {
		pluginsIsFollowedBy(PreStateCrawlingPlugin.class,
		        ImmutableSet.of(DomChangeNotifierPlugin.class, OnFireEventFailedPlugin.class,
		                OnInvariantViolationPlugin.class, OnNewStatePlugin.class,
		                OnUrlLoadPlugin.class));
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
		// assertThat(orrurencesOf(ProxyServerPlugin.class), is(1));
		assertThat(orrurencesOf(PreCrawlingPlugin.class), is(1));
		assertThat(orrurencesOf(PostCrawlingPlugin.class), is(1));
	}

	@Test
	public void domStatesChangesAreEqualToNumberOfStatesAfterIndex() {
		int numberOfStates = session.getStateFlowGraph().getAllStates().size();
		int newStatesAfterIndexPage = numberOfStates - 1;
		assertThat(orrurencesOf(DomChangeNotifierPlugin.class), is(newStatesAfterIndexPage));
	}

	@Test
	public void newStatePluginCallsAreEqualToNumberOfStates() {
		int numberOfStates = session.getStateFlowGraph().getAllStates().size();
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

package com.crawljax.core.plugin;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.metrics.MetricsModule;
import com.crawljax.test.BrowserTest;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map.Entry;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test cases to test the running and correct functioning of the plugins. Used to address issue #26
 */
@Category(BrowserTest.class)
@RunWith(MockitoJUnitRunner.class)
public class PluginsTest {

	private Plugins plugins;

	@Mock
	private OnBrowserCreatedPlugin browserCreatedPlugin;

	@Mock
	private OnFireEventFailedPlugin fireEventFailedPlugin;

	@Mock
	private OnInvariantViolationPlugin invariantViolationPlugin;

	@Mock
	private OnNewStatePlugin newStatePlugin;

	@Mock
	private OnRevisitStatePlugin onRevisitStatePlugin;

	@Mock
	private OnUrlLoadPlugin urlLoadPlugin;

	@Mock
	private PostCrawlingPlugin postCrawlingPlugin;

	@Mock
	private PreStateCrawlingPlugin preStatePlugin;

	@Mock
	private CrawlerContext context;

	@Mock
	private CrawlSession session;

	@Mock
	private StateVertex vertex;

	private MetricRegistry registry;

	@Before
	public void setup() {
		registry = new MetricRegistry();
		CrawljaxConfiguration config = CrawljaxConfiguration.builderFor("http://localhost")
				.addPlugin(browserCreatedPlugin, fireEventFailedPlugin,
						invariantViolationPlugin, newStatePlugin, onRevisitStatePlugin,
						urlLoadPlugin, postCrawlingPlugin, preStatePlugin)
				.setBrowserConfig(new BrowserConfiguration(BrowserProvider.getBrowserType()))
				.build();
		plugins = new Plugins(config, registry);
	}

	@Test
	public void testAllCountersRegistered() {
		assertThat(registry.getCounters().size(), is(Plugins.KNOWN_PLUGINS.size()));
	}

	@Test
	public void postCrawlPluginIsCalled() {
		plugins.runPostCrawlingPlugins(session, ExitStatus.EXHAUSTED);
		verify(postCrawlingPlugin).postCrawling(session, ExitStatus.EXHAUSTED);
		assertThat(counterFor(PostCrawlingPlugin.class), is(1));
	}

	private int counterFor(Class<? extends Plugin> plugin) {
		for (Entry<String, Counter> counter : registry.getCounters().entrySet()) {
			if (counter.getKey().contains(plugin.getSimpleName())
					&& counter.getKey().endsWith("invocations")) {
				return (int) counter.getValue().getCount();
			}
		}

		Assert.fail("No counter found for " + plugin.getClass().getSimpleName());
		return -1;
	}

	@Test
	public void urlLoadIsCalled() {
		plugins.runOnUrlLoadPlugins(context);
		verify(urlLoadPlugin).onUrlLoad(context);
		assertThat(counterFor(OnUrlLoadPlugin.class), is(1));
	}

	@Test
	public void revisitStatePluginIsCalled() {
		StateVertex currentState = mock(StateVertex.class);
		plugins.runOnRevisitStatePlugins(context, currentState);
		verify(onRevisitStatePlugin).onRevisitState(context, currentState);
		assertThat(counterFor(OnRevisitStatePlugin.class), is(1));
	}

	@Test
	public void newStatePluginIsCalled() {
		plugins.runOnNewStatePlugins(context, vertex);
		verify(newStatePlugin).onNewState(context, vertex);
		assertThat(counterFor(OnNewStatePlugin.class), is(1));
	}

	@Test
	public void invariantViolatedIsCalled() {
		Invariant invariant = mock(Invariant.class);
		plugins.runOnInvariantViolationPlugins(invariant, context);
		verify(invariantViolationPlugin).onInvariantViolation(invariant, context);
		assertThat(counterFor(OnInvariantViolationPlugin.class), is(1));
	}

	@Test
	public void browserCreatedIsCalled() {
		EmbeddedBrowser newBrowser = mock(EmbeddedBrowser.class);
		plugins.runOnBrowserCreatedPlugins(newBrowser);
		verify(browserCreatedPlugin).onBrowserCreated(newBrowser);

		assertThat(counterFor(OnBrowserCreatedPlugin.class), is(1));
	}

	@Test
	public void verifyPreCrawlPluginIsCalled() {
		ImmutableList<CandidateElement> candidateElements = ImmutableList.of();
		plugins.runPreStateCrawlingPlugins(context, candidateElements, vertex);
		verify(preStatePlugin).preStateCrawling(context, candidateElements, vertex);
		assertThat(counterFor(PreStateCrawlingPlugin.class), is(1));
	}

	@Test
	public void fireEventFailedIsCalled() {
		List<Eventable> path = ImmutableList.of();
		Eventable eventable = mock(Eventable.class);
		plugins.runOnFireEventFailedPlugins(context, eventable, path);
		verify(fireEventFailedPlugin).onFireEventFailed(context, eventable, path);
		assertThat(counterFor(OnFireEventFailedPlugin.class), is(1));
	}
}

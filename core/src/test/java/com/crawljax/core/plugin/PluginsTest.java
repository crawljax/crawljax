package com.crawljax.core.plugin;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BrowserTest;
import com.google.common.collect.ImmutableList;

/**
 * Test cases to test the running and correct functioning of the plugins. Used to address issue #26
 */
@Category(BrowserTest.class)
@Ignore("Temporary ignored. Will be fixed in a different branch.")
@RunWith(MockitoJUnitRunner.class)
public class PluginsTest {

	private Plugins plugins;

	@Mock
	private DomChangeNotifierPlugin domChange;

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
	private PreCrawlingPlugin preCrawlingPlugin;

	@Mock
	private PreStateCrawlingPlugin prestatePlugin;

	@Mock
	private ProxyServerPlugin proxyServerPlugin;

	@Mock
	private CrawlSession session;

	@Mock
	private StateVertex vertex;

	@Before
	public void setup() {
		plugins =
		        new Plugins(ImmutableList.of(domChange, browserCreatedPlugin,
		                fireEventFailedPlugin, invariantViolationPlugin, newStatePlugin,
		                onRevisitStatePlugin,
		                urlLoadPlugin, postCrawlingPlugin, preCrawlingPlugin, prestatePlugin,
		                proxyServerPlugin));
	}

	@Test
	public void proxyCallisCalled() throws Exception {
		ProxyConfiguration config = ProxyConfiguration.noProxy();
		plugins.runProxyServerPlugins(config);
		verify(proxyServerPlugin).proxyServer(config);
	}

	@Test
	public void preStateCrawlisCalled() throws Exception {
		ImmutableList<CandidateElement> candidateElements = ImmutableList.of();
		plugins.runPreStateCrawlingPlugins(session, candidateElements, vertex);
		verify(prestatePlugin).preStateCrawling(session, candidateElements, vertex);
	}

	@Test
	public void postCrawlPluginIsCalled() {
		plugins.runPostCrawlingPlugins(session);
		verify(postCrawlingPlugin).postCrawling(session);
	}

	@Test
	public void urlLoadisCalled() throws Exception {
		EmbeddedBrowser browser = mock(EmbeddedBrowser.class);
		plugins.runOnUrlLoadPlugins(browser);
	}

	@Test
	public void revisitStatePluginIsCalled() throws Exception {
		StateVertex currentState = mock(StateVertex.class);
		plugins.runOnRevisitStatePlugins(session, currentState);
		verify(onRevisitStatePlugin).onRevisitState(session, currentState);
	}

	@Test
	public void newStatePluginIsCalled() throws Exception {
		plugins.runOnNewStatePlugins(session, vertex);
		verify(newStatePlugin).onNewState(session, vertex);
	}

	@Test
	public void invariantViolatedIsCalled() throws Exception {
		EmbeddedBrowser browser = Mockito.mock(EmbeddedBrowser.class);
		Invariant invariant = mock(Invariant.class);
		plugins.runOnInvriantViolationPlugins(invariant, session, browser);
		verify(invariantViolationPlugin).onInvariantViolation(invariant, session, browser);
	}

	@Test
	public void domChangeNotifierIsCalled() {
		StateVertex stateBefore = mock(StateVertex.class);
		Eventable eventable = mock(Eventable.class);
		StateVertex stateAfter = mock(StateVertex.class);
		EmbeddedBrowser browser = mock(EmbeddedBrowser.class);
		String oldDom = "old";
		String newDom = "new";
		when(stateBefore.getDom()).thenReturn(oldDom);
		when(stateAfter.getDom()).thenReturn(newDom);

		plugins.runDomChangeNotifierPlugins(stateBefore, eventable, stateAfter, browser);
		verify(domChange).isDomChanged(oldDom, eventable, newDom, browser);
	}

	@Test(expected = IllegalArgumentException.class)
	public void onlyOneDomChangePluginCanBeAdded() {
		new Plugins(ImmutableList.of(domChange, domChange));
	}

	@Test
	public void browserCreatedIsCalled() {
		EmbeddedBrowser newBrowser = mock(EmbeddedBrowser.class);
		plugins.runOnBrowserCreatedPlugins(newBrowser);
		verify(browserCreatedPlugin).onBrowserCreated(newBrowser);
	}

	@Test
	public void verifyPreCrawlPluginIsCalled() {
		ImmutableList<CandidateElement> candidateElements = ImmutableList.of();
		plugins.runPreStateCrawlingPlugins(session, candidateElements, vertex);
		verify(prestatePlugin).preStateCrawling(session, candidateElements, vertex);
	}

	@Test
	public void fireEventFailedIsCalled() {
		List<Eventable> path = ImmutableList.of();
		Eventable eventable = mock(Eventable.class);
		plugins.runOnFireEventFailedPlugins(eventable, path);
		verify(fireEventFailedPlugin).onFireEventFailed(eventable, path);
	}

	@Test
	public void whenDomChangeErrorsTheDefaultIsUsed() {
		StateVertex stateBefore = mock(StateVertex.class);
		Eventable eventable = mock(Eventable.class);
		StateVertex stateAfter = mock(StateVertex.class);
		EmbeddedBrowser browser = mock(EmbeddedBrowser.class);
		String oldDom = "old";
		String newDom = "new";
		when(stateBefore.getDom()).thenReturn(oldDom);
		when(stateAfter.getDom()).thenReturn(newDom);

		when(domChange.isDomChanged(oldDom, eventable, newDom, browser)).thenThrow(
		        new RuntimeException("This is an expected excpetion. ignore"));
		assertThat(
		        plugins.runDomChangeNotifierPlugins(stateBefore, eventable, stateAfter, browser),
		        is(true));
	}
}

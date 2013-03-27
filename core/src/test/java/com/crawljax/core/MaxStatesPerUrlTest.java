package com.crawljax.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.crawljax.browser.BrowserPool;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification.How;
import com.google.common.collect.ImmutableList;

public class MaxStatesPerUrlTest {
	
	private StateMachine sm;
	private final int MAX_STATES_PER_URL = 1;

	private StateVertex index;
	private StateVertex state2;
	private StateVertex state3;
	private StateVertex state4;
	private StateFlowGraph sfg;
	
	@Mock
	private EmbeddedBrowser dummyBrowser;

	private final BrowserPool dummyPool = new BrowserPool(CrawljaxConfiguration.builderFor(
	        "http://localhost").build());

	/**
	 * Run before every test case.
	 */
	@Before
	public void setup() {		
		index = new StateVertex("www.ubc.ca", "index", "www.ubc.ca", "www.ubc.ca");
		state2 = new StateVertex("www.yahoo.com", "STATE2", "www.yahoo.com", "www.yahoo.com");
		state3 = new StateVertex("www.yahoo.com", "STATE3", "www.yahoo.com", "www.yahoo.com");
		state4 = new StateVertex("http://www.youtube.com", "STATE4", "http://www.youtube.com", "http://www.youtube.com");
		sfg = new StateFlowGraph(index);
		sm = new StateMachine(sfg, index, ImmutableList.<Invariant> of(), Plugins.noPlugins());
	}	

	@Test
	public void testMaxStatesPerUrl() {
		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.updateAndCheckIfClone(c, state2, dummyBrowser, new CrawlSession(dummyPool), MAX_STATES_PER_URL));
		sfg.addState(state2);
		// state2 and state3 are the same
		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertFalse(sm.updateAndCheckIfClone(c2, state3, dummyBrowser, new CrawlSession(dummyPool), MAX_STATES_PER_URL));
		
		Eventable c3 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.updateAndCheckIfClone(c3, state4, dummyBrowser, new CrawlSession(dummyPool), MAX_STATES_PER_URL));
				
		assertThat(sfg.getNumberOfStates(), is(3));
	}
}

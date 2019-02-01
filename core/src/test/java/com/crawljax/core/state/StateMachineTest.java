package com.crawljax.core.state;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnInvariantViolationPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification.How;
import com.crawljax.oraclecomparator.StateComparator;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineTest {
	private StateMachine sm;
	private final StateVertex index =
			new StateVertexImpl(StateVertex.INDEX_ID, "index", "<table><div>index</div></table>");

	@Mock
	private EmbeddedBrowser dummyBrowser;

	@Mock
	private StateComparator comparator;

	@Mock
	private CrawlSession session;

	@Mock
	private CrawlerContext context;

	@Mock
	private Plugins plugins;

	private static boolean hit = false;

	/**
	 * Run before every test case.
	 */
	@Before
	public void initStateMachine() {
		InMemoryStateFlowGraph sfg = newStateFlowGraph();
		sm = new StateMachine(sfg, ImmutableList.<Invariant>of(), plugins, comparator,
				new ArrayList<>());
	}

	@Test
	public void testInitOk() {
		assertNotNull(sm);
		assertNotNull(sm.getCurrentState());
		assertEquals(sm.getCurrentState(), index);
	}

	/**
	 * Test the Change State operation.
	 */
	@Test
	public void testChangeState() {
		StateVertex state2 = new StateVertexImpl(2, "state2", "<table><div>state2</div></table>");

		// Can not change index because not added.
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		// Add index.
		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.switchToStateAndCheckIfClone(c, state2, context));

		// Current index is the new index
		assertEquals(sm.getCurrentState(), state2);

		// Change back.
		assertTrue(sm.changeState(index));
		assertEquals(sm.getCurrentState(), index);
	}

	/**
	 * Test the Clone state behaviour.
	 */
	@Test
	public void testCloneState() {
		// state2.equals(state3)
		StateVertex state2 = new StateVertexImpl(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertexImpl(3, "state3", "<table><div>state2</div></table>");
		/*
		 * Can not change to state2 because not inserted yet.
		 */
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.switchToStateAndCheckIfClone(c, state2, context));

		// can not change to state2 because we are already in state2
		assertFalse(sm.changeState(state2));
		assertSame(sm.getCurrentState(), state2);

		// state2.equals(state3)
		assertEquals("state2 equals state3", state2, state3);

		// state2 != state3 because other objects.
		assertNotSame("state2 != state3", state2, state3);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla2"), EventType.click);

		// False because its CLONE!
		assertFalse(sm.switchToStateAndCheckIfClone(c2, state3, context));

		// state2.equals(state3)
		assertEquals("state2 equals state3", state2, state3);

		// state2 == sm.getCurrentState() because changed in update.
		assertSame("state2 == state3", state2, sm.getCurrentState());

	}

	/**
	 * Test the Rewind Operation.
	 */
	@Test
	public void testRewind() {
		// state2.equals(state3)
		StateVertex state2 = new StateVertexImpl(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertexImpl(3, "state3", "<table><div>state2</div></table>");
		StateVertex state4 = new StateVertexImpl(4, "state4", "<table><div>state4</div></table>");
		/*
		 * Can not change to state2 because not inserted yet.
		 */
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.switchToStateAndCheckIfClone(c, state2, context));

		// can not change to state2 because we are already in state2
		assertFalse(sm.changeState(state2));
		assertSame(sm.getCurrentState(), state2);

		// state2.equals(state3)
		assertEquals("state2 equals state3", state2, state3);

		// !state2.equals(state4)
		assertFalse("state2 not equals state4", state2.equals(state4));

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla2"), EventType.click);

		// False because its CLONE!
		assertFalse(sm.switchToStateAndCheckIfClone(c2, state3, context));

		Eventable c3 = new Eventable(new Identification(How.xpath, "/bla2"), EventType.click);

		// True because its not yet known
		assertTrue(sm.switchToStateAndCheckIfClone(c3, state4, context));

		sm.rewind();

		assertEquals("CurrentState == index", index, sm.getCurrentState());

		// Now we can go from index -> state2
		assertTrue(sm.changeState(state2));

		// Now we can go from state2 -> state2
		assertTrue(sm.changeState(state2));

		// Now we can go from state2 -> state4
		assertTrue(sm.changeState(state4));

		sm.rewind();

		assertEquals("CurrentState == index", index, sm.getCurrentState());

		// Now we can not go from index -> state4
		assertFalse(sm.changeState(state4));

	}

	/**
	 * Make sure Invariants are executed!
	 */
	@Test
	public void testInvariants() {
		// state2.equals(state3)
		StateVertex state2 = new StateVertexImpl(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertexImpl(3, "state3", "<table><div>state2</div></table>");

		hit = false;
		ImmutableList<Invariant> iList =
				ImmutableList.of(new Invariant("Test123", browser -> {
					hit = true;
					return false;
				}));
		InMemoryStateFlowGraph sfg = newStateFlowGraph();
		StateMachine smLocal =
				new StateMachine(sfg, iList, plugins, comparator, new ArrayList<>());

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(smLocal.switchToStateAndCheckIfClone(c, state2, context));

		// New State so hit must be true;
		assertTrue("Invariants are executed", hit);
		hit = false;
		assertFalse("Hit reset", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(smLocal.switchToStateAndCheckIfClone(c2, state3, context));
		// CLONE State so hit must be true;
		assertTrue("Invariants are executed", hit);
	}

	private InMemoryStateFlowGraph newStateFlowGraph() {
		InMemoryStateFlowGraph sfg =
				new InMemoryStateFlowGraph(new ExitNotifier(0), new DefaultStateVertexFactory());
		sfg.putIndex(index);
		return sfg;
	}

	/**
	 * Make sure On new State Plugin executed.
	 */
	@Test
	public void testOnNewStatePlugin() {
		hit = false;
		CrawljaxConfiguration config = CrawljaxConfiguration.builderFor("http://localhost")
				.addPlugin((OnNewStatePlugin) (context, state) -> hit = true).build();
		setStateMachineForConfig(config);

		// state2.equals(state3)
		StateVertex state2 = new StateVertexImpl(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertexImpl(3, "state3", "<table><div>state2</div></table>");

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(sm.switchToStateAndCheckIfClone(c, state2, context));

		// New State so hit must be true;
		assertTrue("Plugins are executed", hit);
		hit = false;
		assertFalse("Hit reset", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(sm.switchToStateAndCheckIfClone(c2, state3, context));

		// CLONE State so no plugin execution
		assertFalse("Plugins are NOT executed", hit);
	}

	private void setStateMachineForConfig(CrawljaxConfiguration config) {
		sm = new StateMachine(newStateFlowGraph(), config.getCrawlRules().getInvariants(),
				new Plugins(config, new MetricRegistry()), comparator, new ArrayList<>());
	}

	/**
	 * Make sure InvariantViolationPlugin executed.
	 */
	@Test
	public void testInvariantFailurePlugin() {
		hit = false;
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration
				.builderFor("http://localhost").addPlugin(
						(OnInvariantViolationPlugin) (invariant, context) -> hit = true);
		builder.crawlRules().addInvariant(new Invariant("Test123", browser -> false));
		setStateMachineForConfig(builder.build());

		// state2.equals(state3)
		StateVertex state2 = new StateVertexImpl(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertexImpl(3, "state3", "<table><div>state2</div></table>");

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(sm.switchToStateAndCheckIfClone(c, state2, context));

		// New State so hit must be true;
		assertTrue("InvariantViolationPlugin are executed", hit);
		hit = false;
		assertFalse("Hit reset", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(sm.switchToStateAndCheckIfClone(c2, state3, context));

		// New State so plugin execution
		assertTrue("InvariantViolationPlugin are executed", hit);
	}
}

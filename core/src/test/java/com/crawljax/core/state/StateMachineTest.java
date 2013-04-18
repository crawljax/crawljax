package com.crawljax.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnInvariantViolationPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification.How;
import com.crawljax.oraclecomparator.StateComparator;
import com.google.common.collect.ImmutableList;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineTest {
	private StateMachine sm;
	private final StateVertex index = new StateVertex(StateVertex.INDEX_ID, "index",
	        "<table><div>index</div></table>");

	@Mock
	private EmbeddedBrowser dummyBrowser;

	@Mock
	private StateComparator comparator;

	@Mock
	private CrawlSession session;

	private static boolean hit = false;

	/**
	 * Run before every test case.
	 */
	@Before
	public void initStateMachine() {
		StateFlowGraph sfg = newStateFlowGraph();
		sm = new StateMachine(sfg, ImmutableList.<Invariant> of(), Plugins.noPlugins(),
		        comparator);
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
		StateVertex state2 = new StateVertex(2, "state2", "<table><div>state2</div></table>");

		/**
		 * Can not change index because not added.
		 */
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		/**
		 * Add index.
		 */
		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.swithToStateAndCheckIfClone(c, state2, dummyBrowser, session));

		/**
		 * Name is correctly changed
		 */
		assertEquals("State name changed correctly", "state1", state2.getName());

		/**
		 * Current index is the new index
		 */
		assertEquals(sm.getCurrentState(), state2);

		/**
		 * Change back.
		 */
		assertTrue(sm.changeState(index));
		assertEquals(sm.getCurrentState(), index);
	}

	/**
	 * Test the Clone state behaviour.
	 */
	@Test
	public void testCloneState() {
		// state2.equals(state3)
		StateVertex state2 = new StateVertex(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex(3, "state3", "<table><div>state2</div></table>");
		/**
		 * Can not change to state2 because not inserted yet.
		 */
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.swithToStateAndCheckIfClone(c, state2, dummyBrowser, session));

		/**
		 * Name is correctly changed
		 */
		assertEquals("State name changed correctly", "state1", state2.getName());

		// can not change to state2 because we are already in state2
		assertFalse(sm.changeState(state2));
		assertSame(sm.getCurrentState(), state2);

		// state2.equals(state3)
		assertEquals("state2 equals state3", state2, state3);

		// state2 != state3 because other objects.
		assertNotSame("state2 != state3", state2, state3);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla2"), EventType.click);

		// False because its CLONE!
		assertFalse(sm.swithToStateAndCheckIfClone(c2, state3, dummyBrowser, session));

		// state2.equals(state3)
		assertEquals("state2 equals state3", state2, state3);

		// state2 == sm.getCurrentState() because changed in update.
		assertSame("state2 == state3", state2, sm.getCurrentState());

		/**
		 * Name is correctly changed
		 */
		assertEquals("State name changed correctly", "state1", sm.getCurrentState().getName());

	}

	/**
	 * Test the Rewind Operation.
	 */
	@Test
	public void testRewind() {
		// state2.equals(state3)
		StateVertex state2 = new StateVertex(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex(3, "state3", "<table><div>state2</div></table>");
		StateVertex state4 = new StateVertex(4, "state4", "<table><div>state4</div></table>");
		/**
		 * Can not change to state2 because not inserted yet.
		 */
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.swithToStateAndCheckIfClone(c, state2, dummyBrowser, session));

		/**
		 * Name is correctly changed
		 */
		assertEquals("State name changed correctly", "state1", state2.getName());

		// can not change to state2 because we are already in state2
		assertFalse(sm.changeState(state2));
		assertSame(sm.getCurrentState(), state2);

		// state2.equals(state3)
		assertEquals("state2 equals state3", state2, state3);

		// !state2.equals(state4)
		assertFalse("state2 not equals state4", state2.equals(state4));

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla2"), EventType.click);

		// False because its CLONE!
		assertFalse(sm.swithToStateAndCheckIfClone(c2, state3, dummyBrowser, session));

		Eventable c3 = new Eventable(new Identification(How.xpath, "/bla2"), EventType.click);

		// True because its not yet known
		assertTrue(sm
		        .swithToStateAndCheckIfClone(c3, state4, dummyBrowser, session));

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
		StateVertex state2 = new StateVertex(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex(3, "state3", "<table><div>state2</div></table>");

		hit = false;
		ImmutableList<Invariant> iList =
		        ImmutableList.of(new Invariant("Test123", new Condition() {

			        @Override
			        public NodeList getAffectedNodes() {
				        return null;
			        }

			        @Override
			        public boolean check(EmbeddedBrowser browser) {
				        hit = true;
				        return false;
			        }
		        }));
		StateFlowGraph sfg = newStateFlowGraph();
		StateMachine smLocal =
		        new StateMachine(sfg, iList, Plugins.noPlugins(), comparator);

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(smLocal.swithToStateAndCheckIfClone(c, state2, dummyBrowser, session));

		// New State so hit must be true;
		assertTrue("Invariants are exeucted", hit);
		hit = false;
		assertFalse("Hit reseted", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(smLocal.swithToStateAndCheckIfClone(c2, state3, dummyBrowser, session));
		// CLONE State so hit must be true;
		assertTrue("Invariants are exeucted", hit);
	}

	private StateFlowGraph newStateFlowGraph() {
		StateFlowGraph sfg = new StateFlowGraph();
		sfg.putIfAbsent(index, false);
		return sfg;
	}

	/**
	 * Make sure On new State Plugin executed.
	 * 
	 * @throws ConfigurationException
	 *             when failure configuring Properties
	 */
	@Test
	public void testOnNewStatePlugin() throws ConfigurationException {
		hit = false;
		CrawljaxConfiguration config = CrawljaxConfiguration.builderFor(
		        "http://localhost").addPlugin(new OnNewStatePlugin() {

			@Override
			public void onNewState(CrawlSession session, StateVertex state) {
				hit = true;
			}
		}).build();
		setStateMachineForConfig(config);

		// state2.equals(state3)
		StateVertex state2 = new StateVertex(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex(3, "state3", "<table><div>state2</div></table>");

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(sm.swithToStateAndCheckIfClone(c, state2, dummyBrowser, session));

		// New State so hit must be true;
		assertTrue("Plugins are exeucted", hit);
		hit = false;
		assertFalse("Hit reseted", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(sm.swithToStateAndCheckIfClone(c2, state3, dummyBrowser, session));

		// CLONE State so no plugin execution
		assertFalse("Plugins are NOT exeucted", hit);
	}

	private void setStateMachineForConfig(CrawljaxConfiguration config) {
		sm = new StateMachine(newStateFlowGraph(), config.getCrawlRules().getInvariants(),
		        config.getPlugins(), comparator);
	}

	/**
	 * Make sure InvariantViolationPlugin executed.
	 * 
	 * @throws ConfigurationException
	 *             when failure configuring Properties
	 */
	@Test
	public void testInvariantFailurePlugin() throws ConfigurationException {
		hit = false;
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(
		        "http://localhost").addPlugin(new OnInvariantViolationPlugin() {
			@Override
			public void onInvariantViolation(Invariant invariant, CrawlSession session,
			        EmbeddedBrowser browser) {
				hit = true;
			}
		});
		builder.crawlRules().addInvariant(new Invariant("Test123", new Condition() {

			@Override
			public NodeList getAffectedNodes() {
				return null;
			}

			@Override
			public boolean check(EmbeddedBrowser browser) {
				return false;
			}
		}));
		setStateMachineForConfig(builder.build());

		// state2.equals(state3)
		StateVertex state2 = new StateVertex(2, "state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex(3, "state3", "<table><div>state2</div></table>");

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(sm.swithToStateAndCheckIfClone(c, state2, dummyBrowser, session));

		// New State so hit must be true;
		assertTrue("InvariantViolationPlugin are exeucted", hit);
		hit = false;
		assertFalse("Hit reseted", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(sm.swithToStateAndCheckIfClone(c2, state3, dummyBrowser, session));

		// New State so plugin execution
		assertTrue("InvariantViolationPlugin are exeucted", hit);
	}
}

/**
 * Created Dec 20, 2007.
 */
package com.crawljax.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NodeList;

import com.crawljax.browser.BrowserPool;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.plugin.OnInvariantViolationPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification.How;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineTest {
	private StateMachine sm;
	private final StateVertex index = new StateVertex("index", "<table><div>index</div></table>");

	@Mock
	private EmbeddedBrowser dummyBrowser;

	private final BrowserPool dummyPool = new BrowserPool(new CrawljaxConfigurationReader(
	        new CrawljaxConfiguration()));

	private static boolean hit = false;

	/**
	 * Run before every test case.
	 */
	@Before
	public void initStateMachine() {
		StateFlowGraph sfg = new StateFlowGraph(index);
		sm = new StateMachine(sfg, index);
	}

	/**
	 * Init ok?
	 */
	@Test
	public void testStateMachine() {
		assertNotNull(sm);
		assertNotNull(sm.getCurrentState());
		assertEquals(sm.getCurrentState(), index);
	}

	/**
	 * Test the Change State operation.
	 */
	@Test
	public void testChangeState() {
		StateVertex state2 = new StateVertex("state2", "<table><div>state2</div></table>");

		/**
		 * Can not change index because not added.
		 */
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		/**
		 * Add index.
		 */
		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.update(c, state2, dummyBrowser, new CrawlSession(dummyPool)));

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
		StateVertex state2 = new StateVertex("state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex("state3", "<table><div>state2</div></table>");
		/**
		 * Can not change to state2 because not inserted yet.
		 */
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.update(c, state2, dummyBrowser, new CrawlSession(dummyPool)));

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
		assertFalse(sm.update(c2, state3, dummyBrowser, new CrawlSession(dummyPool)));

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
		StateVertex state2 = new StateVertex("state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex("state3", "<table><div>state2</div></table>");
		StateVertex state4 = new StateVertex("state4", "<table><div>state4</div></table>");
		/**
		 * Can not change to state2 because not inserted yet.
		 */
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);
		assertTrue(sm.update(c, state2, dummyBrowser, new CrawlSession(dummyPool)));

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
		assertFalse(sm.update(c2, state3, dummyBrowser, new CrawlSession(dummyPool)));

		Eventable c3 = new Eventable(new Identification(How.xpath, "/bla2"), EventType.click);

		// True because its not yet known
		assertTrue(sm.update(c3, state4, dummyBrowser, new CrawlSession(dummyPool)));

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
		StateVertex state2 = new StateVertex("state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex("state3", "<table><div>state2</div></table>");

		ArrayList<Invariant> iList = new ArrayList<Invariant>();
		hit = false;
		iList.add(new Invariant("Test123", new Condition() {

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
		StateMachine smLocal = new StateMachine(new StateFlowGraph(index), index, iList);

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(smLocal.update(c, state2, dummyBrowser, new CrawlSession(dummyPool)));

		// New State so hit must be true;
		assertTrue("Invariants are exeucted", hit);
		hit = false;
		assertFalse("Hit reseted", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(smLocal.update(c2, state3, dummyBrowser, new CrawlSession(dummyPool)));
		// CLONE State so hit must be true;
		assertTrue("Invariants are exeucted", hit);
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
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		CrawlSpecification spec = new CrawlSpecification("about:plugins");
		cfg.setCrawlSpecification(spec);
		cfg.addPlugin(new OnNewStatePlugin() {

			@Override
			public void onNewState(CrawlSession session) {
				hit = true;
			}
		});
		CrawljaxPluginsUtil.loadPlugins(new CrawljaxConfigurationReader(cfg).getPlugins());

		// state2.equals(state3)
		StateVertex state2 = new StateVertex("state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex("state3", "<table><div>state2</div></table>");

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(sm.update(c, state2, dummyBrowser, new CrawlSession(dummyPool)));

		// New State so hit must be true;
		assertTrue("Plugins are exeucted", hit);
		hit = false;
		assertFalse("Hit reseted", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(sm.update(c2, state3, dummyBrowser, new CrawlSession(dummyPool)));

		// CLONE State so no plugin execution
		assertFalse("Plugins are NOT exeucted", hit);
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
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		CrawlSpecification spec = new CrawlSpecification("about:plugins");
		cfg.setCrawlSpecification(spec);
		cfg.addPlugin(new OnInvariantViolationPlugin() {
			@Override
			public void onInvariantViolation(Invariant invariant, CrawlSession session) {
				hit = true;
			}
		});
		CrawljaxPluginsUtil.loadPlugins(new CrawljaxConfigurationReader(cfg).getPlugins());

		ArrayList<Invariant> iList = new ArrayList<Invariant>();
		iList.add(new Invariant("Test123", new Condition() {

			@Override
			public NodeList getAffectedNodes() {
				return null;
			}

			@Override
			public boolean check(EmbeddedBrowser browser) {
				return false;
			}
		}));
		StateMachine smLocal = new StateMachine(new StateFlowGraph(index), index, iList);
		// state2.equals(state3)
		StateVertex state2 = new StateVertex("state2", "<table><div>state2</div></table>");
		StateVertex state3 = new StateVertex("state3", "<table><div>state2</div></table>");

		Eventable c = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertTrue(smLocal.update(c, state2, dummyBrowser, new CrawlSession(dummyPool)));

		// New State so hit must be true;
		assertTrue("InvariantViolationPlugin are exeucted", hit);
		hit = false;
		assertFalse("Hit reseted", hit);

		Eventable c2 = new Eventable(new Identification(How.xpath, "/bla"), EventType.click);

		assertFalse(smLocal.update(c2, state3, dummyBrowser, new CrawlSession(dummyPool)));

		// New State so plugin execution
		assertTrue("InvariantViolationPlugin are exeucted", hit);
	}
}

/**
 * Created Dec 20, 2007
 */
package com.crawljax.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.util.PropertyHelper;

/**
 * @author mesbah
 * @version $Id: StateMachineTest.java 6275 2009-12-23 14:13:05Z stefan $
 */
public class StateMachineTest {
	private StateMachine sm;
	private final StateVertix state = new StateVertix("index", "<table><div>state</div></table>");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(new CrawlSpecification("http://www.crawljax.com"));
		PropertyHelper.init(config);
	}

	@Test
	public void testStateMachine() {
		sm = new StateMachine(state);
		assertNotNull(sm);
		assertNotNull(sm.getCurrentState());
		assertEquals(sm.getCurrentState(), state);
	}

	@Test
	public void testChangeState() {
		sm = new StateMachine(state);

		StateVertix state2 = new StateVertix("state2", "<table><div>state2</div></table>");
		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification("xpath", "/bla"), "onclick");
		sm.addStateToCurrentState(state2, c);
		assertTrue(sm.changeState(state2));
		assertEquals(sm.getCurrentState(), state2);
	}

	@Test
	public void testAddState() {
		sm = new StateMachine(state);

		StateVertix state2 = new StateVertix("state2", "<table><div>state2</div></table>");

		assertFalse(sm.changeState(state2));
		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification("xpath", "/bla"), "onclick");
		sm.addStateToCurrentState(state2, c);
		assertTrue(sm.changeState(state2));
		assertEquals(sm.getCurrentState(), state2);
	}

	@Test
	public void testAddStateToCurrentState() {
		sm = new StateMachine(state);

		StateVertix state2 = new StateVertix("state2", "<table><div>state2</div></table>");
		StateVertix state3 = new StateVertix("state3", "<table><div>state3</div></table>");

		assertNotSame(sm.getCurrentState(), state2);

		Eventable c = new Eventable(new Identification("xpath", "/body/div[3]/a"), "onclick");
		sm.addStateToCurrentState(state2, c);
		assertTrue(sm.changeState(state2));
		assertEquals(sm.getCurrentState(), state2);
		sm.addStateToCurrentState(state3, c);
		sm.changeState(state);
		sm.addStateToCurrentState(state3, c);
		assertEquals(3, sm.getStateFlowGraph().getAllStates().size());
	}
}

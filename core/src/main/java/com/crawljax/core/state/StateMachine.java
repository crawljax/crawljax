/**
 * Created Aug 28, 2007
 */
package com.crawljax.core.state;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.condition.invariant.InvariantChecker;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;

/**
 * The State Machine.
 * 
 * @author mesbah
 * @version $Id$
 */
public class StateMachine {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class.getName());
	/**
	 * One-to-one relation with the StateFlowGraph, the stateFlowGraph variable is never changed.
	 */
	private final StateFlowGraph stateFlowGraph;

	/**
	 * One-to-one relation with the initalState, the initalState is never changed.
	 */
	private final StateVertex initialState;

	private StateVertex currentState;

	private StateVertex previousState;

	/**
	 * The invariantChecker to use when updating the state machine.
	 */
	private final InvariantChecker invariantChecker;

	/**
	 * Create a new StateMachine with a empty Invariant list in the {@link InvariantChecker}.
	 * 
	 * @param sfg
	 *            the state flow graph that is shared.
	 * @param indexState
	 *            the state representing the Index vertix
	 */
	public StateMachine(StateFlowGraph sfg, StateVertex indexState) {
		this(sfg, indexState, new ArrayList<Invariant>());
	}

	/**
	 * Create a new StateMachine.
	 * 
	 * @param sfg
	 *            the state flow graph that is shared.
	 * @param indexState
	 *            the state representing the Index vertix
	 * @param invariantList
	 *            the invariants to use in the InvariantChecker.
	 */
	public StateMachine(StateFlowGraph sfg, StateVertex indexState, List<Invariant> invariantList) {
		stateFlowGraph = sfg;
		this.initialState = indexState;
		currentState = initialState;
		invariantChecker = new InvariantChecker(invariantList);
	}

	/**
	 * Change the currentState to the nextState if possible. The next state should already be
	 * present in the graph.
	 * 
	 * @param nextState
	 *            the next state.
	 * @return true if currentState is successfully changed.
	 */
	public boolean changeState(StateVertex nextState) {
		if (nextState == null) {
			LOGGER.info("nextState given is null");
			return false;
		}
		LOGGER.debug("AFTER: sm.current: " + currentState.getName() + " hold.current: "
		        + nextState.getName());

		if (stateFlowGraph.canGoTo(currentState, nextState)) {

			LOGGER.debug("Changed To state: " + nextState.getName() + " From: "
			        + currentState.getName());

			this.previousState = this.currentState;
			currentState = nextState;

			LOGGER.info("StateMachine's Pointer changed to: " + currentState);

			return true;
		} else {
			LOGGER.info("Cannot change To state: " + nextState.getName() + " From: "
			        + currentState.getName());
			return false;
		}
	}

	/**
	 * Adds the newState and the edge between the currentState and the newState on the SFG.
	 * 
	 * @param newState
	 *            the new state.
	 * @param eventable
	 *            the clickable causing the new state.
	 * @return the clone state iff newState is a clone, else returns null
	 */
	private StateVertex addStateToCurrentState(StateVertex newState, Eventable eventable) {
		LOGGER.debug("currentState: " + currentState.getName());
		LOGGER.debug("newState: " + newState.getName());

		// Add the state to the stateFlowGraph. Store the result
		StateVertex cloneState = stateFlowGraph.addState(newState);

		// Is there a clone detected?
		if (cloneState != null) {
			LOGGER.info("CLONE State detected: " + newState.getName() + " and "
			        + cloneState.getName() + " are the same.");
			LOGGER.debug("CLONE CURRENTSTATE: " + currentState.getName());
			LOGGER.debug("CLONE STATE: " + cloneState.getName());
			LOGGER.debug("CLONE CLICKABLE: " + eventable);
			newState = cloneState;

		} else {
			LOGGER.info("State " + newState.getName() + " added to the StateMachine.");
		}

		// Add the Edge
		stateFlowGraph.addEdge(currentState, newState, eventable);

		return cloneState;
	}

	/**
	 * Return the current State in this state machine.
	 * 
	 * @return the current State.
	 */
	public StateVertex getCurrentState() {
		return currentState;
	}

	/**
	 * reset the state machine to the initial state.
	 */
	public void rewind() {
		this.currentState = this.initialState;
		this.previousState = null;
	}

	/**
	 * @param event
	 *            the event edge.
	 * @param newState
	 *            the new state.
	 * @param browser
	 *            used to feet to checkInvariants
	 * @param session
	 *            the current Session
	 * @return true if the new state is not found in the state machine.
	 */
	public boolean update(final Eventable event, StateVertex newState, EmbeddedBrowser browser,
	        CrawlSession session) {
		StateVertex cloneState = this.addStateToCurrentState(newState, event);

		if (cloneState != null) {
			newState = cloneState;
		}

		this.changeState(newState);

		LOGGER.info("StateMachine's Pointer changed to: " + this.currentState.getName()
		        + " FROM " + previousState.getName());

		session.setCurrentState(newState);

		checkInvariants(browser, session);

		if (cloneState == null) {
			CrawljaxPluginsUtil.runOnNewStatePlugins(session);
			// recurse
			return true;
		} else {
			// non recurse
			return false;
		}
	}

	/**
	 * Check the invariants. This call is nearly thread safe only calls to set/get affected nodes in
	 * a Invariant may produce wrong output.
	 * 
	 * @param browser
	 *            the browser to feed to the invariants
	 * @param session
	 *            the current CrawlSession
	 */
	private void checkInvariants(EmbeddedBrowser browser, CrawlSession session) {
		if (invariantChecker.getInvariants() != null
		        && !invariantChecker.getInvariants().isEmpty()
		        && !invariantChecker.check(browser)) {
			for (Invariant failedInvariant : invariantChecker.getFailedInvariants()) {
				CrawljaxPluginsUtil.runOnInvriantViolationPlugins(failedInvariant, session);
			}
		}
	}
}

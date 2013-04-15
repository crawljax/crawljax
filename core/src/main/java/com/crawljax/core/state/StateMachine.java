package com.crawljax.core.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.oraclecomparator.StateComparator;
import com.google.common.collect.ImmutableList;

/**
 * The State Machine.
 */
public class StateMachine {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class.getName());

	private final StateFlowGraph stateFlowGraph;

	private final StateVertex initialState;

	private StateVertex currentState;
	private Object stateLock = new Object();

	/**
	 * The invariantChecker to use when updating the state machine.
	 */
	private final ConditionTypeChecker<Invariant> invariantChecker;

	private final Plugins plugins;

	private final StateComparator stateComparator;

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
	public StateMachine(StateFlowGraph sfg, StateVertex indexState,
	        ImmutableList<Invariant> invariantList, Plugins plugins,
	        StateComparator stateComparator) {
		stateFlowGraph = sfg;
		this.initialState = indexState;
		this.plugins = plugins;
		this.stateComparator = stateComparator;
		currentState = initialState;
		invariantChecker = new ConditionTypeChecker<>(invariantList);
	}

	public StateVertex newStateFor(EmbeddedBrowser browser) {
		return new StateVertex(browser.getCurrentUrl(),
		        stateFlowGraph.getNewStateName(),
		        browser.getDom(),
		        stateComparator.getStrippedDom(browser));
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
		LOGGER.debug("Trying to change to state: '{}' from: '{}'", nextState.getName(),
		        currentState.getName());

		synchronized (stateLock) {
			if (stateFlowGraph.canGoTo(currentState, nextState)) {

				LOGGER.debug("Changed to state: '{}' from: '{}'", nextState.getName(),
				        currentState.getName());

				currentState = nextState;

				return true;
			} else {
				LOGGER.info("Cannot go to state: '{}' from: '{}'", nextState.getName(),
				        currentState.getName());
				return false;
			}
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
		LOGGER.debug("addStateToCurrentState currentState: {} newstate {}",
		        currentState.getName(), newState.getName());

		// Add the state to the stateFlowGraph. Store the result
		StateVertex cloneState = stateFlowGraph.putIfAbsent(newState);

		// Is there a clone detected?
		if (cloneState != null) {
			LOGGER.info("CLONE State detected: {} and {} are the same.", newState.getName(),
			        cloneState.getName());
			LOGGER.debug("CLONE CURRENTSTATE: {}", currentState.getName());
			LOGGER.debug("CLONE STATE: {}", cloneState.getName());
			LOGGER.debug("CLONE CLICKABLE: {}", eventable);
			stateFlowGraph.addEdge(currentState, cloneState, eventable);
		} else {
			stateFlowGraph.addEdge(currentState, newState, eventable);
			LOGGER.info("State {} added to the StateMachine.", newState.getName());
		}

		// Add the Edge

		return cloneState;
	}

	/**
	 * Return the current State in this state machine.
	 * 
	 * @return the current State.
	 */
	public StateVertex getCurrentState() {
		synchronized (stateLock) {
			return currentState;
		}
	}

	/**
	 * reset the state machine to the initial state.
	 */
	public void rewind() {
		synchronized (stateLock) {
			this.currentState = this.initialState;
		}
	}

	/**
	 * Adds an edge between teh current and new state.
	 * 
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
	public boolean swithToStateAndCheckIfClone(final Eventable event, StateVertex newState,
	        EmbeddedBrowser browser,
	        CrawlSession session) {
		StateVertex cloneState = this.addStateToCurrentState(newState, event);

		runOnInvriantViolationPlugins(browser, session);

		if (cloneState == null) {
			this.changeState(newState);
			plugins.runOnNewStatePlugins(session);
			return true;
		} else {
			this.changeState(cloneState);
			return false;
		}
	}

	private void runOnInvriantViolationPlugins(EmbeddedBrowser browser, CrawlSession session) {
		for (Invariant failedInvariant : invariantChecker.getFailedConditions(browser)) {
			plugins.runOnInvriantViolationPlugins(failedInvariant, session);
		}
	}
}

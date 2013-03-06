package com.crawljax.core.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.google.common.collect.ImmutableList;

/**
 * The State Machine.
 */
public class StateMachine {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class.getName());

	private final StateFlowGraph stateFlowGraph;

	private final StateVertex initialState;

	private StateVertex currentState;
	private StateVertex previousState;
	private Object stateLock = new Object();

	/**
	 * The invariantChecker to use when updating the state machine.
	 */
	private final ConditionTypeChecker<Invariant> invariantChecker;

	/**
	 * Create a new StateMachine with a empty Invariant list in the {@link ConditionTypeChecker}.
	 * 
	 * @param sfg
	 *            the state flow graph that is shared.
	 * @param indexState
	 *            the state representing the Index vertix
	 */
	public StateMachine(StateFlowGraph sfg, StateVertex indexState) {
		this(sfg, indexState, ImmutableList.<Invariant> of());
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
	public StateMachine(StateFlowGraph sfg, StateVertex indexState,
	        ImmutableList<Invariant> invariantList) {
		stateFlowGraph = sfg;
		this.initialState = indexState;
		currentState = initialState;
		invariantChecker = new ConditionTypeChecker<>(invariantList);
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
		LOGGER.debug("AFTER: sm.current: {} hold.current: {}", currentState.getName(),
		        nextState.getName());

		synchronized (stateLock) {
			if (stateFlowGraph.canGoTo(currentState, nextState)) {

				LOGGER.debug("Changed To state: {} From: {}", nextState.getName(),
				        currentState.getName());

				this.previousState = this.currentState;
				currentState = nextState;

				LOGGER.debug("StateMachine's Pointer changed to: {}", currentState);

				return true;
			} else {
				LOGGER.info("Cannot change to state: {} from: {}", nextState.getName(),
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
		StateVertex cloneState = stateFlowGraph.addState(newState);

		// Is there a clone detected?
		if (cloneState != null) {
			LOGGER.info("CLONE State detected: {} and {} are the same.", newState.getName(),
			        cloneState.getName());
			LOGGER.debug("CLONE CURRENTSTATE: {}", currentState.getName());
			LOGGER.debug("CLONE STATE: {}", cloneState.getName());
			LOGGER.debug("CLONE CLICKABLE: {}", eventable);
			newState = cloneState;

		} else {
			LOGGER.info("State {} added to the StateMachine.", newState.getName());
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
			this.previousState = null;
		}
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
	public boolean updateAndCheckIfClone(final Eventable event, StateVertex newState,
	        EmbeddedBrowser browser,
	        CrawlSession session) {
		StateVertex cloneState = this.addStateToCurrentState(newState, event);

		if (cloneState != null) {
			newState = cloneState;
		}

		this.changeState(newState);

		LOGGER.debug("StateMachine's Pointer changed to: {} FROM {}",
		        this.currentState.getName(), previousState.getName());

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

	private void checkInvariants(EmbeddedBrowser browser, CrawlSession session) {
		for (Invariant failedInvariant : invariantChecker.getFailedConditions(browser)) {
			CrawljaxPluginsUtil.runOnInvriantViolationPlugins(failedInvariant, session);
		}
	}
}

package com.crawljax.core.state;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.oraclecomparator.StateComparator;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The State Machine.
 */
public class StateMachine {

	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class.getName());

	private final InMemoryStateFlowGraph stateFlowGraph;

	private final StateVertex initialState;

	private StateVertex currentState;

	/**
	 * The invariantChecker to use when updating the state machine.
	 */
	private final ConditionTypeChecker<Invariant> invariantChecker;

	private final Plugins plugins;

	private final StateComparator stateComparator;

	private List<StateVertex> onURLSet = new ArrayList<>();

	public StateMachine(InMemoryStateFlowGraph sfg, ImmutableList<Invariant> invariantList,
			Plugins plugins, StateComparator stateComparator, List<StateVertex> onURLSet) {
		stateFlowGraph = sfg;
		this.initialState = sfg.getInitialState();
		this.plugins = plugins;
		this.stateComparator = stateComparator;
		setCurrentState(initialState);
		invariantChecker = new ConditionTypeChecker<>(invariantList);
		if (onURLSet != null)
			this.onURLSet = onURLSet;
		// Rahul : creating a set of initial states
		if (onURLSet.isEmpty())
			onURLSet.add(initialState);
	}

	public StateVertex newStateFor(EmbeddedBrowser browser) {
		return stateFlowGraph.newStateFor(browser.getCurrentUrl(), browser.getStrippedDom(),
				stateComparator.getStrippedDom(browser), browser);
	}

	/**
	 * Change the currentState to the nextState if possible. The next state should already be
	 * present in the graph.
	 *
	 * @param nextState the next state.
	 * @return true if currentState is successfully changed.
	 */
	public boolean changeState(StateVertex nextState) {
		if (nextState == null) {
			LOGGER.info("nextState given is null");
			return false;
		}
		LOGGER.debug("Trying to change to state: '{}' from: '{}'", nextState.getName(),
				currentState.getName());

		if (stateFlowGraph.canGoTo(currentState, nextState)) {

			LOGGER.debug("Changed to state: '{}' from: '{}'", nextState.getName(),
					currentState.getName());

			setCurrentState(nextState);

			return true;
		} else {
			LOGGER.info("Cannot go to state: '{}' from: '{}'", nextState.getName(),
					currentState.getName());
			return false;
		}
	}

	/**
	 * Adds the newState and the edge between the currentState and the newState on the SFG.
	 *
	 * @param newState  the new state.
	 * @param eventable the clickable causing the new state.
	 * @return the clone state iff newState is a clone, else returns null
	 */
	private StateVertex addStateToCurrentState(StateVertex newState, Eventable eventable) {
		LOGGER.debug("addStateToCurrentState currentState: {} newState {}",
				currentState.getName(), newState.getName());

		// Add the state to the stateFlowGraph. Store the result
		StateVertex cloneState = stateFlowGraph.putIfAbsent(newState);

		// Is there a clone detected?
		if (cloneState != null) {
			LOGGER.info("CLONE State detected: {} and {} are the same.", newState.getName(),
					cloneState.getName());
			LOGGER.debug("CLONE CURRENT STATE: {}", currentState.getName());
			LOGGER.debug("CLONE STATE: {}", cloneState.getName());
			LOGGER.debug("CLONE CLICKABLE: {}", eventable);
			boolean added = stateFlowGraph.addEdge(currentState, cloneState, eventable);
			if (!added) {
				LOGGER.debug("Clone edge !! Need to fix the crawlPath??");
			}
		} else {
			stateFlowGraph.addEdge(currentState, newState, eventable);
			LOGGER.info("State {} added to the StateMachine.", newState.getName());
		}

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
	}

	/**
	 * Adds an edge between the current and new state.
	 *
	 * @return true if the new state is not found in the state machine.
	 */
	public boolean switchToStateAndCheckIfClone(final Eventable event, StateVertex newState,
												CrawlerContext context) {
		StateVertex cloneState = this.addStateToCurrentState(newState, event);

		runOnInvariantViolationPlugins(context);

		if (cloneState == null) {
			changeState(newState);
			plugins.runOnNewStatePlugins(context, newState);
			return true;
		} else {
			changeState(cloneState);
			return false;
		}
	}

	public void runOnInvariantViolationPlugins(CrawlerContext context) {
		for (Invariant failedInvariant : invariantChecker
				.getFailedConditions(context.getBrowser())) {
			plugins.runOnInvariantViolationPlugins(failedInvariant, context);
		}
	}

	public void setCurrentState(StateVertex currentState) {
		this.currentState = currentState;
	}

	public List<StateVertex> getOnURLSet() {
		return this.onURLSet;
	}

	public InMemoryStateFlowGraph getStateFlowGraph() {
		return stateFlowGraph;
	}

}

package com.crawljax.core.state;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.domcomparators.DomStrippers;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The State Machine.
 */
public class StateMachine {

	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class.getName());

	private final InMemoryStateFlowGraph stateFlowGraph;

	private final StateVertex initialState;
	private final boolean shouldCrawlFrames;

	private StateVertex currentState;

	/**
	 * The invariantChecker to use when updating the state machine.
	 */
	private final ConditionTypeChecker<Invariant> invariantChecker;

	private final Plugins plugins;

	private final DomStrippers domStrippers;

	public StateMachine(InMemoryStateFlowGraph sfg,
	        ImmutableList<Invariant> invariantList, Plugins plugins,
	        DomStrippers domStrippers, CrawlRules crawlRules) {
		stateFlowGraph = sfg;
		this.initialState = sfg.getInitialState();
		this.plugins = plugins;
		this.domStrippers = domStrippers;
		currentState = initialState;
		invariantChecker = new ConditionTypeChecker<>(invariantList);
		shouldCrawlFrames = crawlRules.shouldCrawlFrames();
	}

	public StateVertex newStateFor(EmbeddedBrowser browser) {
		String dom;
		if (shouldCrawlFrames) {
			dom = browser.getDomWithIFrameContents();
		} else {
			dom = browser.getDom();
		}
		return stateFlowGraph.newStateFor(
		        browser.getCurrentUrl(),
		        dom,
		        domStrippers.getStrippedDom(dom));
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
	public boolean swithToStateAndCheckIfClone(final Eventable event, StateVertex newState,
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

	private void runOnInvariantViolationPlugins(CrawlerContext context) {
		for (Invariant failedInvariant : invariantChecker.getFailedConditions(context
		        .getBrowser())) {
			plugins.runOnInvariantViolationPlugins(failedInvariant, context);
		}
	}

}

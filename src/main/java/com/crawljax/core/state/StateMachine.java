/**
 * Created Aug 28, 2007
 */
package com.crawljax.core.state;

import org.apache.log4j.Logger;

import com.crawljax.util.database.HibernateUtil;

/**
 * The State Machine.
 * 
 * @author mesbah
 * @version $Id$
 */
public class StateMachine {
	private static final Logger LOGGER = Logger.getLogger(StateMachine.class.getName());
	private final StateFlowGraph stateFlowGraph;
	private final StateVertix initialState;
	private StateVertix currentState;
	private StateVertix previousState;

	/**
	 * Create a new StateMachine.
	 * 
	 * @param indexState
	 *            the state representing the Index vertix
	 */
	public StateMachine(StateVertix indexState) {
		stateFlowGraph = new StateFlowGraph();
		this.initialState = indexState;
		currentState = initialState;
		stateFlowGraph.addState(currentState);
	}

	/**
	 * Change the currentState to the nextState if possible. The next state should already be
	 * present in the graph.
	 * 
	 * @param nextState
	 *            the next state.
	 * @return true if currentState is successfully changed.
	 */
	public boolean changeState(StateVertix nextState) {
		if (stateFlowGraph.canGoTo(currentState, nextState)) {
			LOGGER.debug("Changed To state: " + nextState.getName() + " From: "
			        + currentState.getName());
			this.previousState = this.currentState;
			currentState = nextState;

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
	public StateVertix addStateToCurrentState(StateVertix newState, Eventable eventable) {
		LOGGER.debug("currentState: " + currentState.getName());
		LOGGER.debug("newState: " + newState.getName());
		currentState = stateFlowGraph.getStateInGraph(currentState);
		StateVertix cloneState = null;
		if (stateFlowGraph.containsVertex(newState)) {
			String name = newState.getName();
			newState = stateFlowGraph.getStateInGraph(newState);
			LOGGER.info("CLONE State detected: " + name + " and " + newState.getName()
			        + " are the same.");
			LOGGER.debug("CLONE CURRENTSTATE: " + currentState.getName());
			LOGGER.debug("CLONE STATE: " + newState.getName());
			LOGGER.debug("CLONE CLICKABLE: " + eventable);
			cloneState = newState;
		} else {
			stateFlowGraph.addState(newState);
			LOGGER.info("State " + newState.getName() + " added to the StateMachine.");
		}

		// eventable.setSourceStateVertix(currentState);
		// eventable.setTargetStateVertix(newState);
		HibernateUtil.insert(eventable);

		stateFlowGraph.addEdge(currentState, newState, eventable);

		return cloneState;
	}

	/**
	 * TODO: DOCUMENT ME!
	 * 
	 * @return the current State.
	 */
	public StateVertix getCurrentState() {
		return currentState;
	}

	/**
	 * TODO: DOCUMENT ME!
	 * 
	 * @return the stateFlowGraph
	 */
	public StateFlowGraph getStateFlowGraph() {
		return stateFlowGraph;
	}

	/**
	 * @return the initialState
	 */
	public StateVertix getInitialState() {
		return initialState;
	}

	/**
	 * @return TODO: DOCUMENT ME!
	 */
	public StateVertix getPreviousState() {
		return previousState;
	}
}

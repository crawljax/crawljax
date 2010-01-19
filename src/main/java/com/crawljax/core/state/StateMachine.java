/**
 * Created Aug 28, 2007
 */
package com.crawljax.core.state;

import org.apache.log4j.Logger;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.util.PropertyHelper;
import com.crawljax.util.database.HibernateUtil;

/**
 * The State Machine.
 * 
 * @author mesbah
 * @version $Id$
 */
public class StateMachine {
	private static final Logger LOGGER = Logger.getLogger(StateMachine.class.getName());
	/**
	 * One-to-one releation with the StateFlowGraph, the stateFlowGraph variable is never changed.
	 */
	private final StateFlowGraph stateFlowGraph;

	/**
	 * One-to-one releation with the initalState, the initalState is never changed.
	 */
	private StateVertix initialState;

	/**
	 * TODO Stefan Current and previous state often changes; thus given problems.
	 */
	private StateVertix currentState;

	@SuppressWarnings("unused")
	@Deprecated
	private StateVertix previousState;

	private boolean haveLock = false;

	/**
	 * Create a new StateMachine.
	 * 
	 * @param sfg
	 *            the state flow graph that is shared.
	 * @param indexState
	 *            the state representing the Index vertix
	 */
	public StateMachine(StateFlowGraph sfg, StateVertix indexState) {
		stateFlowGraph = sfg;
		this.initialState = indexState;
		currentState = initialState;
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
		LOGGER.debug("AFTER: sm.current: " + currentState.getName() + " hold.current: "
		        + nextState.getName());
		if (!haveLock) {
			stateFlowGraph.requestStateFlowGraphMutex();
		}
		if (stateFlowGraph.canGoTo(currentState, nextState)) {
			if (!haveLock) {
				stateFlowGraph.releaseStateFlowGraphMutex();
			}
			LOGGER.debug("Changed To state: " + nextState.getName() + " From: "
			        + currentState.getName());
			this.previousState = this.currentState;
			currentState = nextState;
			LOGGER.info("StateMachine's Pointer changed to: " + currentState);

			return true;
		} else {
			if (!haveLock) {
				stateFlowGraph.releaseStateFlowGraphMutex();
			}
			LOGGER.info("Cannot change To state: " + nextState.getName() + " From: "
			        + currentState.getName());
			return false;
		}
	}

	/**
	 * Adds the newState and the edge between the currentState and the newState on the SFG. TODO
	 * Stefan insert synchronisation
	 * 
	 * @param newState
	 *            the new state.
	 * @param eventable
	 *            the clickable causing the new state.
	 * @return the clone state iff newState is a clone, else returns null
	 */
	private StateVertix addStateToCurrentState(StateVertix newState, Eventable eventable) {
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
	 * reset the state machine to the initial state.
	 */
	public void rewind() {
		this.currentState = this.initialState;
		this.previousState = null;
	}

	/**
	 * @param currentHold
	 *            the placeholder for the current stateVertix.
	 * @param event
	 *            the event edge.
	 * @param newState
	 *            the new state.
	 * @param browser
	 *            used to feet to checkInvariants
	 * @param controller
	 *            the CrawljaxController to inquire for the checkInvariants
	 * @return true if the new state is not found in the state machine.
	 */
	public boolean update(final StateVertix currentHold, final Eventable event,
	        StateVertix newState, EmbeddedBrowser browser, CrawljaxController controller) {
		stateFlowGraph.requestStateFlowGraphMutex();
		haveLock = true;
		StateVertix cloneState = this.addStateToCurrentState(newState, event);

		if (cloneState != null) {
			newState = cloneState.clone();
		}

		this.changeState(newState);
		stateFlowGraph.releaseStateFlowGraphMutex();
		haveLock = false;
		LOGGER.info("StateMachine's Pointer changed to: " + this.currentState.getName()
		        + " FROM " + currentHold.getName());

		if (PropertyHelper.getTestInvariantsWhileCrawlingValue()) {
			controller.checkInvariants(browser);
		}

		/**
		 * TODO Stefan FIX this getSession stuff...
		 */
		synchronized (controller.getSession()) {
			/**
			 * Only one thread at the time may set the currentState in the session and expose it to
			 * the OnNewStatePlugins. Garranty it will not be interleaved
			 */
			controller.getSession().setCurrentState(newState);

			if (cloneState == null) {
				CrawljaxPluginsUtil.runOnNewStatePlugins(controller.getSession());
				// recurse
				return true;
			} else {
				// non recurse
				return false;
			}
		}
	}

	/**
	 * @param initialState
	 *            the initialState to set
	 */
	public final void setInitialState(StateVertix initialState) {
		this.initialState = initialState;
		this.currentState = initialState;
	}

	/**
	 * Return the number of states in the StateFlowGraph.
	 * 
	 * @return the number of states in the StateFlowGraph
	 */
	public int getNumberOfStates() {
		return this.stateFlowGraph.getAllStates().size();
	}
}

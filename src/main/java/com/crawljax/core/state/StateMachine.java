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
	 * One-to-one relation with the StateFlowGraph, the stateFlowGraph variable is never changed.
	 */
	private final StateFlowGraph stateFlowGraph;

	/**
	 * One-to-one relation with the initalState, the initalState is never changed.
	 */
	private StateVertix initialState;

	private StateVertix currentState;

	private StateVertix previousState;

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

		// Add the state to the stateFlowGraph. Store the result
		StateVertix cloneState = stateFlowGraph.addState(newState);

		// Is there a clone detected?
		if (cloneState != null) {
			LOGGER.info("CLONE State detected: " + newState.getName() + " and "
			        + cloneState.getName() + " are the same.");
			LOGGER.debug("CLONE CURRENTSTATE: " + currentState.getName());
			LOGGER.debug("CLONE STATE: " + cloneState.getName());
			LOGGER.debug("CLONE CLICKABLE: " + eventable);
		} else {
			LOGGER.info("State " + newState.getName() + " added to the StateMachine.");
		}

		// Store in DB
		HibernateUtil.insert(eventable);

		// Add the Edge
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
	 * TODO Stefan Remove the controller argument.
	 * 
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
	public boolean update(final Eventable event, StateVertix newState, EmbeddedBrowser browser,
	        CrawljaxController controller) {
		StateVertix cloneState = this.addStateToCurrentState(newState, event);

		if (cloneState != null) {
			// Why cloning?
			newState = cloneState;
		}

		this.changeState(newState);

		LOGGER.info("StateMachine's Pointer changed to: " + this.currentState.getName()
		        + " FROM " + previousState.getName());

		if (PropertyHelper.getTestInvariantsWhileCrawlingValue()) {
			controller.checkInvariants(browser);
		}

		/**
		 * TODO Stefan FIX this getSession stuff...
		 */
		synchronized (controller.getSession()) {
			/**
			 * Only one thread at the time may set the currentState in the session and expose it to
			 * the OnNewStatePlugins. Guaranty it will not be interleaved
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

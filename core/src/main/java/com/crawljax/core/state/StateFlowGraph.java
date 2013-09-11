package com.crawljax.core.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.GraphPath;

import java.util.List;
import java.util.Set;

/**
 * A graph of {@link StateVertex} as vertexes and {@link Eventable} as edges
 */
public interface StateFlowGraph {

	/**
	 * @param id
	 *            The ID of the state
	 * @return The state if found or <code>null</code>.
	 */
	StateVertex getById(int id);

	/**
	 * @return The index state.
	 */
	StateVertex getInitialState();

	/**
	 * Returns a set of all clickables outgoing from the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the outgoing edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#outgoingEdgesOf(Object)
	 */
	ImmutableSet<Eventable> getOutgoingClickables(StateVertex stateVertix);

	/**
	 * Returns a set of all edges incoming into the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the incoming edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#incomingEdgesOf(Object)
	 */
	ImmutableSet<Eventable> getIncomingClickable(StateVertex stateVertix);

	/**
	 * Is it possible to go from s1 -> s2?
	 * 
	 * @param source
	 *            the source state.
	 * @param target
	 *            the target state.
	 * @return true if it is possible (edge exists in graph) to go from source to target.
	 */
	boolean canGoTo(StateVertex source, StateVertex target);

	/**
	 * Convenience method to find the Dijkstra shortest path between two states on the graph.
	 * 
	 * @param start
	 *            the start state.
	 * @param end
	 *            the end state.
	 * @return a list of shortest path of clickables from the state to the end
	 */
	ImmutableList<Eventable> getShortestPath(StateVertex start, StateVertex end);

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */
	ImmutableSet<StateVertex> getAllStates();

	/**
	 * Return all the edges in the StateFlowGraph.
	 * 
	 * @return a Set of all edges in the StateFlowGraph
	 */
	ImmutableSet<Eventable> getAllEdges();

	/**
	 * @return Dom string average size (byte).
	 */
	int getMeanStateStringSize();

	/**
	 * @return The number of states, currently in the graph.
	 */
	int getNumberOfStates();

	/**
	 * This method returns all possible paths from the index state using the Kshortest paths.
	 * 
	 * @param index
	 *            the initial state.
	 * @return a list of GraphPath lists.
	 */
	List<List<GraphPath<StateVertex, Eventable>>> getAllPossiblePaths(StateVertex index);

	/**
	 * @param stateVertix
	 *            The source {@link StateVertex}
	 * @return a {@link Set} of {@link StateVertex} that are connected to the source
	 *         {@link StateVertex} via one of the sources outgoing edges.
	 */
	ImmutableSet<StateVertex> getOutgoingStates(StateVertex stateVertix);

}

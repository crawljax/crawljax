package com.crawljax.core.state;

import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * <<<<<<< Updated upstream A graph of {@link StateVertex} as vertexes and {@link Eventable} as
 * edges. ======= A graph with {@link StateVertex} as vertexes and {@link Eventable} as edges.
 * >>>>>>> Stashed changes
 */
public interface StateFlowGraph {

	public enum StateFlowGraphType {
		DEFAULT, SCALABLE
	}

	/**
	 * Adds a state (as a vertix) to the State-Flow Graph if not already present. More formally,
	 * adds the specified vertex, v, to this graph if this graph contains no vertex u such that
	 * u.equals(v). If this graph already contains such vertex, the call leaves this graph unchanged
	 * and returns false. In combination with the restriction on constructors, this ensures that
	 * graphs never contain duplicate vertices. Throws java.lang.NullPointerException - if the
	 * specified vertex is null. This method automatically updates the state name to reflect the
	 * internal state counter.
	 * 
	 * @param stateVertix
	 *            the state to be added.
	 * @return the clone if one is detected null otherwise.
	 * @see org.jgrapht.Graph#addVertex(Object)
	 */
	public StateVertex putIfAbsent(StateVertex stateVertix);

	public StateVertex putIndex(StateVertex index);

	/**
	 * @param id
	 *            The ID of the state
	 * @return The state if found or <code>null</code>.
	 */
	public StateVertex getById(int id);

	/**
	 * @return The index state.
	 */
	public StateVertex getInitialState();

	/**
	 * Adds the specified edge to this graph, going from the source vertex to the target vertex.
	 * More formally, adds the specified edge, e, to this graph if this graph contains no edge e2
	 * such that e2.equals(e). If this graph already contains such an edge, the call leaves this
	 * graph unchanged and returns false. Some graphs do not allow edge-multiplicity. In such cases,
	 * if the graph already contains an edge from the specified source to the specified target, than
	 * this method does not change the graph and returns false. If the edge was added to the graph,
	 * returns true. The source and target vertices must already be contained in this graph. If they
	 * are not found in graph IllegalArgumentException is thrown.
	 * 
	 * @param sourceVert
	 *            source vertex of the edge.
	 * @param targetVert
	 *            target vertex of the edge.
	 * @param clickable
	 *            the clickable edge to be added to this graph.
	 * @return true if this graph did not already contain the specified edge.
	 * @see org.jgrapht.Graph#addEdge(Object, Object, Object)
	 */
	public boolean addEdge(StateVertex sourceVert, StateVertex targetVert,
	        Eventable clickable);

	/**
	 * Returns a set of all clickables outgoing from the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the outgoing edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#outgoingEdgesOf(Object)
	 */
	public ImmutableSet<Eventable> getOutgoingClickables(StateVertex stateVertix);

	/**
	 * Returns a set of all edges incoming into the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the incoming edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#incomingEdgesOf(Object)
	 */
	public ImmutableSet<Eventable> getIncomingClickables(StateVertex stateVertix);

	/**
	 * Is it possible to go from s1 -> s2?
	 * 
	 * @param source
	 *            the source state.
	 * @param target
	 *            the target state.
	 * @return true if it is possible (edge exists in graph) to go from source to target.
	 */
	public boolean canGoTo(StateVertex source, StateVertex target);

	/**
	 * Convenience method to find the Dijkstra shortest path between two states on the graph.
	 * 
	 * @param start
	 *            the start state.
	 * @param end
	 *            the end state.
	 * @return a list of shortest path of clickables from the state to the end
	 */
	public ImmutableList<Eventable> getShortestPath(StateVertex start, StateVertex end);

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */
	public ImmutableSet<StateVertex> getAllStates();

	/**
	 * Return all the edges in the StateFlowGraph.
	 * 
	 * @return a Set of all edges in the StateFlowGraph
	 */
	public ImmutableSet<Eventable> getAllEdges();

	/**
	 * @return Dom string average size (byte).
	 */
	public int getMeanStateStringSize();

	/**
	 * @return The number of states, currently in the graph.
	 */
	public int getNumberOfStates();

	public StateVertex newStateFor(String url, String dom, String strippedDom);

	/**
	 * This method returns all possible paths from the index state using the Kshortest paths.
	 * 
	 * @param index
	 *            the initial state.
	 * @return a list of GraphPath lists.
	 */
	public List<List<GraphPath<StateVertex, Eventable>>> getAllPossiblePaths(StateVertex index);

	/**
	 * @param stateVertix
	 *            The source {@link StateVertex}
	 * @return a {@link Set} of {@link StateVertex} that are connected to the source
	 *         {@link StateVertex} via one of the sources outgoing edges.
	 */
	public ImmutableSet<StateVertex> getOutgoingStates(StateVertex stateVertix);

}

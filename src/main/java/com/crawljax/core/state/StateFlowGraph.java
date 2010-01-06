package com.crawljax.core.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * The State-Flow Graph is a directed graph with states on the vertices and clickables on the edges.
 * 
 * @author mesbah
 * @version $Id: StateFlowGraph.java 6234 2009-12-18 13:46:37Z mesbah $
 */
public class StateFlowGraph {
	private static final Logger LOGGER = Logger.getLogger(StateFlowGraph.class.getName());

	private final DirectedGraph<StateVertix, Eventable> sfg;

	/**
	 * The constructor.
	 */
	public StateFlowGraph() {
		sfg = new DirectedMultigraph<StateVertix, Eventable>(Eventable.class);
	}

	/**
	 * Adds a state (as a vertix) to the State-Flow Graph if not already present. More formally,
	 * adds the specified vertex, v, to this graph if this graph contains no vertex u such that
	 * u.equals(v). If this graph already contains such vertex, the call leaves this graph unchanged
	 * and returns false. In combination with the restriction on constructors, this ensures that
	 * graphs never contain duplicate vertices. Throws java.lang.NullPointerException - if the
	 * specified vertex is null.
	 * 
	 * @param stateVertix
	 *            the state to be added.
	 * @return true if this graph did not already contain the specified vertex.
	 * @see org.jgrapht.Graph#addVertex(Object)
	 */
	public boolean addState(StateVertix stateVertix) {
		return sfg.addVertex(stateVertix);
	}

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
	 * @param sourceVertex
	 *            source vertex of the edge.
	 * @param targetVertex
	 *            target vertex of the edge.
	 * @param clickable
	 *            the clickable edge to be added to this graph.
	 * @return true if this graph did not already contain the specified edge.
	 * @see org.jgrapht.Graph#addEdge(Object, Object, Object)
	 */
	public boolean addEdge(StateVertix sourceVertex, StateVertix targetVertex, Eventable clickable) {
		if (sfg.containsEdge(sourceVertex, targetVertex)
		        && clickable.equals(sfg.getEdge(sourceVertex, targetVertex))) {
			return false;
		}

		return sfg.addEdge(sourceVertex, targetVertex, clickable);
	}

	/**
	 * @return the string representation of the graph.
	 * @see org.jgrapht.DirectedGraph#toString()
	 */
	@Override
	public String toString() {
		return sfg.toString();
	}

	/**
	 * Returns a set of all clickables outgoing from the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the outgoing edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#outgoingEdgesOf(Object)
	 */
	public Set<Eventable> getOutgoingClickables(StateVertix stateVertix) {
		return sfg.outgoingEdgesOf(stateVertix);
	}

	/**
	 * Returns a set of all edges incoming into the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the incoming edges (clickables) of the stateVertix.
	 * @see org.jgrapht.DirectedGraph#incomingEdgesOf(Object)
	 */
	public Set<Eventable> getIncomingClickable(StateVertix stateVertix) {
		return sfg.incomingEdgesOf(stateVertix);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param stateVertix
	 *            the state.
	 * @return the set of outgoing states from the stateVertix.
	 */
	public Set<StateVertix> getOutgoingStates(StateVertix stateVertix) {
		final Set<StateVertix> result = new HashSet<StateVertix>();

		for (Eventable c : getOutgoingClickables(stateVertix)) {
			result.add(sfg.getEdgeTarget(c));
		}

		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param clickable
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public StateVertix getTargetState(Eventable clickable) {
		return sfg.getEdgeTarget(clickable);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param source
	 *            the source state.
	 * @param target
	 *            the target state.
	 * @return true if it is possible (edge exists in graph) to go from source to target.
	 */
	public boolean canGoTo(StateVertix source, StateVertix target) {
		return sfg.containsEdge(source, target) || sfg.containsEdge(target, source);
	}

	/**
	 * Convenience method to find the Dijkstra shortest path between two states on the graph.
	 * 
	 * @param start
	 *            the start state.
	 * @param end
	 *            the end state.
	 * @return a list of shortest path of clickables from the state to the end
	 */
	public List<Eventable> getShortestPath(StateVertix start, StateVertix end) {
		return DijkstraShortestPath.findPathBetween(sfg, start, end);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return all the states on the graph.
	 */
	public Set<StateVertix> getAllStates() {
		return sfg.vertexSet();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Set<Eventable> getAllEdges() {
		return sfg.edgeSet();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param state
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public StateVertix getStateInGraph(StateVertix state) {
		Set<StateVertix> states = getAllStates();

		for (StateVertix st : states) {
			if (state.equals(st)) {
				return st;
			}
		}

		return null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param state
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public boolean containsVertex(StateVertix state) {
		return sfg.containsVertex(state);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */

	public int getMeanStateStringSize() {
		Mean mean = new Mean();
		List<Integer> list = new ArrayList<Integer>();

		for (StateVertix state : getAllStates()) {
			list.add(new Integer(state.getDomSize()));
		}

		/* calculate the mean */
		for (Integer num : list) {
			mean.increment(num.intValue());
		}
		return new Double(mean.getResult()).intValue();
	}

	/**
	 * @return the sfg
	 */
	public DirectedGraph<StateVertix, Eventable> getSfg() {
		return sfg;
	}

	/**
	 * @param state
	 *            The starting state.
	 * @return A list of the deepest states (states with no outgoing edges).
	 */
	public List<StateVertix> getDeepStates(StateVertix state) {
		final Set<String> visitedStates = new HashSet<String>();
		final List<StateVertix> deepStates = new ArrayList<StateVertix>();

		traverse(visitedStates, deepStates, state);

		return deepStates;
	}

	private void traverse(Set<String> visitedStates, List<StateVertix> deepStates,
	        StateVertix state) {
		visitedStates.add(state.getName());

		Set<StateVertix> outgoingSet = getOutgoingStates(state);

		if ((outgoingSet == null) || outgoingSet.isEmpty()) {
			deepStates.add(state);
		} else {
			if (cyclic(visitedStates, outgoingSet)) {
				deepStates.add(state);
			} else {
				for (StateVertix st : outgoingSet) {
					if (!visitedStates.contains(st.getName())) {
						traverse(visitedStates, deepStates, st);
					}
				}
			}
		}
	}

	private boolean cyclic(Set<String> visitedStates, Set<StateVertix> outgoingSet) {
		int i = 0;

		for (StateVertix state : outgoingSet) {
			if (visitedStates.contains(state.getName())) {
				i++;
			}
		}

		return i == outgoingSet.size();
	}

	/**
	 * @param index
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public List<List<GraphPath<StateVertix, Eventable>>> getAllPossiblePaths(StateVertix index) {
		final List<List<GraphPath<StateVertix, Eventable>>> results =
		        new ArrayList<List<GraphPath<StateVertix, Eventable>>>();

		final KShortestPaths<StateVertix, Eventable> kPaths =
		        new KShortestPaths<StateVertix, Eventable>(this.sfg, index, Integer.MAX_VALUE);
		// System.out.println(sfg.toString());

		for (StateVertix state : getDeepStates(index)) {
			// System.out.println("Deep State: " + state.getName());

			try {
				List<GraphPath<StateVertix, Eventable>> paths = kPaths.getPaths(state);
				results.add(paths);
			} catch (Exception e) {

				LOGGER.error("Error with " + state.toString(), e);
			}

		}

		return results;
	}

}

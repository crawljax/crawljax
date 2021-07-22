package com.crawljax.core.state;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.ExitNotifier;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.KShortestSimplePaths;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedPseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The State-Flow Graph is a multi-edge directed graph with states (StateVertex) on the vertices and
 * clickables (Eventable) on the edges.
 */
@Singleton
@SuppressWarnings("serial")
public class InMemoryStateFlowGraph implements Serializable, StateFlowGraph {

	private static final Logger LOG =
			LoggerFactory.getLogger(InMemoryStateFlowGraph.class.getName());

	private final AbstractBaseGraph<StateVertex, Eventable> sfg;
	private final Lock readLock;
	private final Lock writeLock;

	/**
	 * Intermediate counter for the number of states, not relaying on getAllStates.size() because of
	 * Thread-safety.
	 */
	private final AtomicInteger stateCounter = new AtomicInteger();
	private final AtomicInteger nextStateNameCounter = new AtomicInteger();
	private final Map<Integer, StateVertex> stateById;

	private final ExitNotifier exitNotifier;
	private final StateVertexFactory vertexFactory;

	private List<Eventable> expiredEdges = new ArrayList<Eventable>();

	private List<StateVertex> expiredStates = new ArrayList<StateVertex>();

	public List<Eventable> getExpiredEdges() {
		return expiredEdges;
	}
	
	public List<StateVertex> getExpiredStates() {
		return expiredStates;
	}

	/**
	 * The constructor.
	 *
	 * @param exitNotifier used for triggering an exit.
	 */
	@Inject
	public InMemoryStateFlowGraph(ExitNotifier exitNotifier, StateVertexFactory vertexFactory) {
		this.exitNotifier = exitNotifier;
		this.vertexFactory = vertexFactory;
		sfg = new DirectedPseudograph<>(Eventable.class);
		stateById = Collections.synchronizedMap(new HashMap<>());
		LOG.debug("Initialized the state-flow graph");
		ReadWriteLock lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
	}

	/**
	 * Adds a state (as a vertex) to the State-Flow Graph if not already present. More formally,
	 * adds the specified vertex, v, to this graph if this graph contains no vertex u such that
	 * u.equals(v). If this graph already contains such vertex, the call leaves this graph unchanged
	 * and returns false. In combination with the restriction on constructors, this ensures that
	 * graphs never contain duplicate vertices. Throws java.lang.NullPointerException - if the
	 * specified vertex is null. This method automatically updates the state name to reflect the
	 * internal state counter.
	 *
	 * @param stateVertex the state to be added.
	 * @return the clone if one is detected null otherwise.
	 * @see org.jgrapht.Graph#addVertex(Object)
	 */
	public StateVertex putIfAbsent(StateVertex stateVertex) {
		return putIfAbsent(stateVertex, true);
	}

	public StateVertex putIndex(StateVertex index) {
		return putIfAbsent(index, false);
	}

	/**
	 * Adds a state (as a vertex) to the State-Flow Graph if not already present. More formally,
	 * adds the specified vertex, v, to this graph if this graph contains no vertex u such that
	 * u.equals(v). If this graph already contains such vertex, the call leaves this graph unchanged
	 * and returns false. In combination with the restriction on constructors, this ensures that
	 * graphs never contain duplicate vertices. Throws java.lang.NullPointerException - if the
	 * specified vertex is null.
	 *
	 * @param stateVertex the state to be added.
	 * @param correctName if true the name of the state will be corrected according to the internal state
	 *                    counter.
	 * @return the clone if one is detected <code>null</code> otherwise.
	 * @see org.jgrapht.Graph#addVertex(Object)
	 */
	// rahulyk: Modifying the original function to accommodate threshold based clone
	// detection <near duplicates?>
	private StateVertex putIfAbsent(StateVertex stateVertex, boolean correctName) {
		writeLock.lock();
		try {
			boolean clone = this.hasClone(stateVertex);
			if (!clone) {
				setNearDuplicate(stateVertex);

				boolean added = sfg.addVertex(stateVertex);
				if (!added)
					LOG.info("Vertex should be added !!" + stateVertex);
				stateById.put(stateVertex.getId(), stateVertex);
				int count = stateCounter.incrementAndGet();
				exitNotifier.incrementNumberOfStates();
				LOG.info("Number of states in the graph is now {}", count);
				return null;
			} else {
				// Graph already contained the vertex
				LOG.debug("Graph already contains vertex {}", stateVertex);
				return this.getStateInGraph(stateVertex);
			}
		} finally {
			writeLock.unlock();
		}
	}

	private void setNearDuplicate(StateVertex vertex) {
		double minDistance = -1.0;
		StateVertex closestVertex = null;
		for (StateVertex vertexOfGraph : sfg.vertexSet()) {
			double dist = vertex.getDist(vertexOfGraph);

			if (minDistance == -1 || dist < minDistance) {
				minDistance = dist;
				closestVertex = vertexOfGraph;
			}
		}
		if (closestVertex != null) {
			vertex.setDistToNearestState(minDistance);
			vertex.setNearestState(closestVertex.getId());

			if (vertex.inThreshold(closestVertex)) {
				vertex.setHasNearDuplicate(true);

				// TODO: recognize clusters and calculate min distance to the cluster
				/*
				 * if(closestVertex.hasNearDuplicate())
				 * vertex.setNearestState(closestVertex.getNearestState()); else
				 * vertex.setNearestState(closestVertex);
				 */
			} else {
				vertex.setHasNearDuplicate(false);
			}
		}
	}

	/**
	 * Adding assignment to dynamic fragments in case the vertex is hybridstate vertex
	 */
	@Override
	public boolean hasClone(StateVertex vertex) {
		for (StateVertex vertexOfGraph : sfg.vertexSet()) {
			if (vertex.equals(vertexOfGraph)) {
				if(vertexOfGraph instanceof HybridStateVertexImpl) {
					((HybridStateVertexImpl) vertexOfGraph).assignDynamicFragments(vertex);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public StateVertex getById(int id) {
		return stateById.get(id);
	}

	@Override
	public StateVertex getInitialState() {
		return stateById.get(StateVertex.INDEX_ID);
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
	 * @param sourceVertex source vertex of the edge.
	 * @param targetVertex target vertex of the edge.
	 * @param clickable  the clickable edge to be added to this graph.
	 * @return true if this graph did not already contain the specified edge.
	 * @see org.jgrapht.Graph#addEdge(Object, Object, Object)
	 */
	public boolean addEdge(StateVertex sourceVertex, StateVertex targetVertex, Eventable clickable) {
		clickable.setSource(sourceVertex);
		clickable.setTarget(targetVertex);
		writeLock.lock();
		try {
			boolean added = sfg.addEdge(sourceVertex, targetVertex, clickable);
			if (!added) {
				Set<Eventable> allEdges = sfg.getAllEdges(sourceVertex, targetVertex);
				for (Eventable edge : allEdges) {
					if (edge.equals(clickable)) {
						/*
						 * Setting the clickable provided to the clone edge so that crawlpath is in
						 * sync with SFG
						 */
						clickable.setId(-1);
					}
				}
			}
			return added;
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public String toString() {
		readLock.lock();
		try {
			return sfg.toString();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public ImmutableSet<Eventable> getOutgoingClickables(StateVertex stateVertex) {
		readLock.lock();
		try {
			return ImmutableSet.copyOf(sfg.outgoingEdgesOf(stateVertex));
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public ImmutableSet<Eventable> getIncomingClickable(StateVertex stateVertex) {
		readLock.lock();
		try {
			return ImmutableSet.copyOf(sfg.incomingEdgesOf(stateVertex));
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean canGoTo(StateVertex source, StateVertex target) {
		readLock.lock();
		try {
			return sfg.containsEdge(source, target) || sfg.containsEdge(target, source);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public ImmutableList<Eventable> getShortestPath(StateVertex start, StateVertex end) {
		readLock.lock();
		try {
			return ImmutableList
					.copyOf(DijkstraShortestPath.findPathBetween(sfg, start, end).getEdgeList());
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public ImmutableSet<StateVertex> getAllStates() {
		readLock.lock();
		try {
			return ImmutableSet.copyOf(sfg.vertexSet());
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public ImmutableSet<Eventable> getAllEdges() {
		readLock.lock();
		try {
			return ImmutableSet.copyOf(sfg.edgeSet());
		} finally {
			readLock.unlock();
		}
	}

	private StateVertex getStateInGraph(StateVertex state) {
		readLock.lock();
		try {
			for (StateVertex st : sfg.vertexSet()) {
				if (state.equals(st)) {
					return st;
				}
			}
			return null;
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public int getMeanStateStringSize() {
		readLock.lock();
		try {
			final Mean mean = new Mean();

			for (StateVertex state : sfg.vertexSet()) {
				mean.increment(state.getDom().getBytes().length);
			}

			return (int) mean.getResult();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public int getNumberOfStates() {
		return stateCounter.get();
	}

	StateVertex newStateFor(String url, String dom, String strippedDom, EmbeddedBrowser browser) {
		int id = nextStateNameCounter.incrementAndGet();
		return vertexFactory.newStateVertex(id, url, getNewStateName(id), dom, strippedDom,
				browser);
	}

	private String getNewStateName(int id) {
		return "state" + id;
	}

	@Override
	public List<List<GraphPath<StateVertex, Eventable>>> getAllPossiblePaths(StateVertex index) {
		final List<List<GraphPath<StateVertex, Eventable>>> results = Lists.newArrayList();

		final KShortestSimplePaths<StateVertex, Eventable> kPaths =
				new KShortestSimplePaths<>(this.sfg,
						Integer.MAX_VALUE);

		for (StateVertex state : getDeepStates(index)) {
			List<GraphPath<StateVertex, Eventable>> paths =
					kPaths.getPaths(index, state, Integer.MAX_VALUE);
			results.add(paths);
		}

		return results;
	}

	/**
	 * @param state The starting state.
	 * @return A list of the deepest states (states with no outgoing edges).
	 */
	private List<StateVertex> getDeepStates(StateVertex state) {
		final List<StateVertex> deepStates = new ArrayList<>();

		traverse(Sets.newHashSet(), deepStates, state);

		return deepStates;
	}

	private void traverse(Set<String> visitedStates, List<StateVertex> deepStates,
			StateVertex state) {
		visitedStates.add(state.getName());

		Set<StateVertex> outgoingSet = getOutgoingStates(state);

		if ((outgoingSet == null) || outgoingSet.isEmpty()) {
			deepStates.add(state);
		} else {
			if (cyclic(visitedStates, outgoingSet)) {
				deepStates.add(state);
			} else {
				for (StateVertex st : outgoingSet) {
					if (!visitedStates.contains(st.getName())) {
						traverse(visitedStates, deepStates, st);
					}
				}
			}
		}
	}

	private boolean cyclic(Set<String> visitedStates, Set<StateVertex> outgoingSet) {
		int i = 0;

		for (StateVertex state : outgoingSet) {
			if (visitedStates.contains(state.getName())) {
				i++;
			}
		}

		return i == outgoingSet.size();
	}

	@Override
	public ImmutableSet<StateVertex> getOutgoingStates(StateVertex stateVertex) {
		final Set<StateVertex> result = new HashSet<>();

		for (Eventable c : getOutgoingClickables(stateVertex)) {
			result.add(sfg.getEdgeTarget(c));
		}

		return ImmutableSet.copyOf(result);
	}
	
	@Override
	public boolean removeEdge(Eventable event) {
		this.expiredEdges.add(event);
		sfg.removeEdge(event);
		return true;
	}

	@Override
	public boolean restoreEdge(Eventable event) {
		if(expiredEdges.contains(event)) {
			expiredEdges.remove(event);
			return addEdge(event.getSourceStateVertex(), event.getTargetStateVertex(), event);
		}
		return false;
	}
	
	@Override
	public boolean removeState(StateVertex state) {
		if(this.expiredStates.contains(state)){
			LOG.warn("Trying to remove already expired state {} ", state.getId());
		}
		else {
			expiredStates.add(state);
			LOG.info("Removing {} and all its incoming edges ", state.getName());
		}
		ImmutableSet<Eventable> incomingEdges = getIncomingClickable(state);
		for(Eventable incomingEdge: incomingEdges) {
			removeEdge(incomingEdge);
		}
		return true;
	}

	@Override
	public boolean restoreState(StateVertex state) {
		if(expiredStates.contains(state)) {
			expiredStates.remove(state);
			LOG.info("Restoring {} and all its incoming edges ", state.getName());
		}
		else {
			LOG.debug("No need to restore unexpired state {}", state.getName());
			return false;
		}
		for(Eventable expired: expiredEdges) {
			if(expired.getTargetStateVertex().equals(state)) {
				long id = expired.getId();
				boolean added = addEdge(expired.getSourceStateVertex(), expired.getTargetStateVertex(), expired);
				if(!added) {
					LOG.debug("Retaining the id for consistency in tests");
					expired.setId(id);
				}
			}
		}
		return true;
	}
}

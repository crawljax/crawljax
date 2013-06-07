package com.crawljax.core.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DirectedMultigraph;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.UTF8;
import org.neo4j.kernel.Traversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.ExitNotifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The State-Flow Graph is a directed multigraph with states (StateVetex) on the vertices and
 * clickables (Eventables) on the edges. It stores the data in a graph database. The graph database
 * of choice for this version of StateFlowGraph class is neo4j community edition.
 */
@Singleton
public class DBSfgInMemoryPathFinder implements Serializable, StateFlowGraph {

	/**
	 * serial version for persisting the class
	 */
	private static final long serialVersionUID = 8765685878231494104L;

	private static final Logger LOG = LoggerFactory.getLogger(DBSfgInMemoryPathFinder.class
	        .getName());

	/**
	 * Intermediate counter for the number of states, not relaying on getAllStates.size() because of
	 * Thread-safety.
	 */
	private final AtomicInteger stateCounter = new AtomicInteger();
	private final AtomicInteger nextStateNameCounter = new AtomicInteger();
	private final ConcurrentMap<Integer, StateVertex> stateById;

	private final ExitNotifier exitNotifier;

	// The directory path for saving the graph database created by neo4j for
	// storing the state flow graph

	public static String DB_PATH = "target/state-flow-graph-db/atTime";

	// the connector or access point to the graph database

	private static GraphDatabaseService sfgDb;

	/**
	 * keys used for key-value pairs. The key-value pairs are the main place holders used in neo4j
	 * data model of a graph. The data is stored in edges and nodes of the graph as "properties"
	 */

	// the key for storing the persisted StateVertex objects in nodes
	public static final String SERIALIZED_STATE_VERTEX_IN_NODES = "Serialized State Vertex";

	// the key for storing the stripped DOM field of a StateVertex object
	// as a string in a node
	public static final String STRIPPED_DOM_IN_NODES = "Stripped Dom";

	// the key for storing the source key in an edge
	public static final String SOURCE_STRIPPED_DOM_IN_EDGES = "Source Stripped Dom";

	// the key for storing the target key in an edge
	public static final String TARGET_STRIPPED_DOM_IN_EDGES = "Target Stripped Dom";

	// the key for storing the persisted Eventable objects
	public static final String SERIALIZED_CLICKABLE_IN_EDGES = "Serialized Clickable";

	// the key for storing the to string Eventable objects
	public static final String CLICKABLE_IN_EDGES = "Clickable";

	// the combined key for storing the persisted triples of
	// (source StateVertex,Eventable to string ,target StateVertex) saved in a
	// string array
	// of length 3
	// this is used for indexing edges
	public static final String SOURCE_CLICKABLE_TARGET_IN_EDGES_FOR_UNIQUE_INDEXING =
	        "Source-Clickable-Target Triple";

	// the url key
	public static final String URL_IN_NODES = "URL";

	// the state name key
	public static final String STATE_NAME_IN_NODES = "State Name";

	// the state id key
	public static final String STATE_ID_IN_NODES = "State ID";

	// the key for DOM
	public static final String DOM_IN_NODES = "DOM";

	//

	// the id used for the for node indexer object
	public static final String NODES_INDEX_NAME = "nodesIndex";

	// the id used for the for edge indexer object
	public static final String EDGES_INDEX_NAME = "edgesIndex";

	// for building an indexing structure within the graph for quick access to
	// nodes and edges.
	public static Node structuralIndexer;

	// index manager
	private static IndexManager indexManager;

	// indexing data structures for fast retrieval
	private static Index<Node> nodeIndex;
	private static RelationshipIndex edgesIndex;

	private static final int SOURCE_VERTEX_INDEX = 0;
	private static final int TARGET_VERTEX_INDEX = 2;
	private static final int CLICKABLE_INDEX = 1;

	/**
	 * The constructor.
	 * 
	 * @param initialState
	 *            the state to start from.
	 */
	@Inject
	public DBSfgInMemoryPathFinder(ExitNotifier exitNotifier) {
		this.exitNotifier = exitNotifier;
		stateById = Maps.newConcurrentMap();

		// creating the graph database

		// fresh is used to ensure that every time we run the program a
		// clean empty database is used for storing the data

		long fresh = System.nanoTime();
		fresh /= 1000;
		String path = DB_PATH + fresh;
		sfgDb = new GraphDatabaseFactory().newEmbeddedDatabase(path);

		// for quick indexing and retrieval of nodes. This data structure is a
		// additional
		// capability beside
		// the main graph data structures which is comprised of nodes and edges

		nodeIndex = sfgDb.index().forNodes(NODES_INDEX_NAME);

		// again similar to nodeIndex this is a cross indexing of the edges for
		// fast retrieval

		edgesIndex = sfgDb.index().forRelationships(EDGES_INDEX_NAME);

		Node indexNode = null;
		Transaction tx = sfgDb.beginTx();
		try {
			indexNode = sfgDb.createNode();
			indexNode.setProperty("type", "indexing");

			tx.success();
		} finally {
			tx.finish();
		}

		if (indexNode == null) {
			LOG.warn("creating indexNode unsuccessful! exiting now!");
			System.exit(1);
		}

		DBSfgInMemoryPathFinder.structuralIndexer = indexNode;
		LOG.debug("Initialized the stateflowgraph");

		// adding a shutdown hook to ensure the database will be shut down even
		// if the program breaks

		registerShutdownHook(sfgDb);

	}

	@Override
	public StateVertex putIfAbsent(StateVertex stateVertix) {
		return putIfAbsent(stateVertix, true);
	}

	/**
	 * Adds a state (as a vertix) to the State-Flow Graph if not already present. It adds the
	 * specified vertex, v, to this graph if this graph contains no vertex u such that u.equals(v).
	 * If this graph already contains such vertex, the call leaves this graph unchanged and returns
	 * false. In combination with the restriction on constructors, this ensures that graphs never
	 * contain duplicate vertices.
	 * 
	 * @param stateVertix
	 *            the state to be added.
	 * @param correctName
	 *            if true the name of the state will be corrected according to the internal state
	 *            counter.
	 * @return the clone if one is detected <code>null</code> otherwise.
	 */

	@Override
	@GuardedBy("sfgDb")
	public StateVertex putIfAbsent(StateVertex state, boolean correctName) {

		// the node to be added to the graph and then filled with the the data
		// that
		// that need to be stored as the state in the database

		Node toBeAddedNode;

		// for saving the returned result of the method putIfAbsentNode
		// it will be null if the state is not already in the graph

		Node alreadyEsixts;

		synchronized (sfgDb) {
			// starting the transaction
			Transaction tx = sfgDb.beginTx();
			try {

				// adding the container for the state which is going to be added
				// to the graph database

				toBeAddedNode = sfgDb.createNode();

				// indexing the state in Index manager. the key that we are
				// using
				// for indexing is the stripped_dom field. This, in particular,
				// is
				// compliance with the domChanged method in the class Crawler

				alreadyEsixts = putIfAbsentNode(toBeAddedNode,
				        UTF8.decode(UTF8.encode(state.getStrippedDom())));

				if (alreadyEsixts != null) {
					// the state is already indexed
					LOG.debug("putIfAbsent: Graph already contained vertex {}",
					        state);

					// because the state already exists in the graph the
					// transaction
					// is marked for being rolled back
					tx.failure();
				} else {

					// correcting the name
					stateById.put(state.getId(), state);
					int count = stateCounter.incrementAndGet();
					exitNotifier.incrementNumberOfStates();
					LOG.debug("Number of states is now {}", count);
					if (correctName) {
						correctStateName(state);
					}

					// serializing the state
					byte[] serializedSV = serializeStateVertex(state);

					// adding the state property which is the main data we store
					// for
					// each node (i.e. each StateVertex)
					// the serialized stateVertex and the Stripped DOM are used
					// for crawling
					// purposes too!
					toBeAddedNode.setProperty(SERIALIZED_STATE_VERTEX_IN_NODES,
					        serializedSV);
					toBeAddedNode.setProperty(STRIPPED_DOM_IN_NODES,
					        UTF8.decode((UTF8.encode(state.getStrippedDom()))));

					// adding textual data which is not used for crawling
					// porpose but are useful for text based
					// querie

					// the url of the state
					String url = state.getUrl();
					if (url != null) {
						toBeAddedNode.setProperty(URL_IN_NODES, state.getUrl());
					} else {
						toBeAddedNode.setProperty(URL_IN_NODES, "null");
					}

					// the DOM
					String dom = state.getUrl();
					if (dom != null) {
						toBeAddedNode.setProperty(DOM_IN_NODES, state.getDom());
					} else {
						toBeAddedNode.setProperty(DOM_IN_NODES, "null");
					}

					// the name of the state
					String name = state.getName();
					if (name != null) {
						toBeAddedNode.setProperty(STATE_NAME_IN_NODES,
						        state.getName());
					} else {
						toBeAddedNode.setProperty(STATE_NAME_IN_NODES, "null");
					}

					// the id of the state
					int id = state.getId();
					toBeAddedNode.setProperty(STATE_ID_IN_NODES, id);

					// flagging successful transaction
					tx.success();
				}
			} finally {
				tx.finish();
			}

			if (alreadyEsixts == null) {
				// the state was not found in the database
				// so it was stored in the database
				return null;
			} else {

				// // Return the state retrieved from database in case the state
				// is
				// // already present in the graph
				// return (StateVertex) deserializeStateVertex((byte[])
				// alreadyEsixts
				// .getProperty(STATE_VERTEX_KEY));
				return state;
			}
		}

	}

	private void correctStateName(StateVertex stateVertix) {
		// the -1 is for the "index" state.
		int totalNumberOfStates = this.getAllStatesAndPartialStates().size() - 1;
		String correctedName = makeStateName(totalNumberOfStates);
		if (!"index".equals(stateVertix.getName())
		        && !stateVertix.getName().equals(correctedName)) {
			LOG.info("Correcting state name from {}  to {}",
			        stateVertix.getName(), correctedName);
			stateVertix.setName(correctedName);
		}
	}

	/**
	 * @param id
	 *            The ID of the state
	 * @return The state if found or <code>null</code>.
	 */
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
	 * @param sourceVert
	 *            source vertex of the edge.
	 * @param targetVert
	 *            target vertex of the edge.
	 * @param clickable
	 *            the clickable edge to be added to this graph.
	 * @return true if this graph did not already contain the specified edge.
	 */

	@Override
	@GuardedBy("sfgDb")
	public boolean addEdge(StateVertex sourceVert, StateVertex targetVert,
	        Eventable eventable) {
		boolean exists = false;

		// this is done automatically with jgraphT

		eventable.setSourceStateVertex(sourceVert);
		eventable.setTargetStateVertex(targetVert);

		byte[] serializedEventable = serializeEventable(eventable, 1);

		// eventable.setSourceStateVertex(sourceVert);
		// eventable.setTargetStateVertex(targetVert);

		Relationship toBeAddedEdge = null;
		Relationship alreadyExists = null;

		String[] combinedEdgeKey = new String[3];
		combinedEdgeKey[SOURCE_VERTEX_INDEX] = UTF8.decode(UTF8
		        .encode(sourceVert.getStrippedDom()));
		combinedEdgeKey[CLICKABLE_INDEX] = UTF8.decode(UTF8.encode(eventable
		        .toString()));

		combinedEdgeKey[TARGET_VERTEX_INDEX] = UTF8.decode(UTF8
		        .encode(targetVert.getStrippedDom()));
		// combinedEdgeKey[1] = UTF8.decode(serializedEventable);

		synchronized (sfgDb) {
			Transaction tx = sfgDb.beginTx();
			try {

				Node sourceNode = getNodeFromDB(UTF8.decode(UTF8
				        .encode(sourceVert.getStrippedDom()))); // nodeIndex.get(STRIPPED_DOM_KEY,
				// sourceVert.getStrippedDom().getBytes()).getSingle();
				Node targetNode = getNodeFromDB(UTF8.decode(UTF8
				        .encode(targetVert.getStrippedDom())));// nodeIndex.get(STRIPPED_DOM_KEY,
				// targetVert.getStrippedDom().getBytes()).getSingle();
				toBeAddedEdge = sourceNode.createRelationshipTo(targetNode,
				        RelTypes.TRANSITIONS_TO);

				// adding the new edge to the index. it returns null if the edge
				// is
				// successfully added
				// and returns the found edge if and identical edge already
				// exists
				// in the index.
				// alreadyExists = edgesIndex.putIfAbsent(toBeAddedEdge,
				// EDGE_COMBNINED_KEY, combinedEdgeKey);
				alreadyExists = edgePutIfAbsent(toBeAddedEdge,
				        SOURCE_CLICKABLE_TARGET_IN_EDGES_FOR_UNIQUE_INDEXING,
				        combinedEdgeKey);

				if (alreadyExists != null) {
					exists = true;
					tx.failure();
				} else {
					exists = false;

					toBeAddedEdge.setProperty(SERIALIZED_CLICKABLE_IN_EDGES,
					        serializedEventable);

					toBeAddedEdge.setProperty(CLICKABLE_IN_EDGES,
					        UTF8.decode(UTF8.encode(eventable.toString())));

					toBeAddedEdge.setProperty(SOURCE_STRIPPED_DOM_IN_EDGES,
					        UTF8.decode((UTF8.encode(sourceVert
					                .getStrippedDom()))));
					toBeAddedEdge
					        .setProperty(TARGET_STRIPPED_DOM_IN_EDGES, UTF8
					                .decode(UTF8.encode(targetVert
					                        .getStrippedDom())));
				}
				tx.success();
			} finally {
				tx.finish();

			}

			if (exists) {

				return false;
			} else {
				return true;

			}
		}

	}

	// it sounds like the name is corrected based on the number of states read
	// from the graph
	// but it's safer to use the atomic integer though we might end up having
	// sparse name space, i.e. we end up having gaps between the numbers
	// associated to startes.

	/**
	 * @return the string representation of the graph.
	 */
	@Override
	public String toString() {
		return buildJgraphT().toString();
	}

	/**
	 * Returns a set of all clickables outgoing from the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the outgoing edges (clickables) of the stateVertix.
	 */
	@Override
	public ImmutableSet<Eventable> getOutgoingClickables(StateVertex stateVertix) {

		// todo

		// retrieve source and targets state too!
		Set<Eventable> outgoing = new HashSet<Eventable>();
		Node state = getNodeFromDB(UTF8.decode(UTF8.encode(stateVertix
		        .getStrippedDom())));
		for (Relationship edge : state.getRelationships(
		        RelTypes.TRANSITIONS_TO, Direction.OUTGOING)) {
			byte[] serializedEvantable = (byte[]) edge
			        .getProperty(SERIALIZED_CLICKABLE_IN_EDGES);
			Eventable eventable = deserializeEventable(serializedEvantable, 1);
			outgoing.add(eventable);
		}

		return ImmutableSet.copyOf(outgoing);
	}

	/**
	 * Returns a set of all edges incoming into the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertix.
	 * @return a set of the incoming edges (clickables) of the stateVertix.
	 */
	@Override
	public ImmutableSet<Eventable> getIncomingClickable(StateVertex stateVertix) {

		// todo

		// retrieve source and targets state too!

		Set<Eventable> incoming = new HashSet<Eventable>();
		Node state = getNodeFromDB(UTF8.decode(UTF8.encode(stateVertix
		        .getStrippedDom())));

		for (Relationship edge : state.getRelationships(
		        RelTypes.TRANSITIONS_TO, Direction.INCOMING)) {
			byte[] serializedEvantable = (byte[]) edge
			        .getProperty(SERIALIZED_CLICKABLE_IN_EDGES);
			Eventable eventable = deserializeEventable(serializedEvantable, 1);
			incoming.add(eventable);
		}

		return ImmutableSet.copyOf(incoming);

	}

	/**
	 * Returns the set of outgoing states.
	 * 
	 * @param stateVertix
	 *            the state.
	 * @return the set of outgoing states from the stateVertix.
	 */
	@Override
	public ImmutableSet<StateVertex> getOutgoingStates(StateVertex stateVertix) {

		final Set<StateVertex> outgoing = new HashSet<StateVertex>();

		Node sourceNode = getNodeFromDB(UTF8.decode(UTF8.encode(stateVertix
		        .getStrippedDom())));

		for (Relationship edge : sourceNode.getRelationships(
		        RelTypes.TRANSITIONS_TO, Direction.OUTGOING)) {
			Node endNode = edge.getEndNode();
			byte[] serializedState = (byte[]) endNode
			        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);
			StateVertex targetState = deserializeStateVertex(serializedState);
			outgoing.add(targetState);
		}

		return ImmutableSet.copyOf(outgoing);

	}

	/**
	 * @param clickable
	 *            the edge.
	 * @return the target state of this edge.
	 */
	@Override
	public StateVertex getTargetState(Eventable clickable) {

		// to do
		// you can get source and target index and look for the eventable
		// another way!

		byte[] serializedEventable = serializeEventable(clickable, 1);

		Relationship edge = edgesIndex.get(SERIALIZED_CLICKABLE_IN_EDGES,
		        serializedEventable).getSingle();

		Node targetNode = edge.getEndNode();

		byte[] srializedState = (byte[]) targetNode
		        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);
		StateVertex target = deserializeStateVertex(srializedState);

		return target;
	}

	/**
	 * Is it possible to go from s1 -> s2?
	 * 
	 * @param source
	 *            the source state.
	 * @param target
	 *            the target state.
	 * @return true if it is possible (edge exists in graph) to go from source to target.
	 */
	@Override
	@GuardedBy("sfgDb")
	public boolean canGoTo(StateVertex source, StateVertex target) {

		synchronized (sfgDb) {
			Node sourceNode = getNodeFromDB(UTF8.decode(UTF8.encode(source
			        .getStrippedDom())));
			for (Relationship edge : sourceNode.getRelationships(
			        RelTypes.TRANSITIONS_TO, Direction.OUTGOING)) {

				Node targetNode = edge.getEndNode();
				byte[] serializedNode = (byte[]) targetNode
				        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);

				StateVertex ts = deserializeStateVertex(serializedNode);
				if (ts.equals(target)) {
					return true;
				}
			}

			// searching for back links
			Node tagetNode = getNodeFromDB(UTF8.decode(UTF8.encode(target
			        .getStrippedDom())));
			for (Relationship edge : tagetNode.getRelationships(
			        RelTypes.TRANSITIONS_TO, Direction.OUTGOING)) {

				Node srcNode = edge.getEndNode();
				byte[] serializedNode = (byte[]) srcNode
				        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);

				StateVertex ts = deserializeStateVertex(serializedNode);
				if (ts.equals(source)) {
					return true;
				}
			}

			return false;
		}
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

	public ImmutableList<Eventable> getShortestPath3(StateVertex start,
	        StateVertex end) {

		DirectedGraph<StateVertex, Eventable> sfg = buildJgraphT();
		StateVertex startFromJgraphT = getStateInJgrpahT(start, sfg);
		StateVertex endFromJgraphT = getStateInJgrpahT(end, sfg);

		return ImmutableList.copyOf(DijkstraShortestPath.findPathBetween(sfg,
		        startFromJgraphT, endFromJgraphT));
	}

	@Override
	public ImmutableList<Eventable> getShortestPath(StateVertex start,
	        StateVertex end) {

		List<Eventable> shortestPath = new ArrayList<Eventable>();
		Node startNode = getNodeFromDB(start.getStrippedDom());
		Node endNode = getNodeFromDB(end.getStrippedDom());

		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(Traversal
		        .pathExpanderForTypes(RelTypes.TRANSITIONS_TO,
		                Direction.OUTGOING), Integer.MAX_VALUE);

		Iterable<Path> paths = finder.findAllPaths(startNode, endNode);
		if (paths.iterator().hasNext()) {
			Path path = paths.iterator().next();

			Iterator<PropertyContainer> container = path.iterator();

			// skipping the first node! we already have it!
			if (container.hasNext()) {
				container.next();
			}

			while (container.hasNext()) {

				// retrieving the edge
				Relationship edge = (Relationship) container.next();
				byte[] serEventable = (byte[]) edge
				        .getProperty(SERIALIZED_CLICKABLE_IN_EDGES);
				Eventable eventable = deserializeEventable(serEventable, 0);
				// adding the source state of the edge!
				eventable.setSourceStateVertex(start);

				// retrieving the target node

				Node endNodeForThisEdge = (Node) container.next();
				byte[] serEndNode = (byte[]) endNodeForThisEdge
				        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);
				StateVertex endingState = deserializeStateVertex(serEndNode);

				// adding the target state
				eventable.setTargetStateVertex(endingState);

				// adding the edge to the shortest path
				shortestPath.add(eventable);

				start = endingState;

			}

		}

		return ImmutableList.copyOf(shortestPath);
	}

	public ImmutableList<Eventable> getShortestPath2(StateVertex start,
	        StateVertex end) {

		DirectedGraph<StateVertex, Eventable> sfg = buildJgraphTLight();
		int stateId = start.getId();
		StateVertex startFromJgraphT = new StateVertex(stateId, stateId + "",
		        stateId + "", stateId + "", stateId + "");
		startFromJgraphT = getStateInJgrpahT(startFromJgraphT, sfg);

		stateId = end.getId();
		StateVertex endFromJgraphT = new StateVertex(stateId, stateId + "",
		        stateId + "", stateId + "", stateId + "");

		endFromJgraphT = getStateInJgrpahT(end, sfg);

		return ImmutableList.copyOf(DijkstraShortestPath.findPathBetween(sfg,
		        startFromJgraphT, endFromJgraphT));
	}

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */
	public ImmutableSet<StateVertex> getAllStatesAndPartialStates() {

		final Set<StateVertex> allStates = new HashSet<StateVertex>();

		// for (Node node : nodeIndex.query(STRIPPED_DOM_KEY, "*")) {
		for (Relationship relationship : structuralIndexer.getRelationships(
		        Direction.OUTGOING, RelTypes.INDEXES)) {

			Node node = relationship.getEndNode();

			byte[] serializedNode = (byte[]) node.getProperty(
			        SERIALIZED_STATE_VERTEX_IN_NODES, null);

			if (serializedNode != null) {
				StateVertex state = deserializeStateVertex(serializedNode);
				allStates.add(state);
			} else {
				allStates.add(new StateVertex(Integer.MAX_VALUE, "mock",
				        "mock", "mock", "mock"));

			}

		}

		return ImmutableSet.copyOf(allStates);
	}

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */

	@Override
	public ImmutableSet<StateVertex> getAllStates() {

		final Set<StateVertex> allStates = new HashSet<StateVertex>();

		// for (Node node : nodeIndex.query(STRIPPED_DOM_KEY, "*")) {
		for (Relationship relationship : structuralIndexer.getRelationships(
		        Direction.OUTGOING, RelTypes.INDEXES)) {

			Node node = relationship.getEndNode();

			byte[] serializedNode = (byte[]) node.getProperty(
			        SERIALIZED_STATE_VERTEX_IN_NODES, null);

			if (serializedNode != null) {
				StateVertex state = deserializeStateVertex(serializedNode);
				allStates.add(state);
			} else {
				allStates.add(new StateVertex(Integer.MAX_VALUE, "mock",
				        "mock", "mock", "mock"));

			}

		}

		return ImmutableSet.copyOf(allStates);
	}

	public ImmutableSet<StateVertex> getAllStatesLight() {

		final Set<StateVertex> allStates = new HashSet<StateVertex>();

		// for (Node node : nodeIndex.query(STRIPPED_DOM_KEY, "*")) {
		for (Relationship relationship : structuralIndexer.getRelationships(
		        Direction.OUTGOING, RelTypes.INDEXES)) {

			Node node = relationship.getEndNode();

			int stateId = (int) node.getProperty(STATE_ID_IN_NODES,
			        Integer.MAX_VALUE);

			allStates.add(new StateVertex(stateId, stateId + "", stateId + "",
			        stateId + "", stateId + ""));

		}

		return ImmutableSet.copyOf(allStates);
	}

	/**
	 * Return all the edges in the StateFlowGraph.
	 * 
	 * @return a Set of all edges in the StateFlowGraph
	 */
	@Override
	public ImmutableSet<Eventable> getAllEdges() {

		final Set<Eventable> all = new HashSet<Eventable>();

		Transaction tx = sfgDb.beginTx();
		try {

			// for (Relationship edge : edgesIndex.query(EDGE_COMBNINED_KEY,
			// "*")) {

			for (Relationship relationship : structuralIndexer
			        .getRelationships(Direction.OUTGOING, RelTypes.INDEXES)) {
				for (Relationship edge : relationship.getEndNode()
				        .getRelationships(Direction.OUTGOING,
				                RelTypes.TRANSITIONS_TO)) {

					byte[] serializededge = (byte[]) edge
					        .getProperty(SERIALIZED_CLICKABLE_IN_EDGES);
					Eventable eventable = deserializeEventable(serializededge,
					        1);

					Node startNode = edge.getStartNode();
					byte[] serializedStartNode = (byte[]) startNode
					        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);
					StateVertex startState = deserializeStateVertex(serializedStartNode);
					eventable.setSourceStateVertex(startState);

					Node endNode = edge.getEndNode();
					byte[] serializedEndNode = (byte[]) endNode
					        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);
					StateVertex endState = deserializeStateVertex(serializedEndNode);
					eventable.setTargetStateVertex(endState);

					all.add(eventable);

				}
			}
			tx.success();
		} finally {
			tx.finish();
		}

		return ImmutableSet.copyOf(all);
	}

	/**
	 * Retrieve the copy of a state from the StateFlowGraph for a given StateVertix. Basically it
	 * performs v.equals(u).
	 * 
	 * @param state
	 *            the StateVertix to search
	 * @return the copy of the StateVertix in the StateFlowGraph where v.equals(u)
	 */
	private StateVertex getStateInGraph(StateVertex state) {
		Set<StateVertex> states = getAllStates();

		for (StateVertex st : states) {
			if (state.equals(st)) {
				return st;
			}
		}

		return null;
	}

	/**
	 * @return Dom string average size (byte).
	 */
	@Override
	public int getMeanStateStringSize() {
		final Mean mean = new Mean();

		for (StateVertex state : getAllStates()) {
			mean.increment(state.getDomSize());
		}

		return (int) mean.getResult();
	}

	/**
	 * @param state
	 *            The starting state.
	 * @return A list of the deepest states (states with no outgoing edges).
	 */
	@Override
	public List<StateVertex> getDeepStates(StateVertex state) {
		final Set<String> visitedStates = new HashSet<String>();
		final List<StateVertex> deepStates = new ArrayList<StateVertex>();

		traverse(visitedStates, deepStates, state);

		return deepStates;
	}

	private void traverse(Set<String> visitedStates,
	        List<StateVertex> deepStates, StateVertex state) {
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

	private boolean cyclic(Set<String> visitedStates,
	        Set<StateVertex> outgoingSet) {
		int i = 0;

		for (StateVertex state : outgoingSet) {
			if (visitedStates.contains(state.getName())) {
				i++;
			}
		}

		return i == outgoingSet.size();
	}

	/**
	 * This method returns all possible paths from the index state using the Kshortest paths.
	 * 
	 * @param index
	 *            the initial state.
	 * @return a list of GraphPath lists.
	 */

	@Override
	public List<List<GraphPath<StateVertex, Eventable>>> getAllPossiblePaths(
	        StateVertex index) {
		final List<List<GraphPath<StateVertex, Eventable>>> results = Lists
		        .newArrayList();

		DirectedGraph<StateVertex, Eventable> sfg = this.buildJgraphT();

		StateVertex indexFromGraph = getStateInJgrpahT(index, sfg);
		if (indexFromGraph == null) {
			LOG.warn("state not found in JgraphT: {} ", index.getName());
			System.exit(1);
		}

		final KShortestPaths<StateVertex, Eventable> kPaths = new KShortestPaths<>(
		        sfg, indexFromGraph, Integer.MAX_VALUE);

		for (StateVertex state : getDeepStates(index)) {

			StateVertex stateFromGraph = getStateInJgrpahT(state, sfg);
			if (stateFromGraph == null) {
				LOG.warn("state not found in JgraphT {}", state.getName());
				System.exit(1);
			}

			try {
				List<GraphPath<StateVertex, Eventable>> paths = kPaths
				        .getPaths(stateFromGraph);
				results.add(paths);
			} catch (Exception e) {
				// TODO Stefan; which Exception is catched here???Can this be
				// removed?
				LOG.error("Error with " + state.toString(), e);
			}

		}

		return results;
	}

	/**
	 * Return the name of the (new)State. By using the AtomicInteger the stateCounter is thread-safe
	 * 
	 * @return State name the name of the state
	 */

	/**
	 * Return the name of the (new)State. By using the AtomicInteger the stateCounter is thread-safe
	 * 
	 * @return State name the name of the state
	 */
	@Override
	public String getNewStateName(int id) {
		String state = makeStateName(id);
		return state;
	}

	@Override
	public int getNextStateId() {
		return nextStateNameCounter.incrementAndGet();
	}

	/**
	 * Make a new state name given its id. Separated to get a central point when changing the names
	 * of states. The automatic state names start with "state" and guided ones with "guide".
	 * 
	 * @param id
	 *            the id where this name needs to be for.
	 * @return the String containing the new name.
	 */
	private String makeStateName(int id) {
		return "state" + id;
	}

	/**
	 * @return The number of states, currently in the graph.
	 */
	@Override
	public int getNumberOfStates() {
		return stateCounter.get();
	}

	public static void setSfgDb(GraphDatabaseService sfgDb) {
		DBSfgInMemoryPathFinder.sfgDb = sfgDb;
	}

	public static GraphDatabaseService getSfgDb() {
		return sfgDb;
	}

	private Relationship edgePutIfAbsent(Relationship toBeAddedEdge,
	        String key, String[] combinedEdgeKey) {

		for (Relationship edge : edgesIndex.query(key, "*")) {

			String sourceDom = (String) edge
			        .getProperty(SOURCE_STRIPPED_DOM_IN_EDGES);
			String targetDom = (String) edge
			        .getProperty(TARGET_STRIPPED_DOM_IN_EDGES);
			String clickableToString = (String) edge
			        .getProperty(CLICKABLE_IN_EDGES);

			if (sourceDom.equals(combinedEdgeKey[SOURCE_VERTEX_INDEX])
			        && targetDom.equals(combinedEdgeKey[TARGET_VERTEX_INDEX])
			        && clickableToString
			                .equals(combinedEdgeKey[CLICKABLE_INDEX])) {
				return edge;

			}
		}

		edgesIndex.add(toBeAddedEdge, key, combinedEdgeKey);

		return null;
	}

	private Node getNodeFromDB(String strippedDom) {
		for (Node node : nodeIndex.query(STRIPPED_DOM_IN_NODES, "*")) {

			byte[] serializedNode = (byte[]) node
			        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);

			StateVertex state = deserializeStateVertex(serializedNode);

			String newDom = strippedDom;
			String prev = state.getStrippedDom();

			if (newDom.equals(prev)) {
				return node;

			}

		}

		return null;

	}

	private Node putIfAbsentNode(Node toBeAddedNode, String strippedDom) {

		for (Node node : nodeIndex.query(STRIPPED_DOM_IN_NODES, "*")) {

			byte[] serializedNode = (byte[]) node
			        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES);

			// retrieve the stripped dome directly rahter than from the
			// serializedState
			StateVertex state = deserializeStateVertex(serializedNode);

			String newDom = strippedDom;
			String prev = state.getStrippedDom();

			if (newDom.equals(prev)) {
				return node;

			}

		}
		nodeIndex.add(toBeAddedNode, STRIPPED_DOM_IN_NODES, strippedDom);
		structuralIndexer.createRelationshipTo(toBeAddedNode, RelTypes.INDEXES);

		return null;
	}

	/**
	 * Registering a shutdown hook for the database instance so as to shut it down nicely when the
	 * VM exits
	 * 
	 * @param graphDatabaseService
	 *            the database for which a shutdown hook will be registered
	 */
	private static void registerShutdownHook(
	        final GraphDatabaseService graphDatabaseService) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDatabaseService.shutdown();
			}
		});
	}

	public static void setIndexManager(IndexManager index) {
		DBSfgInMemoryPathFinder.indexManager = index;
	}

	public static IndexManager getIndexManager() {
		return indexManager;
	}

	public static void setNodeIndex(Index<Node> nodeIndex) {
		DBSfgInMemoryPathFinder.nodeIndex = nodeIndex;
	}

	public static void setEdgesIndex(RelationshipIndex edgesIndex) {
		DBSfgInMemoryPathFinder.edgesIndex = edgesIndex;
	}

	/**
	 * @param stateVertex
	 *            the state which will be serialized
	 * @return a byte array containing persisted byte array of the stateVertex object.
	 */

	public static byte[] serializeStateVertex(StateVertex stateVertex) {

		// result holder
		byte[] serializedStateVertex = null;

		// this an output stream that does not require writing to the file and
		// instead
		// the output stream is stored in a buffer
		// we use this class to utilize the Java serialization api which writes
		// and reads
		// objects to and from streams

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			ObjectOutputStream oos = new ObjectOutputStream(baos);

			// Serializing the stateVertex object to the stream

			oos.writeObject(stateVertex);

			// converting the byte array to UTF-8 string for portability reasons

			serializedStateVertex = baos.toByteArray();

			// closing streams

			oos.close();
			baos.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return serializedStateVertex;
	}

	public static StateVertex deserializeStateVertex(
	        byte[] serializedStateVertex) {
		// the returned value

		StateVertex deserializedSV = null;

		try {

			ByteArrayInputStream bais = new ByteArrayInputStream(
			        serializedStateVertex);

			ObjectInputStream ois = new ObjectInputStream(bais);

			deserializedSV = (StateVertex) ois.readObject();

			// Closing streams

			ois.close();
			bais.close();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return deserializedSV;

	}

	public static byte[] serializeEventable(Eventable eventable, int i) {
		byte[] result = SerializationUtils.serialize(eventable);
		return result;
	}

	public static Eventable deserializeEventable(byte[] serializedEventable,
	        int i) {
		Eventable results = (Eventable) SerializationUtils
		        .deserialize(serializedEventable);

		return results;

	}

	public static byte[] serializeEventable(Eventable eventable) {

		byte[] serializedEventable = null;

		// this an output stream that does not require writing to the file and
		// instead
		// the output stream is stored in a buffer
		// we use this class to utilize the Java serialization api which writes
		// and reads
		// object to and from streams

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			ObjectOutputStream oos = new ObjectOutputStream(baos);

			// serializing the Eventable object to the stream

			oos.writeObject(eventable);

			// converting the byte array to UTF-8 string for portability reasons

			serializedEventable = baos.toByteArray();

			// closing streams

			oos.close();
			baos.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return serializedEventable;
	}

	public static Eventable deserializeEventable(byte[] serializedEventable) {
		// the returned value

		Eventable deserializedEventable = null;

		try {

			ByteArrayInputStream bais = new ByteArrayInputStream(
			        serializedEventable);

			ObjectInputStream ois = new ObjectInputStream(bais);

			deserializedEventable = (Eventable) ois.readObject();

			// Closing streams

			ois.close();
			bais.close();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return deserializedEventable;

	}

	public DirectedGraph<StateVertex, Eventable> buildJgraphT() {
		DirectedGraph<StateVertex, Eventable> sfg;
		sfg = new DirectedMultigraph<>(Eventable.class);

		// inserting states
		for (StateVertex state : getAllStates()) {
			if (insertStateInJgraphT(state, sfg) != null) {
				LOG.warn("duplicate state found");
				System.exit(1);
			}
		}

		// inserting edges
		Transaction tx = sfgDb.beginTx();
		try {
			for (Relationship relationship : structuralIndexer
			        .getRelationships(Direction.OUTGOING, RelTypes.INDEXES)) {
				for (Relationship edge : relationship.getEndNode()
				        .getRelationships(Direction.OUTGOING,
				                RelTypes.TRANSITIONS_TO)) {

					byte[] serializededge = (byte[]) edge
					        .getProperty(SERIALIZED_CLICKABLE_IN_EDGES);
					Eventable eventable = deserializeEventable(serializededge,
					        1);

					Node sourceNode = edge.getStartNode();
					byte[] serializedSourceStateVertex = (byte[]) sourceNode
					        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES, null);
					StateVertex sourceStateVertex =
					        deserializeStateVertex(serializedSourceStateVertex);

					Node targetNode = edge.getEndNode();
					byte[] serializedTargetStateVertex = (byte[]) targetNode
					        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES, null);
					StateVertex targetStateVertex =
					        deserializeStateVertex(serializedTargetStateVertex);

					if ((eventable == null) || (sourceStateVertex == null)
					        || (targetStateVertex == null)) {
						LOG.warn("eventable was not retrieved correctly!");
						System.exit(1);
					} else {
						if (sfg.addEdge(sourceStateVertex, targetStateVertex,
						        eventable)) {
							eventable.setSourceStateVertex(sourceStateVertex);
							eventable.setTargetStateVertex(targetStateVertex);

							// LOG.info("edge added succesfully to JgraphT");
						} else {
							LOG.warn("edge insertion failed");
							System.exit(1);
						}
					}

				}
			}
			tx.success();
		} finally {
			tx.finish();
		}

		return sfg;
	}

	public DirectedGraph<StateVertex, Eventable> buildJgraphTLight() {
		DirectedGraph<StateVertex, Eventable> sfg;
		sfg = new DirectedMultigraph<>(Eventable.class);

		// inserting states
		for (StateVertex state : getAllStatesLight()) {
			if (insertStateInJgraphT(state, sfg) != null) {
				LOG.warn("duplicate state found");
				System.exit(1);
			}
		}

		// inserting edges
		Transaction tx = sfgDb.beginTx();
		try {
			for (Relationship relationship : structuralIndexer
			        .getRelationships(Direction.OUTGOING, RelTypes.INDEXES)) {
				for (Relationship edge : relationship.getEndNode()
				        .getRelationships(Direction.OUTGOING,
				                RelTypes.TRANSITIONS_TO)) {

					byte[] serializededge = (byte[]) edge
					        .getProperty(SERIALIZED_CLICKABLE_IN_EDGES);
					Eventable eventable = deserializeEventable(serializededge,
					        1);

					Node sourceNode = edge.getStartNode();
					int sourceStateId = (int) sourceNode.getProperty(
					        STATE_ID_IN_NODES, Integer.MAX_VALUE);
					StateVertex sourceStateVertex = new StateVertex(
					        sourceStateId, sourceStateId + "", sourceStateId
					                + "", sourceStateId + "", sourceStateId
					                + "");

					Node targetNode = edge.getEndNode();

					int targetStateId = (int) targetNode.getProperty(
					        STATE_ID_IN_NODES, Integer.MAX_VALUE);
					StateVertex targetStateVertex = new StateVertex(
					        targetStateId, targetStateId + "", targetStateId
					                + "", targetStateId + "", targetStateId
					                + "");

					if ((eventable == null) || (sourceStateVertex == null)
					        || (targetStateVertex == null)) {
						LOG.warn("eventable was not retrieved correctly!");
						System.exit(1);
					} else {
						if (sfg.addEdge(sourceStateVertex, targetStateVertex,
						        eventable)) {
							eventable.setSourceStateVertex(sourceStateVertex);
							eventable.setTargetStateVertex(targetStateVertex);

							// LOG.info("edge added succesfully to JgraphT");
						} else {
							LOG.warn("edge insertion failed");
							System.exit(1);
						}
					}

				}
			}
			tx.success();
		} finally {
			tx.finish();
		}

		return sfg;
	}

	private StateVertex insertStateInJgraphT(StateVertex stateVertix,
	        DirectedGraph<StateVertex, Eventable> sfg) {
		boolean added = sfg.addVertex(stateVertix);
		if (added) {
			// LOG.debug("state added successfully to JgraphT");
			return null;
		} else {
			// Graph already contained the vertix
			// LOG.debug("Graph already contained vertex {}",
			// stateVertix.getName());
			return this.getStateInGraph(stateVertix);
		}

	}

	/**
	 * Retrieve the copy of a state from the StateFlowGraph for a given StateVertix. Basically it
	 * performs v.equals(u).
	 * 
	 * @param state
	 *            the StateVertix to search
	 * @return the copy of the StateVertix in the StateFlowGraph where v.equals(u) or
	 *         <code>null</code> if not found.
	 */
	private StateVertex getStateInJgrpahT(StateVertex state,
	        DirectedGraph<StateVertex, Eventable> sfg) {
		for (StateVertex st : sfg.vertexSet()) {
			if (state.equals(st)) {
				return st;
			}
		}
		return null;
	}

}

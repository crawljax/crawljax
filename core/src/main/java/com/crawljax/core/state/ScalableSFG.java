package com.crawljax.core.state;

import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.CLICKABLE_IN_EDGES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.DOM_IN_NODES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.EDGES_INDEX_NAME;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.NODES_INDEX_NAME;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.NODE_TYPE;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.SERIALIZED_CLICKABLE_IN_EDGES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.SERIALIZED_STATE_VERTEX_IN_NODES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.SOURCE_CLICKABLE_TARGET_IN_EDGES_FOR_UNIQUE_INDEXING;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.SOURCE_STRIPPED_DOM_IN_EDGES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.STATE_ID_IN_NODES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.STATE_NAME_IN_NODES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.STRIPPED_DOM_IN_NODES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.TARGET_STRIPPED_DOM_IN_EDGES;
import static com.crawljax.core.state.DatabaseBackedStateFlowGraphUtilities.URL_IN_NODES;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
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
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.UTF8;
import org.neo4j.kernel.Traversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.ExitNotifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The State-Flow Graph is a directed multigraph with states (StateVetex) on the vertices and
 * clickables (Eventables) on the edges. It stores the data in a graph database. The graph database
 * of choice for this version of StateFlowGraph class is neo4j community edition.
 */

@Singleton
public class ScalableSFG implements Serializable, StateFlowGraph {

	/**
	 * serial version for persisting the class
	 */
	private static final long serialVersionUID = 8765685878231494104L;

	private static final Logger LOG = LoggerFactory.getLogger(ScalableSFG.class
	        .getName());

	/**
	 * Intermediate counter for the number of states, not relaying on getAllStates.size() because of
	 * Thread-safety.
	 */
	private final AtomicInteger stateCounter = new AtomicInteger();
	private final AtomicInteger nextStateNameCounter = new AtomicInteger();

	private final ExitNotifier exitNotifier;

	// The directory path for saving the graph database created by neo4j for
	// storing the state flow graph

	public static String DB_PATH = "target/state-flow-graph-db/graph.db";

	// the connector and main access point to the neo4j graph database

	private static GraphDatabaseService sfgDb;

	// for building an indexing structure within the graph for quick access to nodes and edges.
	public static Node root;

	// index manager
	private static IndexManager indexManager;

	// indexing data structures for ensuring valid concurrent insertion and fast retrieval
	private static Index<Node> nodeIndex;
	private static RelationshipIndex edgesIndex;

	// array indexes for the triple key used in edge indexing
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
	public ScalableSFG(ExitNotifier exitNotifier) {
		this.exitNotifier = exitNotifier;

		setUpDatabase();
		initializeIndices();

		LOG.debug("Initialized the stateflowgraph");

		// adding a shutdown hook to ensure the database will be shut down even
		// if the program breaks

		registerShutdownHook(sfgDb);

	}

	/**
	 * creating the graph database In this method time microseconds are used to ensure that every
	 * time we run the program a clean empty database is used for storing the data
	 */

	private void setUpDatabase() {

		Date date = new Date();

		long time = System.nanoTime();

		String dirPath = DB_PATH + date.toString() + time;
		sfgDb = new GraphDatabaseFactory().newEmbeddedDatabase(dirPath);
	}

	/**
	 * initializing indices for edges and nodes. This indexing data structure is an additional
	 * capability for fast retrieval. It is beside the main graph data structures (which is
	 * comprised of nodes and edges)
	 */

	private void initializeIndices() {

		nodeIndex =
		        sfgDb.index().forNodes(NODES_INDEX_NAME);

		edgesIndex = sfgDb.index().forRelationships(EDGES_INDEX_NAME);

		Node indexNode = null;
		Transaction tx = sfgDb.beginTx();
		try {
			indexNode = sfgDb.createNode();
			indexNode.setProperty(NODE_TYPE, "indexing");

			tx.success();
		} finally {
			tx.finish();
		}

		ScalableSFG.root = indexNode;

	}

	@Override
	public StateVertex putIfAbsent(StateVertex stateVertix) {
		return putIfAbsent(stateVertix, true);
	}

	/**
	 * Adds a state (as a vertex) to the State-Flow Graph if not already present. It adds the
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
	public StateVertex putIfAbsent(StateVertex state, boolean correctName) {
		// the node to be added to the graph and then filled with the state data
		Node toBeAddedNode;

		// for saving the returned result of the method putIfAbsentNode it will be null if the state
		// is not already present in the graph
		Node alreadyEsixts;

		// starting the transaction
		Transaction tx = sfgDb.beginTx();
		try {
			// adding the container for the state which is going to be added to the graph database
			toBeAddedNode = sfgDb.createNode();

			// indexing the state in Index manager. the key that we are using for indexing is
			// the stripped_dom field. This, in particular, is in compliance with the domChanged
			// method in the class Crawler

			alreadyEsixts = putIfAbsentNode(toBeAddedNode,
			        UTF8.decode(UTF8.encode(state.getStrippedDom())));

			if (alreadyEsixts != null) {
				// the state is already indexed
				LOG.debug("putIfAbsent: Graph already contained vertex {}",
				        state);

				// because the state already exists in the graph this transaction
				// is marked to be rolled back
				tx.failure();
			} else {

				addEssentialStateProperties(state, toBeAddedNode, correctName);

				addAdditionalStateProperties(state, toBeAddedNode);

				// flagging successful transaction
				tx.success();
			}
		} finally {
			tx.finish();
		}
		if (alreadyEsixts == null) {
			// the state was not found in the database so it was stored in the database as a new
			// state
			return null;
		} else {
			return state;
		}

	}

	/**
	 * adding textual data which is not used for crawling purpose but are useful for text based
	 * queries in the future
	 * 
	 * @param state
	 *            the state whose data is added to the node as properties
	 * @param toBeAddedNode
	 *            the node which is just added and is the property container for the state data
	 */

	private void addAdditionalStateProperties(StateVertex state, Node toBeAddedNode) {

		// the URL of the state

		String url = state.getUrl();
		if (url != null) {
			toBeAddedNode.setProperty(URL_IN_NODES, url);
		} else {
			toBeAddedNode.setProperty(URL_IN_NODES, "null");
		}

		// the DOM
		String dom = state.getDom();
		if (dom != null) {
			toBeAddedNode.setProperty(DOM_IN_NODES, dom);
		} else {
			toBeAddedNode.setProperty(DOM_IN_NODES, "null");
		}

		// the name of the state
		String name = state.getName();
		if (name != null) {
			toBeAddedNode.setProperty(STATE_NAME_IN_NODES,
			        name);
		} else {
			toBeAddedNode.setProperty(STATE_NAME_IN_NODES, "null");
		}

		// the id of the state
		int id = state.getId();
		toBeAddedNode.setProperty(STATE_ID_IN_NODES, id);

	}

	/**
	 * this method is called after making sure the state is not present in the graph so we continue
	 * with adding it to the graph and add the data which is used in the crawling algorithm of
	 * Crawljax
	 * 
	 * @param state
	 * @param toBeAddedNode
	 * @param correctName
	 */

	private void addEssentialStateProperties(StateVertex state, Node toBeAddedNode,
	        boolean correctName) {

		// indexing the state by its id in addition to its stripped DOM
		nodeIndex.add(toBeAddedNode, STATE_ID_IN_NODES, state.getId());

		// correcting the name

		int count = stateCounter.incrementAndGet();
		exitNotifier.incrementNumberOfStates();
		LOG.debug("Number of states is now {}", count);
		if (correctName) {
			correctStateName(state);
		}

		// serializing the state
		byte[] serializedSV = serializeStateVertex(state);

		// adding the state property which is the main data we store for each node (i.e.
		// each StateVertex) the serialized stateVertex and the Stripped DOM are used
		// for crawling purposes too!

		toBeAddedNode.setProperty(SERIALIZED_STATE_VERTEX_IN_NODES,
		        serializedSV);

		toBeAddedNode.setProperty(STRIPPED_DOM_IN_NODES,
		        UTF8.decode((UTF8.encode(state.getStrippedDom()))));

	}

	private void correctStateName(StateVertex stateVertex) {
		// the -1 is for the "index" state.
		int totalNumberOfStates = this.getNumberofStates() - 1;
		String correctedName = makeStateName(totalNumberOfStates);
		if (!"index".equals(stateVertex.getName())
		        && !stateVertex.getName().equals(correctedName)) {
			LOG.info("Correcting state name from {}  to {}",
			        stateVertex.getName(), correctedName);
			stateVertex.setName(correctedName);
		}
	}

	/**
	 * @param id
	 *            The ID of the state
	 * @return The state if found or <code>null</code>.
	 */
	@Override
	public StateVertex getById(int id) {
		Node node = nodeIndex.get(STATE_ID_IN_NODES, id).getSingle();

		StateVertex state = nodeToState(node);
		return state;
	}

	@Override
	public StateVertex getInitialState() {
		return getById(StateVertex.INDEX_ID);
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
	public boolean addEdge(StateVertex sourceVert, StateVertex targetVert,
	        Eventable eventable) {
		boolean exists = false;

		// When retrieving an edge it is crucial to retrieve the start state and end state of that
		// edge and set them via the setter methods which are implemented by reflection. This is
		// done automatically with jgraphT.

		eventable.setSourceStateVertex(sourceVert);
		eventable.setTargetStateVertex(targetVert);

		byte[] serializedEventable = serializeEventable(eventable);

		Relationship toBeAddedEdge = null;
		Relationship alreadyExists = null;

		String[] edgeTriadKey = new String[3];
		edgeTriadKey[SOURCE_VERTEX_INDEX] = UTF8.decode(UTF8
		        .encode(sourceVert.getStrippedDom()));
		edgeTriadKey[CLICKABLE_INDEX] = UTF8.decode(UTF8.encode(eventable
		        .toString()));
		edgeTriadKey[TARGET_VERTEX_INDEX] = UTF8.decode(UTF8
		        .encode(targetVert.getStrippedDom()));

		String edgeConcatenatedKey =
		        edgeTriadKey[SOURCE_VERTEX_INDEX] + edgeTriadKey[CLICKABLE_INDEX]
		                + edgeTriadKey[TARGET_VERTEX_INDEX];

		Transaction tx = sfgDb.beginTx();
		try {

			Node sourceNode = getNodeFromDB(UTF8.decode(UTF8
			        .encode(sourceVert.getStrippedDom())));
			Node targetNode = getNodeFromDB(UTF8.decode(UTF8
			        .encode(targetVert.getStrippedDom())));
			toBeAddedEdge = sourceNode.createRelationshipTo(targetNode,
			        RelTypes.TRANSITIONS_TO);

			// adding the new edge to the index. it returns null if the edge is successfully
			// added and returns the found edge if an identical edge already exists in the
			// index.

			alreadyExists = edgePutIfAbsent(toBeAddedEdge, edgeConcatenatedKey);

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

	/**
	 * @return the string representation of the graph.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		IndexHits<Node> nodes =
		        nodeIndex.query(STRIPPED_DOM_IN_NODES, "*");
		try {
			for (Node node : nodes) {
				String name = (String) node.getProperty(STATE_NAME_IN_NODES, "noName");

				builder.append(name);
				builder.append(", ");
			}
		} finally {
			nodes.close();
		}

		int lastComma = builder.lastIndexOf(",");
		if (lastComma == builder.length() - 2) {
			builder.delete(lastComma, lastComma + 1);
		}

		builder.append("]");

		return builder.toString();

	}

	/**
	 * Returns a set of all clickables outgoing from the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertex.
	 * @return a set of the outgoing edges (clickables) of the stateVertix.
	 */
	@Override
	public ImmutableSet<Eventable> getOutgoingClickables(StateVertex stateVertix) {

		ImmutableSet.Builder<Eventable> outgoing = new ImmutableSet.Builder<Eventable>();

		Node node = getNodeFromDB(UTF8.decode(UTF8.encode(stateVertix
		        .getStrippedDom())));
		for (Relationship edge : node.getRelationships(
		        RelTypes.TRANSITIONS_TO, Direction.OUTGOING)) {
			Eventable eventable = edgeToEventable(edge);
			// retrieving outgoing state
			Node targetNode = edge.getEndNode();
			StateVertex targetState = nodeToState(targetNode);
			// setting target state and start state

			eventable.setTargetStateVertex(targetState);
			eventable.setSourceStateVertex(stateVertix);

			outgoing.add(eventable);
		}

		return outgoing.build();
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

		ImmutableSet.Builder<Eventable> incoming = new ImmutableSet.Builder<Eventable>();
		Node node = getNodeFromDB(UTF8.decode(UTF8.encode(stateVertix
		        .getStrippedDom())));

		for (Relationship edge : node.getRelationships(
		        RelTypes.TRANSITIONS_TO, Direction.INCOMING)) {
			Eventable eventable = edgeToEventable(edge);
			// retrieving starting state
			Node startNode = edge.getStartNode();
			StateVertex startState = nodeToState(startNode);
			// setting target state and start state

			eventable.setTargetStateVertex(stateVertix);
			eventable.setSourceStateVertex(startState);

			incoming.add(eventable);
		}

		return incoming.build();
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

		ImmutableSet.Builder<StateVertex> outgoing = new ImmutableSet.Builder<>();

		Node sourceNode = getNodeFromDB(UTF8.decode(UTF8.encode(stateVertix
		        .getStrippedDom())));

		for (Relationship edge : sourceNode.getRelationships(
		        RelTypes.TRANSITIONS_TO, Direction.OUTGOING)) {
			Node endNode = edge.getEndNode();
			StateVertex targetState = nodeToState(endNode);
			outgoing.add(targetState);
		}

		return outgoing.build();
	}

	/**
	 * @param clickable
	 *            the edge.
	 * @return the target state of this edge.
	 */
	@Override
	public StateVertex getTargetState(Eventable clickable) {

		// to do
		// get edges one by one and compare it so as to use less memory
		StateVertex foundTargetState = null;
		int occurrence = 0;
		for (Eventable edge : getAllEdges()) {

			if (edge.equals(clickable)) {
				return edge.getTargetStateVertex();
			} else if (edge.toString().equals(clickable.toString())) {
				foundTargetState = edge.getTargetStateVertex();
				occurrence++;
			}
		}

		if (occurrence == 1) {
			return foundTargetState;
		} else {
			return null;
		}
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
	public boolean canGoTo(StateVertex source, StateVertex target) {

		Node sourceNode = getNodeFromDB(UTF8.decode(UTF8.encode(source
		        .getStrippedDom())));
		for (Relationship edge : sourceNode.getRelationships(
		        RelTypes.TRANSITIONS_TO, Direction.OUTGOING)) {

			Node targetNode = edge.getEndNode();

			StateVertex ts = nodeToState(targetNode);
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

			StateVertex ss = nodeToState(srcNode);
			if (ss.equals(source)) {
				return true;
			}
		}

		return false;

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

	@Override
	public ImmutableList<Eventable> getShortestPath(StateVertex start,
	        StateVertex end) {

		ImmutableList.Builder<Eventable> shortestPath = new ImmutableList.Builder<Eventable>();
		Node startNode = getNodeFromDB(start.getStrippedDom());
		Node endNode = getNodeFromDB(end.getStrippedDom());

		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(Traversal
		        .pathExpanderForTypes(RelTypes.TRANSITIONS_TO,
		                Direction.OUTGOING), Integer.MAX_VALUE);

		Iterable<Path> paths = finder.findAllPaths(startNode, endNode);
		if (paths.iterator().hasNext()) {
			Path theShortestPath = paths.iterator().next();

			Iterator<PropertyContainer> container = theShortestPath.iterator();

			// skipping the first node! we already have it from the method parameters!
			if (container.hasNext()) {
				container.next();
			}

			while (container.hasNext()) {

				// retrieving the edge
				Relationship edge = (Relationship) container.next();

				Eventable eventable = edgeToEventable(edge);

				// setting the source state of the edge!
				eventable.setSourceStateVertex(start);

				// retrieving the target node

				Node endNodeForThisEdge = (Node) container.next();
				StateVertex endingState = nodeToState(endNodeForThisEdge);

				// setting the target state
				eventable.setTargetStateVertex(endingState);

				// adding the edge to the shortest path
				shortestPath.add(eventable);

				start = endingState;

			}

		}

		return shortestPath.build();
	}

	public int getNumberofStates() {
		int count = nodeIndex.query(STRIPPED_DOM_IN_NODES, "*").size();
		return count;
	}

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */
	public ImmutableSet<StateVertex> getAllStatesAndPartiallyFilledStates() {

		ImmutableSet.Builder<StateVertex> allStates = new ImmutableSet.Builder<StateVertex>();

		for (Relationship relationship : root.getRelationships(
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
		return allStates.build();

	}

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */

	@Override
	public ImmutableSet<StateVertex> getAllStates() {

		ImmutableSet.Builder<StateVertex> allStates = new ImmutableSet.Builder<StateVertex>();

		for (Relationship relationship : root.getRelationships(
		        Direction.OUTGOING, RelTypes.INDEXES)) {

			Node node = relationship.getEndNode();

			byte[] serializedNode = (byte[]) node.getProperty(
			        SERIALIZED_STATE_VERTEX_IN_NODES, null);

			if (serializedNode != null) {
				StateVertex state = deserializeStateVertex(serializedNode);
				allStates.add(state);
			}
		}

		return allStates.build();
	}

	/**
	 * Return all the edges in the StateFlowGraph.
	 * 
	 * @return a Set of all edges in the StateFlowGraph
	 */
	@Override
	public ImmutableSet<Eventable> getAllEdges() {

		ImmutableSet.Builder<Eventable> all = new ImmutableSet.Builder<Eventable>();
		Transaction tx = sfgDb.beginTx();
		try {
			for (Relationship relationship : root
			        .getRelationships(Direction.OUTGOING, RelTypes.INDEXES)) {
				for (Relationship edge : relationship.getEndNode()
				        .getRelationships(Direction.OUTGOING,
				                RelTypes.TRANSITIONS_TO)) {

					Eventable eventable = edgeToEventable(edge);

					Node startNode = edge.getStartNode();
					StateVertex startState = nodeToState(startNode);

					eventable.setSourceStateVertex(startState);

					Node endNode = edge.getEndNode();
					StateVertex endState = nodeToState(endNode);

					eventable.setTargetStateVertex(endState);

					all.add(eventable);

				}
			}
			tx.success();
		} finally {
			tx.finish();
		}

		return all.build();
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

		String strippedDom = state.getStrippedDom();
		Node node = getNodeFromDB(strippedDom);

		StateVertex deserializedStateVertex = nodeToState(node);
		return deserializedStateVertex;
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

	public List<StateVertex> getDeepStatesFromJgraphT(StateVertex state,
	        DirectedGraph<StateVertex, Eventable> sfg) {
		final Set<String> visitedStates = new HashSet<String>();
		final List<StateVertex> deepStates = new ArrayList<StateVertex>();

		traverseInMemory(visitedStates, deepStates, state, sfg);

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

	private void traverseInMemory(Set<String> visitedStates,
	        List<StateVertex> deepStates, StateVertex state,
	        DirectedGraph<StateVertex, Eventable> sfg) {
		visitedStates.add(state.getName());

		Set<StateVertex> outgoingSet = new HashSet<>();

		for (Eventable c : sfg.outgoingEdgesOf(state)) {
			outgoingSet.add(sfg.getEdgeTarget(c));
		}

		if ((outgoingSet == null) || outgoingSet.isEmpty()) {
			deepStates.add(state);
		} else {
			if (cyclic(visitedStates, outgoingSet)) {
				deepStates.add(state);
			} else {
				for (StateVertex st : outgoingSet) {
					if (!visitedStates.contains(st.getName())) {
						traverseInMemory(visitedStates, deepStates, st, sfg);
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

		StateVertex indexFromGraph = getStateFromJgrpahT(index, sfg);
		if (indexFromGraph == null) {
			LOG.warn("state not found in JgraphT: {} ", index.getName());
			System.exit(1);
		}

		final KShortestPaths<StateVertex, Eventable> kPaths = new KShortestPaths<>(
		        sfg, indexFromGraph, Integer.MAX_VALUE);

		for (StateVertex state : getDeepStatesFromJgraphT(indexFromGraph, sfg)) {

			StateVertex stateFromGraph = getStateFromJgrpahT(state, sfg);
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
		ScalableSFG.sfgDb = sfgDb;
	}

	public static GraphDatabaseService getSfgDb() {
		return sfgDb;
	}

	private Relationship edgePutIfAbsent(Relationship toBeAddedEdge,
	        String edgeConcatenatedKey) {

		Relationship alreadyExists = edgesIndex.putIfAbsent(toBeAddedEdge,
		        SOURCE_CLICKABLE_TARGET_IN_EDGES_FOR_UNIQUE_INDEXING, edgeConcatenatedKey);
		return alreadyExists;

	}

	private Node getNodeFromDB(String strippedDom) {

		Node node = nodeIndex.get(STRIPPED_DOM_IN_NODES, strippedDom).getSingle();
		return node;

	}

	private Node putIfAbsentNode(Node toBeAddedNode, String strippedDom) {

		Node alreadyPresent =
		        nodeIndex.putIfAbsent(toBeAddedNode, STRIPPED_DOM_IN_NODES, strippedDom);
		if (alreadyPresent != null) {
			return alreadyPresent;
		}

		root.createRelationshipTo(toBeAddedNode, RelTypes.INDEXES);

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
		ScalableSFG.indexManager = index;
	}

	public static IndexManager getIndexManager() {
		return indexManager;
	}

	public static void setNodeIndex(Index<Node> nodeIndex) {
		ScalableSFG.nodeIndex = nodeIndex;
	}

	public static void setEdgesIndex(RelationshipIndex edgesIndex) {
		ScalableSFG.edgesIndex = edgesIndex;
	}

	/**
	 * @param stateVertex
	 *            the state which will be serialized
	 * @return a byte array containing persisted byte array of the stateVertex object.
	 */

	public static byte[] serializeStateVertex(StateVertex stateVertex) {

		// for storing the return value
		byte[] serializedStateVertex = null;

		// this an output stream that does not require writing to the file and instead the output
		// stream is stored in a buffer we use this class to utilize the Java serialization api
		// which writes and reads objects to and from streams

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        ObjectOutputStream oos = new ObjectOutputStream(baos)) {

			// Serializing the stateVertex object to the stream

			oos.writeObject(stateVertex);

			// converting the byte array to UTF-8 string for portability reasons

			serializedStateVertex = baos.toByteArray();

		} catch (IOException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

		return serializedStateVertex;
	}

	public static StateVertex deserializeStateVertex(
	        byte[] serializedStateVertex) {

		// the returned value

		StateVertex deserializedSV = null;

		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
		        serializedStateVertex))) {

			deserializedSV = (StateVertex) ois.readObject();

		} catch (Exception e) {

			throw new CrawljaxException(e.getMessage(), e);
		}

		return deserializedSV;

	}

	public static byte[] serializeEventable(Eventable eventable) {

		byte[] serializedEventable = null;

		// this an output stream that does not require writing to the file and instead the output
		// stream is stored in a buffer we use this class to utilize the Java serialization api
		// which writes and reads object to and from streams

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        ObjectOutputStream oos = new ObjectOutputStream(baos)

		) {

			// serializing the Eventable object to the stream

			oos.writeObject(eventable);

			// converting the byte array to UTF-8 string for portability reasons

			serializedEventable = baos.toByteArray();

		} catch (IOException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

		return serializedEventable;
	}

	public static Eventable deserializeEventable(byte[] serializedEventable) {
		// the returned value

		Eventable deserializedEventable = null;

		try (ByteArrayInputStream bais = new ByteArrayInputStream(
		        serializedEventable);

		        ObjectInputStream ois = new ObjectInputStream(bais)) {

			deserializedEventable = (Eventable) ois.readObject();

		} catch (Exception e) {
			throw new CrawljaxException(e.getMessage(), e);
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
			}
		}

		// inserting edges
		Transaction tx = sfgDb.beginTx();
		try {
			for (Relationship relationship : root
			        .getRelationships(Direction.OUTGOING, RelTypes.INDEXES)) {
				for (Relationship edge : relationship.getEndNode()
				        .getRelationships(Direction.OUTGOING,
				                RelTypes.TRANSITIONS_TO)) {

					byte[] serializededge = (byte[]) edge
					        .getProperty(SERIALIZED_CLICKABLE_IN_EDGES);
					Eventable eventable = deserializeEventable(serializededge);

					eventable = edgeToEventable(edge);

					Node sourceNode = edge.getStartNode();
					StateVertex sourceStateVertex = nodeToState(sourceNode);

					Node targetNode = edge.getEndNode();
					StateVertex targetStateVertex = nodeToState(targetNode);

					if ((eventable == null) || (sourceStateVertex == null)
					        || (targetStateVertex == null)) {
						LOG.warn("eventable was not retrieved correctly!");
						System.exit(1);
					} else {
						if (sfg.addEdge(sourceStateVertex, targetStateVertex,
						        eventable)) {
							eventable.setSourceStateVertex(sourceStateVertex);
							eventable.setTargetStateVertex(targetStateVertex);

							LOG.debug("edge added successfully to JgraphT");
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

	private Eventable edgeToEventable(Relationship edge) {

		byte[] serializededge = (byte[]) edge
		        .getProperty(SERIALIZED_CLICKABLE_IN_EDGES, null);
		if (serializededge == null) {
			return null;
		} else {

			Eventable eventable = deserializeEventable(serializededge);

			return eventable;
		}
	}

	private StateVertex nodeToState(Node sourceNode) {

		byte[] serializedStateVertex = (byte[]) sourceNode
		        .getProperty(SERIALIZED_STATE_VERTEX_IN_NODES, null);
		if (serializedStateVertex == null) {
			return null;
		} else {
			StateVertex stateVertex =
			        deserializeStateVertex(serializedStateVertex);
			return stateVertex;
		}
	}

	private StateVertex insertStateInJgraphT(StateVertex stateVertix,
	        DirectedGraph<StateVertex, Eventable> sfg) {
		boolean added = sfg.addVertex(stateVertix);
		if (added) {
			LOG.debug("state added successfully to JgraphT");
			return null;
		} else {
			// Graph already contained the vertix
			LOG.debug("Graph already contained vertex {}",
			        stateVertix.getName());
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
	private StateVertex getStateFromJgrpahT(StateVertex state,
	        DirectedGraph<StateVertex, Eventable> sfg) {
		for (StateVertex st : sfg.vertexSet()) {
			if (state.equals(st)) {
				return st;
			}
		}
		return null;
	}

}

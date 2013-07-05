package com.crawljax.core.state;

import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.CLICKABLE_IN_EDGES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.DOM_IN_NODES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.EDGES_INDEX_NAME;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.NODES_INDEX_NAME;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.NODE_TYPE;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.SERIALIZED_CLICKABLE_IN_EDGES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.SERIALIZED_STATE_VERTEX_IN_NODES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.SOURCE_CLICKABLE_TARGET_IN_EDGES_FOR_UNIQUE_INDEXING;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.SOURCE_STRIPPED_DOM_IN_EDGES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.STATE_ID_IN_NODES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.STATE_NAME_IN_NODES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.STRIPPED_DOM_IN_NODES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.TARGET_STRIPPED_DOM_IN_EDGES;
import static com.crawljax.core.state.InDatabaseStateFlowGraphConstants.URL_IN_NODES;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
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
public class InDatabaseStateFlowGraph implements Serializable, StateFlowGraph {

	private static final long serialVersionUID = 8765685878231494104L;

	private static final Logger LOG = LoggerFactory.getLogger(InDatabaseStateFlowGraph.class
	        .getName());

	/**
	 * Intermediate counter for the number of states, not relaying on getAllStates.size() because of
	 * Thread-safety.
	 */
	private final AtomicInteger stateCounter = new AtomicInteger();
	private final AtomicInteger nextStateNameCounter = new AtomicInteger();

	private final ExitNotifier exitNotifier;

	/**
	 * The directory path for saving the graph database created by neo4j for storing the state flow
	 * graph
	 */
	private String databasePath;

	/**
	 * the connector and main access point to the neo4j graph database
	 */
	private GraphDatabaseService sfgDb;

	/**
	 * for building an indexing structure within the graph to provide quick access to nodes and
	 * edges.
	 */
	private Node root;

	/**
	 * index manager for indexing nodes and relations in the graph database
	 */
	private IndexManager indexManager;

	/**
	 * indexing data structures for ensuring valid concurrent insertion and fast retrieval
	 */
	private Index<Node> nodeIndex;
	private RelationshipIndex edgesIndex;

	/**
	 * @param exitNotifier
	 */
	@Inject
	public InDatabaseStateFlowGraph(ExitNotifier exitNotifier) {
		this.exitNotifier = exitNotifier;

		setUpDatabase();
		initializeIndices();
		LOG.debug("Initialized the stateflowgraph");
		registerShutdownHook(this.sfgDb);

	}

	/**
	 * @param dbPath
	 *            the path to the existing neo4j database
	 * @param root
	 * @param nIndex
	 * @param eIndex
	 * @param exitNotifier
	 */
	public InDatabaseStateFlowGraph(String dbPath, Node root, Index<Node> nIndex,
	        RelationshipIndex eIndex, ExitNotifier exitNotifier) {
		this.exitNotifier = exitNotifier;
		this.databasePath = dbPath;
		this.root = root;
		this.nodeIndex = nIndex;
		this.edgesIndex = eIndex;

		this.sfgDb = new GraphDatabaseFactory().newEmbeddedDatabase(this.databasePath);
		registerShutdownHook(this.sfgDb);

	}

	/**
	 * creating the graph database In this method time microseconds are used to ensure that every
	 * time we run the program a clean empty database is used for storing the data
	 */

	private void setUpDatabase() {

		setDatabasePath(buildDataBaseDirectory());

		sfgDb = new GraphDatabaseFactory().newEmbeddedDatabase(getDatabasePath());
	}

	private String CreateDatabaseDirectory() {

		String dbDir = null;
		File dataBaseDir = new File("target");
		Preconditions
		        .checkNotNull(dataBaseDir, "the database folder is not valid");
		if (dataBaseDir.exists()) {
			checkArgument(dataBaseDir.isDirectory(), dataBaseDir + " is not a directory");
			checkArgument(dataBaseDir.canWrite(), "Database directory is not writable");
		} else {
			boolean created = dataBaseDir.mkdirs();
			checkArgument(created, "Could not create directory " + dataBaseDir);
		}

		dbDir = dataBaseDir.getName();

		return dbDir;

	}

	private String buildDataBaseDirectory() {

		String dbDir = CreateDatabaseDirectory() + "/graph-data/graph.db";
		Date date = new Date();
		long time = System.nanoTime();
		String timeBasedDatabaseDir = dbDir + date.toString() + time;
		return timeBasedDatabaseDir;
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

		this.root = indexNode;

	}

	@Override
	public StateVertex putIfAbsent(StateVertex stateVertix) {
		return putIfAbsent(stateVertix, true);
	}

	@Override
	public StateVertex putIndex(StateVertex index) {
		return putIfAbsent(index, false);
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

	private StateVertex putIfAbsent(StateVertex state, boolean correctName) {
		Node toBeAddedNode;
		Node alreadyEsixts;
		Transaction tx = sfgDb.beginTx();
		try {
			// adding the container for the state which is going to be added to the graph database
			toBeAddedNode = sfgDb.createNode();
			alreadyEsixts =
			        putIfAbsentNode(toBeAddedNode,
			                UTF8.decode(UTF8.encode(state.getStrippedDom())));
			if (alreadyEsixts != null) {
				// the state has already been indexed
				LOG.debug("putIfAbsent: Graph already contained vertex {}",
				        state);
				tx.failure();
			} else {
				// State was not already in the graph so it was placed in the graph and now the
				// associated data is added here
				addEssentialStateProperties(state, toBeAddedNode);
				addAdditionalStateProperties(state, toBeAddedNode);
				tx.success();
			}
		} finally {
			tx.finish();
		}
		if (alreadyEsixts == null) {
			return null;
		} else {
			return state;
		}
	}

	/**
	 * adding textual data which is not used for crawling purposes but are useful for text based
	 * queries in the future
	 * 
	 * @param state
	 *            the state whose data is added to the node as properties
	 * @param toBeAddedNode
	 *            the node which is just added and is the property container for the state data
	 */

	private void addAdditionalStateProperties(StateVertex state, Node toBeAddedNode) {

		String URL = state.getUrl();
		setUrlInNode(URL, toBeAddedNode);

		String dom = state.getDom();
		setDomInNode(dom, toBeAddedNode);

		String name = state.getName();
		setNameInNode(name, toBeAddedNode);

		int id = state.getId();
		setIdInNode(id, toBeAddedNode);

	}

	/**
	 * adds an id property to a node in a graph database
	 * 
	 * @param id
	 * @param toBeAddedNode
	 */
	private void setIdInNode(int id, Node toBeAddedNode) {
		toBeAddedNode.setProperty(STATE_ID_IN_NODES, id);

	}

	/**
	 * adds a name property to a node in a graph database
	 * 
	 * @param name
	 * @param toBeAddedNode
	 */
	private void setNameInNode(String name, Node toBeAddedNode) {
		if (name != null) {
			toBeAddedNode.setProperty(STATE_NAME_IN_NODES,
			        name);
		} else {
			toBeAddedNode.setProperty(STATE_NAME_IN_NODES, "null");
		}
	}

	/**
	 * adds a DOM property to a node in a graph database
	 * 
	 * @param dom
	 * @param toBeAddedNode
	 */
	private void setDomInNode(String dom, Node toBeAddedNode) {
		if (dom != null) {
			toBeAddedNode.setProperty(DOM_IN_NODES, dom);
		} else {
			toBeAddedNode.setProperty(DOM_IN_NODES, "null");
		}

	}

	/**
	 * adds a URL to property to a node in a graph database
	 * 
	 * @param URL
	 * @param toBeAddedNode
	 */

	private void setUrlInNode(String URL, Node toBeAddedNode) {
		if (URL != null) {
			toBeAddedNode.setProperty(URL_IN_NODES, URL);
		} else {
			toBeAddedNode.setProperty(URL_IN_NODES, "null");
		}
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

	private void addEssentialStateProperties(StateVertex state, Node toBeAddedNode) {

		// indexing the state by its id in addition to its stripped DOM
		nodeIndex.add(toBeAddedNode, STATE_ID_IN_NODES, state.getId());
		int count = stateCounter.incrementAndGet();
		exitNotifier.incrementNumberOfStates();
		LOG.debug("Number of states is now {}", count);

		// serializing the state
		byte[] serializedSV = serializeStateVertex(state);

		// adding the state property which is the main data we store for each node (i.e.
		// each StateVertex) the serialized stateVertex and the Stripped DOM are used
		// for crawling purposes in the Crawljax crawling algorithm!
		toBeAddedNode.setProperty(SERIALIZED_STATE_VERTEX_IN_NODES,
		        serializedSV);
		toBeAddedNode.setProperty(STRIPPED_DOM_IN_NODES,
		        UTF8.decode((UTF8.encode(state.getStrippedDom()))));
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

		eventable.setSource(sourceVert);
		eventable.setTarget(targetVert);
		byte[] serializedEventable = serializeEventable(eventable);

		Relationship toBeAddedEdge = null;
		Relationship alreadyExists = null;
		String edgeConcatenatedKey = buildEdgeKey(sourceVert, eventable, targetVert);

		Transaction tx = sfgDb.beginTx();
		try {
			toBeAddedEdge = addEdgeBetweenStates(sourceVert, targetVert);
			alreadyExists = edgePutIfAbsent(toBeAddedEdge, edgeConcatenatedKey);
			if (alreadyExists != null) {
				tx.failure();
			} else {
				addEssentialEdgeProperties(toBeAddedEdge, sourceVert, eventable, targetVert,
				        serializedEventable);
			}
			tx.success();
		} finally {
			tx.finish();
		}
		return (alreadyExists == null);
	}

	private void addEssentialEdgeProperties(Relationship toBeAddedEdge, StateVertex sourceVert,
	        Eventable eventable, StateVertex targetVert, byte[] serializedEventable) {

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

	/**
	 * retrieves the associated nodes of sourceVert and targerVert from the graph and creates an
	 * edge between them. the edges needs to be filled with properties at further stepst
	 * 
	 * @param sourceVert
	 * @param targetVert
	 * @return the created edge
	 */

	private Relationship addEdgeBetweenStates(StateVertex sourceVert, StateVertex targetVert) {
		Node sourceNode = getNodeFromDB(UTF8.decode(UTF8
		        .encode(sourceVert.getStrippedDom())));
		Node targetNode = getNodeFromDB(UTF8.decode(UTF8
		        .encode(targetVert.getStrippedDom())));
		Relationship toBeAddedEdge = sourceNode.createRelationshipTo(targetNode,
		        RelTypes.TRANSITIONS_TO);

		return toBeAddedEdge;
	}

	/**
	 * the uniqueness of new enentables is checked by means of a triple key composed of the input
	 * parameters
	 * 
	 * @param sourceVert
	 * @param eventable
	 * @param targetVert
	 * @return the key used for indexing eventables in the graph database
	 */
	private String buildEdgeKey(StateVertex sourceVert, Eventable eventable,
	        StateVertex targetVert) {
		// array indexes for the triple key used in edge indexing
		final int SOURCE_VERTEX_INDEX = 0;
		final int TARGET_VERTEX_INDEX = 2;
		final int CLICKABLE_INDEX = 1;

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
		return edgeConcatenatedKey;
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
				String name = (String) node.getProperty(STATE_NAME_IN_NODES, "No Name Returned");
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
			// retrieving target state
			Node targetNode = edge.getEndNode();
			StateVertex targetState = nodeToState(targetNode);
			// setting target state and start state
			eventable.setTarget(targetState);
			eventable.setSource(stateVertix);

			outgoing.add(eventable);
		}

		return outgoing.build();
	}

	/**
	 * Returns a set of all edges incoming into the specified vertex.
	 * 
	 * @param stateVertix
	 *            the state vertex.
	 * @return a set of the incoming edges (clickables) of the stateVertix.
	 */
	@Override
	public ImmutableSet<Eventable> getIncomingClickables(StateVertex stateVertix) {
		ImmutableSet.Builder<Eventable> incoming = new ImmutableSet.Builder<Eventable>();
		Node node = getNodeFromDB(UTF8.decode(UTF8.encode(stateVertix
		        .getStrippedDom())));
		for (Relationship edge : node.getRelationships(
		        RelTypes.TRANSITIONS_TO, Direction.INCOMING)) {
			Eventable eventable = edgeToEventable(edge);
			// retrieving starting state
			Node startNode = edge.getStartNode();
			StateVertex startState = nodeToState(startNode);
			// setting the target state and the start state
			eventable.setTarget(stateVertix);
			eventable.setSource(startState);

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
	 * Is it possible to go from s1 -> s2 or s2 -> S1
	 * 
	 * @param source
	 *            the source state.
	 * @param target
	 *            the target state.
	 * @return true if it is possible (edge exists in graph) to go from source to target.
	 */
	@Override
	public boolean canGoTo(StateVertex source, StateVertex target) {

		if (canGoToDirectional(source, target))
		{
			return true;
		}
		if (canGoToDirectional(target, source)) {
			return true;
		}
		return false;
	}

	/**
	 * Is it possible to go from source -> target
	 * 
	 * @param source
	 *            the source state.
	 * @param target
	 *            the target state.
	 * @return true if it is possible (edge exists in graph) to go from source to target.
	 */

	private boolean canGoToDirectional(StateVertex source, StateVertex target) {
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
		Node startNode = getNodeFromDB(start.getStrippedDom());
		Node endNode = getNodeFromDB(end.getStrippedDom());

		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(Traversal
		        .pathExpanderForTypes(RelTypes.TRANSITIONS_TO,
		                Direction.OUTGOING), Integer.MAX_VALUE);

		Iterable<Path> paths = finder.findAllPaths(startNode, endNode);

		ImmutableList.Builder<Eventable> shortestPath =
		        extractEventablesListFromNeo4jPath(paths, start);

		return shortestPath.build();
	}

	/**
	 * converts a neo4j path to a list of eventabls containing the eventables in the neo4j path. it
	 * keeps the order of the eventables unchanged
	 * 
	 * @param paths
	 * @param start
	 *            the starting node in the path
	 * @return
	 */
	private Builder<Eventable> extractEventablesListFromNeo4jPath(Iterable<Path> paths,
	        StateVertex start) {
		ImmutableList.Builder<Eventable> shortestPath = new ImmutableList.Builder<Eventable>();
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
				eventable.setSource(start);

				// retrieving the target node
				Node endNodeForThisEdge = (Node) container.next();
				StateVertex endingState = nodeToState(endNodeForThisEdge);
				eventable.setTarget(endingState);

				// adding the edge to the shortest path
				shortestPath.add(eventable);

				start = endingState;
			}
		}
		return shortestPath;
	}

	// @Override
	// public int getNumberOfStates() {
	// int count = nodeIndex.query(STRIPPED_DOM_IN_NODES, "*").size();
	// return count;
	// }

	/**
	 * Return all the states in the StateFlowGraph.
	 * 
	 * @return all the states on the graph.
	 */
	private ImmutableSet<StateVertex> getAllStatesAndPartiallyFilledStates() {
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
				allStates.add(new StateVertexImpl(Integer.MAX_VALUE, "mock",
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
			for (Relationship relationship : root.getRelationships(Direction.OUTGOING,
			        RelTypes.INDEXES)) {
				for (Relationship edge : relationship.getEndNode().getRelationships(
				        Direction.OUTGOING, RelTypes.TRANSITIONS_TO)) {
					Eventable eventable = edgeToEventable(edge);

					Node startNode = edge.getStartNode();
					StateVertex startState = nodeToState(startNode);
					eventable.setSource(startState);

					Node endNode = edge.getEndNode();
					StateVertex endState = nodeToState(endNode);
					eventable.setTarget(endState);

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

		StateVertex stateFromGraph = nodeToState(node);
		return stateFromGraph;
	}

	/**
	 * @return Dom string average size (byte).
	 */
	@Override
	public int getMeanStateStringSize() {
		final Mean mean = new Mean();

		for (StateVertex state : getAllStates()) {
			mean.increment(state.getDom().getBytes().length);
		}

		return (int) mean.getResult();
	}

	/**
	 * @param state
	 *            The starting state.
	 * @return A list of the deepest states (states with no outgoing edges).
	 */
	private List<StateVertex> getDeepStates(StateVertex state) {
		final Set<String> visitedStates = new HashSet<String>();
		final List<StateVertex> deepStates = new ArrayList<StateVertex>();

		traverse(visitedStates, deepStates, state);

		return deepStates;
	}

	private List<StateVertex> getDeepStatesFromJgraphT(StateVertex state,
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
	 * @return The number of states, currently in the graph.
	 */
	@Override
	public int getNumberOfStates() {
		return stateCounter.get();
	}

	@Override
	public StateVertex newStateFor(String url, String dom, String strippedDom) {
		int id = nextStateNameCounter.incrementAndGet();
		return new StateVertexImpl(id, url, getNewStateName(id), dom, strippedDom);
	}

	private String getNewStateName(int id) {
		return "state" + id;
	}

	/**
	 * This method returns all possible paths from the index state using the Kshortest paths.
	 * 
	 * @param index
	 *            the initial state.
	 * @return a list of GraphPath lists.
	 */

	public List<List<GraphPath<StateVertex, Eventable>>> getAllPossiblePaths(
	        StateVertex index) {
		final List<List<GraphPath<StateVertex, Eventable>>> results = Lists.newArrayList();

		DirectedGraph<StateVertex, Eventable> sfg = this.buildJgraphT();
		StateVertex indexFromGraph = getNotNullStateFromJgrpahT(index, sfg);

		final KShortestPaths<StateVertex, Eventable> kPaths = new KShortestPaths<>(
		        sfg, indexFromGraph, Integer.MAX_VALUE);
		for (StateVertex state : getDeepStatesFromJgraphT(indexFromGraph, sfg)) {
			StateVertex stateFromGraph = getNotNullStateFromJgrpahT(state, sfg);

			List<GraphPath<StateVertex, Eventable>> paths = kPaths.getPaths(stateFromGraph);
			results.add(paths);
		}
		return results;
	}

	private StateVertex getNotNullStateFromJgrpahT(StateVertex state,
	        DirectedGraph<StateVertex, Eventable> sfg) {
		StateVertex stateFromGraph = getStateFromJgrpahT(state, sfg);
		if (stateFromGraph == null) {
			LOG.warn("state not found in JgraphT: {} ", state.getName());
			throw new CrawljaxException("state not found in JgraphT: " + state.getName());
		}
		return stateFromGraph;
	}

	public void setSfgDb(GraphDatabaseService sfgDb) {
		this.sfgDb = sfgDb;
	}

	public GraphDatabaseService getSfgDb() {
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
	private void registerShutdownHook(
	        final GraphDatabaseService graphDatabaseService) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDatabaseService.shutdown();
			}
		});
	}

	public void setIndexManager(IndexManager index) {
		this.indexManager = index;
	}

	public IndexManager getIndexManager() {
		return indexManager;
	}

	public void setNodeIndex(Index<Node> nodeIndex) {
		this.nodeIndex = nodeIndex;
	}

	public void setEdgesIndex(RelationshipIndex edgesIndex) {
		this.edgesIndex = edgesIndex;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public Node getRoot() {
		return root;
	}

	/**
	 * @param databasePath
	 */
	public void setDatabasePath(String databasePath) {
		this.databasePath = databasePath;
	}

	/**
	 * @return
	 */
	public String getDatabasePath() {
		return databasePath;
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
		        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
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
		DirectedGraph<StateVertex, Eventable> sfg = new DirectedMultigraph<>(Eventable.class);

		addAllStatesFromDbToJgraphT(sfg);
		addAllEdgesFromDbToJgraphT(sfg);

		return sfg;
	}

	private void addAllEdgesFromDbToJgraphT(DirectedGraph<StateVertex, Eventable> sfg) {
		Transaction tx = sfgDb.beginTx();
		try {
			for (Relationship relationship : root.getRelationships(Direction.OUTGOING,
			        RelTypes.INDEXES)) {
				for (Relationship edge : relationship.getEndNode().getRelationships(
				        Direction.OUTGOING, RelTypes.TRANSITIONS_TO)) {
					Eventable eventable = edgeToEventable(edge);
					Node sourceNode = edge.getStartNode();
					StateVertex sourceStateVertex = nodeToState(sourceNode);
					Node targetNode = edge.getEndNode();
					StateVertex targetStateVertex = nodeToState(targetNode);
					if ((eventable == null) || (sourceStateVertex == null)
					        || (targetStateVertex == null)) {
						LOG.warn("eventable was not retrieved correctly!");
						throw new CrawljaxException("eventable was not retrieved correctly!");
					} else {
						addEdgeToJgraphT(sourceStateVertex, eventable, targetStateVertex, sfg);

					}
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
	}

	private void addEdgeToJgraphT(StateVertex sourceStateVertex, Eventable eventable,
	        StateVertex targetStateVertex, DirectedGraph<StateVertex, Eventable> sfg) {
		if (sfg.addEdge(sourceStateVertex, targetStateVertex,
		        eventable)) {
			eventable.setSource(sourceStateVertex);
			eventable.setTarget(targetStateVertex);
			LOG.debug("edge added successfully to JgraphT");
		} else {
			LOG.warn("edge insertion failed");
			throw new CrawljaxException("edge insertion failed");
		}
	}

	private void addAllStatesFromDbToJgraphT(DirectedGraph<StateVertex, Eventable> sfg) {
		for (StateVertex state : getAllStates()) {
			if (insertStateInJgraphT(state, sfg) != null) {
				LOG.warn("duplicate state found");
			}
		}

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

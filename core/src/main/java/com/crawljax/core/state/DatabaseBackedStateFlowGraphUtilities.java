/**
 * 
 */
package com.crawljax.core.state;

/**
 * @author arz
 */
public final class DatabaseBackedStateFlowGraphUtilities {

	/**
	 * keys listed here are used for the "key-value pairs" with which data re stored in neo4j nodes
	 * and edges. The key-value pairs are the main places for storing data in neo4j data model of a
	 * graph. The data is stored in edges and nodes of the graph as "properties". Keys here can be
	 * interpreted as labels too.
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

	// the combined key for storing the triples of (sourceStateVertex, Eventable, targetStateVertex)
	// which is used for indexing edges
	public static final String SOURCE_CLICKABLE_TARGET_IN_EDGES_FOR_UNIQUE_INDEXING =
	        "Source-Clickable-Target Triple";

	// node type (indexing, data)
	public static final String NODE_TYPE = "Node Type";
	// the URL key
	public static final String URL_IN_NODES = "URL";

	// the state name key
	public static final String STATE_NAME_IN_NODES = "State Name";

	// the state id key
	public static final String STATE_ID_IN_NODES = "State ID";

	// the key for DOM
	public static final String DOM_IN_NODES = "DOM";

	// the id used for the for node indexer object
	public static final String NODES_INDEX_NAME = "nodesIndex";

	// the id used for the for edge indexer object
	public static final String EDGES_INDEX_NAME = "edgesIndex";

}

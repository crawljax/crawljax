package com.crawljax.core.state;

import org.neo4j.graphdb.RelationshipType;

// The edges in the graph are modeled as relationships between nodes.
// These relationships have enum names and they also are able to have
// properties for holding the data associated with the edges in the
// application

// the relationship between a source vertex and the destination vertex
public enum RelTypes implements RelationshipType {

	// there is a directed edge from state A to state B
	// if there is a clickable in state A which transitions from A to B.

	TRANSITIONS_TO, INDEXES, REFRENCES
}

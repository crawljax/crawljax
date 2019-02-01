package com.crawljax.plugins.crawloverview.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class AlchemyGraphModel {

	private final State[] nodes;
	private final AlchemyEdge[] links;

	@JsonCreator
	public AlchemyGraphModel(@JsonProperty("nodes") State[] nodes,
	        @JsonProperty("links") AlchemyEdge[] links) {
		this.nodes = nodes;
		this.links = links;

	}

	public State[] getNodes() {
		return nodes;
	}

	public AlchemyEdge[] getlinks() {
		return links;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(nodes, links);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof AlchemyGraphModel) {
			AlchemyGraphModel that = (AlchemyGraphModel) object;
			return Objects.equal(this.nodes, that.nodes)
			        && Objects.equal(this.links, that.links);

		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("nodes", nodes).add("links", links)
		        .toString();
	}

}

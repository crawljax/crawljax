package com.crawljax.stateabstractions.dom;

import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.oraclecomparator.comparators.EditDistanceComparator;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class LevenshteinStateVertexImpl extends StateVertexImpl {

	private static final long serialVersionUID = 123400017983489L;

	private EditDistanceComparator editDistanceComparator;

	/**
	 * Defines a State.
	 *
	 * @param url                    the current url of the state
	 * @param name                   the name of the state
	 * @param dom                    the current DOM tree of the browser
	 * @param strippedDom            the stripped dom by the OracleComparators
	 * @param editDistanceComparator
	 */
	public LevenshteinStateVertexImpl(int id, String url, String name, String dom,
			String strippedDom,
			EditDistanceComparator editDistanceComparator) {
		super(id, url, name, dom, strippedDom);
		this.editDistanceComparator = editDistanceComparator;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getStrippedDom());
	}

	@Override
	public boolean equals(Object object) {
		LevenshteinStateVertexImpl that = (LevenshteinStateVertexImpl) object;
		return editDistanceComparator.isEquivalent(this.getStrippedDom(), that.getStrippedDom());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", super.getId())
				.add("name", super.getName()).toString();
	}

	@Override
	public double getDist(StateVertex vertexOfGraph) {
		if (vertexOfGraph instanceof LevenshteinStateVertexImpl) {
			LevenshteinStateVertexImpl vertex = (LevenshteinStateVertexImpl) vertexOfGraph;

			return (double) new LevenshteinDistance().apply(this.getStrippedDom(),
					vertex.getStrippedDom());
		}
		return -1;
	}
}

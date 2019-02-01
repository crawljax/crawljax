package com.crawljax.stateabstractions.dom;

import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.stateabstractions.dom.RTED.RTEDUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class RTEDStateVertexImpl extends StateVertexImpl {

	private static final long serialVersionUID = 123400017983489L;

	private double threshold = 0.0;

	/**
	 * Defines a State.
	 *
	 * @param id          id of the state in the SFG
	 * @param url         the current url of the state
	 * @param name        the name of the state
	 * @param dom         the current DOM tree of the browser
	 * @param strippedDom the stripped dom by the OracleComparators
	 * @param threshold   the threshold to be used
	 */
	public RTEDStateVertexImpl(int id, String url, String name, String dom, String strippedDom,
			double threshold) {
		super(id, url, name, dom, strippedDom);
		this.threshold = threshold;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getStrippedDom());
	}

	public double computeDistance(String dom1, String dom2) {

		return RTEDUtils.getRobustTreeEditDistance(dom1, dom2);
	}

	@Override
	public boolean equals(Object object) {
		RTEDStateVertexImpl that = (RTEDStateVertexImpl) object;
		double distance = computeDistance(this.getDom(), that.getDom());
		return distance <= threshold;

	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", super.getId())
				.add("name", super.getName()).toString();
	}

	@Override
	public double getDist(StateVertex vertexOfGraph) {
		if (vertexOfGraph instanceof RTEDStateVertexImpl) {
			RTEDStateVertexImpl vertex = (RTEDStateVertexImpl) vertexOfGraph;
			return computeDistance(this.getDom(), vertex.getDom());
		}
		return -1;
	}
}

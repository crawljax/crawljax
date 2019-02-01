package com.crawljax.stateabstractions.visual.imagehashes;

import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.opencv.core.Mat;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class MarrHildrethImageHashStateVertexImpl extends StateVertexImpl {

	private static final long serialVersionUID = 123400017983489L;

	MarrHildrethImageHash hash;

	public Mat hashMat;

	/**
	 * Creates a current state without an url and the @strippedDom same as the @dom.
	 *
	 * @param name the name of the state
	 * @param dom  the current DOM tree of the browser
	 */
	@VisibleForTesting MarrHildrethImageHashStateVertexImpl(int id, String name, String dom, MarrHildrethImageHash visHash,
			Mat hashMat) {
		this(id, null, name, dom, dom, visHash, hashMat);
	}

	/**
	 * Defines a State.
	 *
	 * @param url         the current url of the state
	 * @param name        the name of the state
	 * @param dom         the current DOM tree of the browser
	 * @param strippedDom the stripped dom by the OracleComparators
	 */
	public MarrHildrethImageHashStateVertexImpl(int id, String url, String name, String dom, String strippedDom,
			MarrHildrethImageHash visHash, Mat hashMat) { 
		super(id, url, name, dom, strippedDom);
		this.hash = visHash;
		this.hashMat = hashMat;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(hashMat);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof MarrHildrethImageHashStateVertexImpl) {
			MarrHildrethImageHashStateVertexImpl that = (MarrHildrethImageHashStateVertexImpl) object;
			double distance = hash.compare(this.hashMat, that.hashMat);
			return (distance >= hash.minThreshold && distance <= hash.maxThreshold)
					|| (distance == 0.0);
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", super.getId())
				.add("name", super.getName())
				.add("hash", hash).toString();
	}

	@Override
	public boolean inThreshold(StateVertex vertexOfGraph) {
		// Only implemented when there is a threshold for near duplicates
		if (vertexOfGraph instanceof MarrHildrethImageHashStateVertexImpl) {
			MarrHildrethImageHashStateVertexImpl vertex = (MarrHildrethImageHashStateVertexImpl) vertexOfGraph;
			double distance = hash.compare(this.hashMat, vertex.hashMat);
			return distance >= hash.minThreshold && distance <= hash.maxThreshold;
		}
		return false;
	}

	@Override
	public double getDist(StateVertex vertexOfGraph) {
		if (vertexOfGraph instanceof MarrHildrethImageHashStateVertexImpl) {
			MarrHildrethImageHashStateVertexImpl vertex = (MarrHildrethImageHashStateVertexImpl) vertexOfGraph;
			return hash.compare(this.hashMat, vertex.hashMat);
		}
		return -1;
	}
}

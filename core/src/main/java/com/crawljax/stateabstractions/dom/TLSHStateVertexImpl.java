package com.crawljax.stateabstractions.dom;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.stateabstractions.dom.DOMConfiguration.Mode;
import com.crawljax.oraclecomparator.comparators.EditDistanceComparator;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class TLSHStateVertexImpl extends StateVertexImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(TLSHStateVertexImpl.class.getName());

	private static final long serialVersionUID = 123400017983489L;
	
	private double threshold;
	
	private double maxRaw = 633;

	private Mode mode;
	
	private String usedDom;

	private EditDistanceComparator editDistanceComparator;
	
	

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 * 
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 */
	/**
	 * Defines a State.
	 * 
	 * @param url
	 *            the current url of the state
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 * @param strippedDom
	 *            the stripped dom by the OracleComparators
	 * @param threshold 
	 * @param mode 
	 */
	public TLSHStateVertexImpl(int id, String url, String name, String dom, String strippedDom, double threshold, Mode mode, EditDistanceComparator editDistanceComparator) {
		super(id, url, name, dom, strippedDom);
		this.threshold = threshold;
		this.mode = mode;
		this.usedDom = DOMConfiguration.getConfiguredDOM(dom, strippedDom, mode);
		this.editDistanceComparator = editDistanceComparator;
	}	

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getName() + "," + this.getUsedDom());
	}

	
	/**
	 * @TODO in the equals, we could also measure the distance between two pHashes
	 */
	@Override
	public boolean equals(Object object) {
		TLSHStateVertexImpl that = (TLSHStateVertexImpl) object;
		if(this.getName().equalsIgnoreCase(that.getName()))
			return true;
		try {
			double distance = TLSHStateVertexFactory.computeTLSHDistance(this.getUsedDom(), that.getUsedDom());
			if(distance <= threshold*maxRaw)
				return true;
			else
				return false;
		}catch(IllegalArgumentException Ex) {
			LOG.info("DOM not complex enough for TLSH. Falling back on Levenshtein");
			return editDistanceComparator.isEquivalent(this.getUsedDom(), that.getUsedDom());
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", super.getId())
		        .add("name", super.getName()).toString();
	}

	@Override
	public double getDist(StateVertex vertexOfGraph) {
		if (vertexOfGraph instanceof TLSHStateVertexImpl) {
			TLSHStateVertexImpl vertex = (TLSHStateVertexImpl) vertexOfGraph;
			try {
				double distance = TLSHStateVertexFactory.computeTLSHDistance(this.getUsedDom(), vertex.getUsedDom());
				return distance;
			}catch(IllegalArgumentException IAEx) {
				LOG.info("DOM not complex enough for TLSH. Falling back on Levenshtein");
				double distance = StringUtils.getLevenshteinDistance(this.getUsedDom(), vertex.getUsedDom());
				return distance;
			}
		}
		return -1;
	}

	private String getUsedDom() {
		return this.usedDom;
	}
}

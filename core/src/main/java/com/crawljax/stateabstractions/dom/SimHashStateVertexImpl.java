package com.crawljax.stateabstractions.dom;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.stateabstractions.dom.DOMConfiguration.Mode;
import com.crawljax.util.DomUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class SimHashStateVertexImpl extends StateVertexImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(SimHashStateVertexImpl.class.getName());

	private static final long serialVersionUID = 123400017983489L;
	
	private double threshold;

	private Mode mode;
	
	private List<String> tokens;
	
	private String usedDom;

	private String simHash;

	
	

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
	public SimHashStateVertexImpl(int id, String url, String name, String dom, String strippedDom, double threshold, Mode mode) {
		super(id, url, name, dom, strippedDom);
		this.threshold = threshold;
		this.mode = mode;
		this.usedDom = DOMConfiguration.getConfiguredDOM(dom, strippedDom, mode);
		if(this.mode == Mode.CONTENT)
			this.tokens = SimHashStateVertexFactory.tokenizeContent(usedDom);
		else
			this.tokens = SimHashStateVertexFactory.tokenizeContent(DomUtils.getDOMContent(dom));
		this.simHash = SimHashStateVertexFactory.getSimHash(tokens);
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
		SimHashStateVertexImpl that = (SimHashStateVertexImpl) object;
		if(this.getName().equalsIgnoreCase(that.getName()))
			return true;
		double distance = getDist(that);
		if(distance < 64*threshold)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", super.getId())
		        .add("name", super.getName()).toString();
	}

	@Override
	public double getDist(StateVertex vertexOfGraph) {
		if (vertexOfGraph instanceof SimHashStateVertexImpl) {
			SimHashStateVertexImpl vertex = (SimHashStateVertexImpl) vertexOfGraph;
			double distance = SimHashStateVertexFactory.calcuateSimHashDistance(this.simHash, vertex.getSimHash());
			return distance;
		}
		return -1;
	}

	private String getUsedDom() {
		return this.usedDom;
	}
	
	private String getSimHash() {
		return this.simHash;
	}
}

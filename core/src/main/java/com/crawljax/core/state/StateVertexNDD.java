package com.crawljax.core.state;

import java.io.IOException;
import java.util.LinkedList;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.state.duplicatedetection.FeatureException;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionSingleton;
import com.crawljax.util.DomUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import org.jgrapht.DirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class StateVertexNDD implements StateVertex {

	private static final long serialVersionUID = 123400017983488L;
	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class.getName());

	private final int id;
	private final String dom;
	private final String strippedDom;
	private final String url;
	private int[] hash;
	private String name;

	private ImmutableList<CandidateElement> candidateElements;

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 * 
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 */
	@VisibleForTesting
	StateVertexNDD(int id, String name, String dom) {
		this(id, null, name, dom, dom);
		
	}

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
	 */
	public StateVertexNDD(int id, String url, String name, String dom, String strippedDom) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
		try {
			this.hash = NearDuplicateDetectionSingleton.getInstance().generateHash(strippedDom);
		} catch (FeatureException e) {
			this.hash = new int[1];
			this.hash[0] = strippedDom.hashCode();
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDom() {
		return dom;
	}

	@Override
	public String getStrippedDom() {
		return strippedDom;
	}

	@Override
	public String getUrl() {
		return url;
	}

	public int[] getHashes() {
		return this.hash;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof StateVertex) {
			StateVertexNDD that = (StateVertexNDD) object;
			
			return NearDuplicateDetectionSingleton.getInstance().isNearDuplicateHash(this.getHashes(), that.getHashes());
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("id", id)
		        .add("name", name)
		        .toString();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Document getDocument() throws IOException {
		return DomUtils.asDocument(this.dom);
	}

	@Override
	public void setElementsFound(LinkedList<CandidateElement> elements) {
		this.candidateElements = ImmutableList.copyOf(elements);

	}

	@Override
	public ImmutableList<CandidateElement> getCandidateElements() {
		return candidateElements;
	}
	
	public boolean hasNearDuplicate(DirectedGraph<StateVertex, Eventable> sfg) {
		for(StateVertex vertexOfGraph : sfg.vertexSet()) {
			if (this.equals(vertexOfGraph)) {
				LOGGER.debug("Duplicate found: {}, {}", this.getId(), vertexOfGraph.getId());
				return true;
			} else {
				LOGGER.debug("Is not a duplicate: {}, {}", this.getId(), vertexOfGraph.getId());
			}
		}
		return false;
	}
}
package com.crawljax.core.state;

import com.crawljax.core.CandidateElement;
import com.crawljax.util.DomUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.LinkedList;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class StateVertexImpl implements StateVertex {

	private static final long serialVersionUID = 123400017983488L;

	private final int id;
	private final String dom;
	private final String strippedDom;
	private final String url;
	private String name;

	private transient ImmutableList<CandidateElement> candidateElements;

	private boolean isNearDuplicate;

	private int nearestState = -1;

	private double distToNearestState;

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 *
	 * @param name the name of the state
	 * @param dom  the current DOM tree of the browser
	 */
	@VisibleForTesting StateVertexImpl(int id, String name, String dom) {
		this(id, null, name, dom, dom);
	}

	/**
	 * Defines a State.
	 *
	 * @param url         the current url of the state
	 * @param name        the name of the state
	 * @param dom         the current DOM tree of the browser
	 * @param strippedDom the stripped dom by the OracleComparators
	 */
	public StateVertexImpl(int id, String url, String name, String dom, String strippedDom) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
		this.distToNearestState = -1;
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

	@Override
	public int hashCode() {
		return Objects.hashCode(strippedDom);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof StateVertex) {
			StateVertex that = (StateVertex) object;
			return Objects.equal(this.strippedDom, that.getStrippedDom());
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
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

	@Override
	public boolean hasNearDuplicate() {
		return isNearDuplicate;
	}

	@Override
	public void setNearestState(int vertex) {
		this.nearestState = vertex;
	}

	@Override
	public void setHasNearDuplicate(boolean b) {
		this.isNearDuplicate = b;
	}

	@Override
	public int getNearestState() {
		return this.nearestState;
	}

	@Override
	public boolean inThreshold(StateVertex vertexOfGraph) {
		// Only implemented when there is a threshold for near duplicates
		return false;
	}

	@Override
	public double getDistToNearestState() {
		return distToNearestState;
	}

	@Override
	public void setDistToNearestState(double distToNearestState) {
		this.distToNearestState = distToNearestState;
	}

	@Override
	public double getDist(StateVertex vertexOfGraph) {
		// Return proper value when implemented
		return -1;
	}
}

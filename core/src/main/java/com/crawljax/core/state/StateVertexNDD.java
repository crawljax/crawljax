package com.crawljax.core.state;

import java.io.IOException;
import java.util.LinkedList;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.state.duplicatedetection.Fingerprint;
import com.crawljax.util.DomUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.w3c.dom.Document;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
@SuppressWarnings("serial")
public class StateVertexNDD implements StateVertex {

	private final int id;
	private final String dom;
	private final String strippedDom;
	private final String url;
	private String name;

	private ImmutableList<CandidateElement> candidateElements;
	private Fingerprint fingerprint;

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 * 
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 */
	@VisibleForTesting
	StateVertexNDD(int id, String name, String dom, Fingerprint fingerprint) {
		this(id, null, name, dom, dom, fingerprint);
		
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
	@Inject
	public StateVertexNDD(int id, String url, String name, String dom, String strippedDom, Fingerprint fingerprint) {
		this.id = id;
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
		this.fingerprint = fingerprint;
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

	public Fingerprint getFingerprint() {
		return this.fingerprint;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof StateVertexNDD) {
			// If the other Object is a StateVertexNDD, use the fingerprints
			// to compare both objects
			StateVertexNDD that = (StateVertexNDD) object;
			return fingerprint.isNearDuplicateHash(that.getFingerprint());
		} else if (object instanceof StateVertex) {
			// If the other StateVertex does not support near-duplicate detection,
			// use the default approach of comparing (using StrippedDoms).
			StateVertex that = (StateVertex) object;
			return Objects.equal(this.strippedDom, that.getStrippedDom());
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

	public double getMinDuplicateDistance() {
		return minDuplicateDistance;
	}
	
	public void setMinDuplicateDistance(double minDuplicateDistance) {
		this.minDuplicateDistance = minDuplicateDistance;
	}
}

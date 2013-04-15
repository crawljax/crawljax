package com.crawljax.core.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.crawljax.util.DomUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;

/**
 * The state vertex class which represents a state in the browser. This class implements the
 * Iterable interface because on a StateVertex it is possible to iterate over the possible
 * CandidateElements found in this state. When iterating over the possible candidate elements every
 * time a candidate is returned its removed from the list so it is a one time only access to the
 * candidates.
 */
public class StateVertex implements Serializable {

	private static final long serialVersionUID = 123400017983488L;

	private static final Logger LOGGER = LoggerFactory.getLogger(StateVertex.class);

	private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

	public static final int FIRST_STATE_ID = 1;

	private final Collection<Eventable> foundEventables;
	private final int id;
	private String name;
	private String dom;
	private final String strippedDom;
	private final String url;

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public StateVertex() {
		id = ID_GENERATOR.incrementAndGet();
		this.strippedDom = "";
		this.url = "";
		foundEventables = Queues.newConcurrentLinkedQueue();
	}

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 * 
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 */
	public StateVertex(String name, String dom) {
		this(null, name, dom, dom);
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
	public StateVertex(String url, String name, String dom, String strippedDom) {
		id = ID_GENERATOR.incrementAndGet();
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
		foundEventables = Queues.newConcurrentLinkedQueue();
	}

	/**
	 * Retrieve the name of the StateVertex.
	 * 
	 * @return the name of the StateVertex
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieve the DOM String.
	 * 
	 * @return the dom for this state
	 */
	public String getDom() {
		return dom;
	}

	/**
	 * @return the stripped dom by the oracle comparators
	 */
	public String getStrippedDom() {
		return strippedDom;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns a hashcode. Uses reflection to determine the fields to test.
	 * 
	 * @return the hashCode of this StateVertex
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		if (Strings.isNullOrEmpty(strippedDom)) {
			builder.append(dom);
		} else {
			builder.append(strippedDom);
		}

		return builder.toHashCode();
	}

	/**
	 * Compare this vertex to a other StateVertex.
	 * 
	 * @param obj
	 *            the Object to compare this vertex
	 * @return Return true if equal. Uses reflection.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StateVertex)) {
			return false;
		}

		if (this == obj) {
			return true;
		}
		final StateVertex rhs = (StateVertex) obj;

		return new EqualsBuilder().append(this.strippedDom, rhs.getStrippedDom())
		        .isEquals();
	}

	/**
	 * Returns the name of this state as string.
	 * 
	 * @return a string representation of the current StateVertex
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Return the size of the DOM in bytes.
	 * 
	 * @return the size of the dom
	 */
	public int getDomSize() {
		return getDom().getBytes().length;
	}

	/**
	 * @return the id. This is guaranteed to be unique per state.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param dom
	 *            the dom to set
	 */
	public void setDom(String dom) {
		this.dom = dom;
	}

	/**
	 * @return a Document instance of the dom string.
	 * @throws IOException
	 *             if an exception is thrown.
	 */
	public Document getDocument() throws IOException {
		return DomUtils.asDocument(this.dom);
	}

	/**
	 * @param eventable
	 *            The eventable that was clicked in this state.
	 */
	public void registerEventable(Eventable eventable) {
		foundEventables.add(eventable);
	}

	public ImmutableList<Eventable> getUsedEventables() {
		return ImmutableList.copyOf(this.foundEventables);
	}
}

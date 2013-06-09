package com.crawljax.core.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import org.w3c.dom.Document;

import com.crawljax.util.DomUtils;
import com.google.common.base.Objects;
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

	public static final int INDEX_ID = 0;

	private final Collection<Eventable> foundEventables;
	private final int id;
	private String name;
	private String dom;
	private final String strippedDom;
	private final String url;

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public StateVertex(int id) {
		this.id = id;
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
	public StateVertex(int id, String name, String dom) {
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
	public StateVertex(int id, String url, String name, String dom, String strippedDom) {
		this.id = id;
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

	@Override
	public int hashCode() {
		return Objects.hashCode(strippedDom);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof StateVertex) {
			StateVertex that = (StateVertex) object;
			return Objects.equal(this.strippedDom, that.strippedDom);
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

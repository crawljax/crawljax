package com.crawljax.core.state;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.crawljax.util.database.HibernateUtil;

/**
 * The state vertix class which represents a state in the browser.
 * 
 * @author mesbah
 * @version $Id$
 */
public class StateVertix {

	private long id;
	private String name;
	private String dom;
	private final String strippedDom;
	private final String url;

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public StateVertix() {
		this.strippedDom = "";
		this.url = "";
	}

	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 * 
	 * @param name
	 *            the name of the state
	 * @param dom
	 *            the current DOM tree of the browser
	 */
	public StateVertix(String name, String dom) {
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
	public StateVertix(String url, String name, String dom, String strippedDom) {
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
	}

	/**
	 * Retrieve the name of the StateVertix.
	 * 
	 * @return the name of the stateVertix
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

	// /**
	// * Sets the stripped hash of the dom.
	// *
	// * @param browser
	// * the browser where the dom is currently in
	// * @param comparatorsWithPreconditions
	// * the comparators used
	// */
	// public void setDom(EmbeddedBrowser browser,
	// List<ComparatorWithPreconditions> comparatorsWithPreconditions) {
	// try {
	// this.dom = browser.getDom();
	// OracleComparator oc = new OracleComparator(comparatorsWithPreconditions);
	// oc.compare("", dom, browser);
	// String strippedDom = oc.getStrippedNewDom();
	// this.strippedDom = strippedDom;
	// this.strippedHashCode = new HashCodeBuilder().append(strippedDom).toHashCode();
	// this.url = browser.getCurrentUrl();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

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
	public StateVertix clone() {
		return new StateVertix(this.url, this.name, this.dom, this.strippedDom);
	}

	/**
	 * Returns a hashcode. Uses reflection to determine the fields to test.
	 * 
	 * @return the hashCode of this StateVertix
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.strippedDom).toHashCode();
	}

	/**
	 * Compare this vertix to a other StateVertix.
	 * 
	 * @param obj
	 *            the Object to compare this vertix
	 * @return Return true if equal. Uses reflection.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StateVertix)) {
			return false;
		}

		if (this == obj) {
			return true;
		}
		final StateVertix rhs = (StateVertix) obj;
		return new EqualsBuilder().append(this.strippedDom, rhs.getStrippedDom()).isEquals();
	}

	/**
	 * Returns the name of this state as string.
	 * 
	 * @return a string representation of the current StateVertix
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
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Retrive the state vertex from Database.
	 * 
	 * @param id
	 *            the Id to search
	 * @return returns the StateVertix with id id
	 */
	public static StateVertix getStateVertix(long id) {
		return (StateVertix) HibernateUtil.currentSession().get(StateVertix.class, new Long(id));
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

}

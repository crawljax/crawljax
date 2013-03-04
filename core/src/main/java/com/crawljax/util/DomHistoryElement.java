package com.crawljax.util;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.crawljax.core.state.Eventable;

/**
 * DomHistoryElement contain a string representation of the DOM and the Document representation. The
 * active element is not explicitly stored because it can always be found by searching for the
 * element which has the requestForProxyId attribute.
 */
public class DomHistoryElement {

	private static final Logger LOG = LoggerFactory.getLogger(DomHistoryElement.class);
	/**
	 * String representation of the DOM.
	 */
	private String domStr;

	/**
	 * Document object of the DOM.
	 */
	private Document dom;

	private List<Eventable> eventSequence;

	/**
	 * Constructor.
	 * 
	 * @param domStr
	 *            The DOM string.
	 * @param dom
	 *            The DOM.
	 * @param eventSeq
	 *            The events sequence as a list.
	 */
	public DomHistoryElement(String domStr, Document dom, List<Eventable> eventSeq) {
		this.domStr = domStr;
		this.dom = dom;
		this.eventSequence = eventSeq;
	}

	/**
	 * Constructor. Parses domStr into a Document object.
	 * 
	 * @param domStr
	 *            The DOM string.
	 * @param eventSeq
	 *            The events sequence as a list.
	 */
	public DomHistoryElement(String domStr, List<Eventable> eventSeq) {
		this.domStr = domStr;
		this.eventSequence = eventSeq;
		try {
			this.dom = DomUtils.asDocument(domStr);
		} catch (IOException e) {
			LOG.error("Could not construct with dom", e);
		}

	}

	/**
	 * @return the domStr
	 */
	public String getDomStr() {
		return this.domStr;
	}

	/**
	 * @param domStr
	 *            the domStr to set
	 */
	public void setDomStr(String domStr) {
		this.domStr = domStr;
	}

	/**
	 * @return the dom
	 */
	public Document getDom() {
		return this.dom;
	}

	/**
	 * @param dom
	 *            the dom to set
	 */
	public void setDom(Document dom) {
		this.dom = dom;
	}

	/**
	 * @return The event sequence.
	 */
	public List<Eventable> getEventSequence() {
		return eventSequence;
	}

	/**
	 * @param eventSequence
	 *            The event sequence.
	 */
	public void setEventSequence(List<Eventable> eventSequence) {
		this.eventSequence = eventSequence;
	}

}

package com.crawljax.core.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.core.CandidateCrawlAction;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.TagElement;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.util.Helper;

/**
 * The state vertix class which represents a state in the browser. This class implements the
 * Iterable interface because on a StateVertix it is possible to iterate over the possible
 * CandidateElements found in this state. When iterating over the possible candidate elements every
 * time a candidate is returned its removed from the list so it is a one time only access to the
 * candidates.
 * 
 * @author mesbah
 * @version $Id$
 */
public class StateVertix implements Iterable<CandidateCrawlAction>, Serializable {

	private static final long serialVersionUID = 123400017983488L;
	private static final Logger LOGGER = Logger.getLogger(StateVertix.class);
	private long id;
	private String name;
	private String dom;
	private final String strippedDom;
	private final String url;
	private boolean guidedCrawling = false;

	/**
	 * This list is used to store the possible candidates. If it is null its not initialised if it's
	 * a empty list its empty.
	 */
	private List<CandidateCrawlAction> candidateActions;

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
		HashCodeBuilder builder = new HashCodeBuilder();
		if (strippedDom == null || "".equals(strippedDom)) {
			builder.append(dom);
		} else {
			builder.append(strippedDom);
		}

		return builder.toHashCode();
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

		return new EqualsBuilder().append(this.strippedDom, rhs.getStrippedDom()).append(
		        this.guidedCrawling, rhs.guidedCrawling).isEquals();
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
	 * @return if this state is created through guided crawling.
	 */
	public boolean isGuidedCrawling() {
		return guidedCrawling;
	}

	/**
	 * @param guidedCrawling
	 *            true if set through guided crawling.
	 */
	public void setGuidedCrawling(boolean guidedCrawling) {
		this.guidedCrawling = guidedCrawling;
	}

	/**
	 * search for new Candidates from this state. The search for candidates is only done when no
	 * list is available yet (candidateActions == null).
	 * 
	 * @param candidateExtractor
	 *            the CandidateElementExtractor to use.
	 * @param crawlTagElements
	 *            the tag elements to examine.
	 * @param crawlExcludeTagElements
	 *            the elements to exclude.
	 * @param clickOnce
	 *            if true examine each element once.
	 */
	public void searchForCandidateElements(CandidateElementExtractor candidateExtractor,
	        List<TagElement> crawlTagElements, List<TagElement> crawlExcludeTagElements,
	        boolean clickOnce) {

		// TODO read the eventtypes from the crawl elements instead
		List<String> eventTypes = new ArrayList<String>();
		eventTypes.add(EventType.click.toString());

		if (candidateActions == null) {
			candidateActions = new ArrayList<CandidateCrawlAction>();
			try {
				List<CandidateElement> candidateList =
				        candidateExtractor.extract(crawlTagElements, crawlExcludeTagElements,
				                clickOnce, this);

				for (CandidateElement candidateElement : candidateList) {
					for (String eventType : eventTypes) {
						if (eventType.equals(EventType.click.toString())) {
							candidateActions.add(new CandidateCrawlAction(candidateElement,
							        EventType.click));
						} else {
							if (eventType.equals(EventType.hover.toString())) {
								candidateActions.add(new CandidateCrawlAction(candidateElement,
								        EventType.hover));
							} else {
								LOGGER
								        .warn("The Event Type: " + eventType
								                + " is not supported.");
							}
						}
					}
				}
			} catch (CrawljaxException e) {
				LOGGER.error("Catched exception while searching for candidates in state "
				        + getName(), e);
			}
		}
	}

	/**
	 * Are there any more candidates to explore?
	 * 
	 * @return true if there are candidates left in this state. false otherwise.
	 */
	public boolean hasMoreToExplore() {
		return candidateActions != null && !candidateActions.isEmpty();
	}

	/**
	 * Return a list of UnprocessedCandidates in a List.
	 * 
	 * @return a list of candidates which are unprocessed.
	 */
	public List<CandidateElement> getUnprocessedCandidateElements() {
		List<CandidateElement> list = new ArrayList<CandidateElement>();
		if (candidateActions == null) {
			return list;
		}
		CandidateElement last = null;
		for (CandidateCrawlAction candidateAction : candidateActions) {
			if (last != candidateAction.getCandidateElement()) {
				last = candidateAction.getCandidateElement();
				list.add(last);
			}
		}
		return list;
	}

	/**
	 * Retrieve a Iterator to iterate with. Made in a anonymous-innerclass.
	 * 
	 * @return the iterator which deletes every element returned.
	 */
	@Override
	public Iterator<CandidateCrawlAction> iterator() {
		return new Iterator<CandidateCrawlAction>() {

			@Override
			public boolean hasNext() {
				return hasMoreToExplore();
			}

			@Override
			public CandidateCrawlAction next() {
				return candidateActions.remove(0);
			}

			@Override
			public void remove() {
			}
		};
	}

	/**
	 * @return a Document instance of the dom string.
	 * @throws SAXException
	 *             if an exception is thrown.
	 * @throws IOException
	 *             if an exception is thrown.
	 */
	public Document getDocument() throws SAXException, IOException {
		return Helper.getDocument(this.dom);
	}

}

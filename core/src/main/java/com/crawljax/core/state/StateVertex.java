package com.crawljax.core.state;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.core.CandidateCrawlAction;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CrawlQueueManager;
import com.crawljax.core.Crawler;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.TagElement;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.util.Helper;

/**
 * The state vertex class which represents a state in the browser. This class implements the
 * Iterable interface because on a StateVertex it is possible to iterate over the possible
 * CandidateElements found in this state. When iterating over the possible candidate elements every
 * time a candidate is returned its removed from the list so it is a one time only access to the
 * candidates.
 * 
 * @author mesbah
 * @version $Id$
 */
public class StateVertex implements Serializable {

	private static final long serialVersionUID = 123400017983488L;

	private static final Logger LOGGER = LoggerFactory.getLogger(StateVertex.class);
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
	private LinkedBlockingDeque<CandidateCrawlAction> candidateActions;

	private final ConcurrentHashMap<Crawler, CandidateCrawlAction> registerdCandidateActions =
	        new ConcurrentHashMap<Crawler, CandidateCrawlAction>();
	private final ConcurrentHashMap<Crawler, CandidateCrawlAction> workInProgressCandidateActions =
	        new ConcurrentHashMap<Crawler, CandidateCrawlAction>();

	private final Object candidateActionsSearchLock = new String("");

	private final LinkedBlockingDeque<Crawler> registeredCrawlers =
	        new LinkedBlockingDeque<Crawler>();

	/**
	 * Default constructor to support saving instances of this class as an XML.
	 */
	public StateVertex() {
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
		this.url = url;
		this.name = name;
		this.dom = dom;
		this.strippedDom = strippedDom;
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
		if (strippedDom == null || "".equals(strippedDom)) {
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
		        .append(this.guidedCrawling, rhs.guidedCrawling).isEquals();
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
	 * @return true if the searchForCandidateElemens has run false otherwise
	 */
	@GuardedBy("candidateActionsSearchLock")
	public boolean searchForCandidateElements(CandidateElementExtractor candidateExtractor,
	        List<TagElement> crawlTagElements, List<TagElement> crawlExcludeTagElements,
	        boolean clickOnce) {
		synchronized (candidateActionsSearchLock) {
			if (candidateActions == null) {
				candidateActions = new LinkedBlockingDeque<CandidateCrawlAction>();
			} else {
				return false;
			}
		}
		// TODO read the eventtypes from the crawl elements instead
		List<String> eventTypes = new ArrayList<String>();
		eventTypes.add(EventType.click.toString());

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
							LOGGER.warn("The Event Type: " + eventType + " is not supported.");
						}
					}
				}
			}
		} catch (CrawljaxException e) {
			LOGGER.error(
			        "Catched exception while searching for candidates in state " + getName(), e);
		}
		return candidateActions.size() > 0; // Only notify of found candidates when there are...

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
	 * Removes Candidate Actions on candidateElements that have been removed by the pre-state crawl
	 * plugin.
	 * 
	 * @param candidateElements
	 */
	public void filterCandidateActions(List<CandidateElement> candidateElements) {
		if (candidateActions == null) {
			return;
		}
		Iterator<CandidateCrawlAction> iter = candidateActions.iterator();
		CandidateCrawlAction currentAction;
		while (iter.hasNext()) {
			currentAction = iter.next();
			if (!candidateElements.contains(currentAction.getCandidateElement())) {
				iter.remove();
				LOGGER.info("filtered candidate action: " + currentAction.getEventType().name()
				        + " on " + currentAction.getCandidateElement().getGeneralString());

			}
		}
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

	/**
	 * This is the main work divider function, calling this function will first look at the
	 * registeedCandidateActions to see if the current Crawler has already registered itself at one
	 * of the jobs. Second it tries to see if the current crawler is not already processing one of
	 * the actions and return that action and last it tries to find an unregistered candidate. If
	 * all else fails it tries to return a action that is registered by an other crawler and
	 * disables that crawler.
	 * 
	 * @param requestingCrawler
	 *            the Crawler placing the request for the Action
	 * @param manager
	 *            the manager that can be used to remove a crawler from the queue.
	 * @return the action that needs to be performed by the Crawler.
	 */
	public CandidateCrawlAction pollCandidateCrawlAction(Crawler requestingCrawler,
	        CrawlQueueManager manager) {
		CandidateCrawlAction action = registerdCandidateActions.remove(requestingCrawler);
		if (action != null) {
			workInProgressCandidateActions.put(requestingCrawler, action);
			return action;
		}
		action = workInProgressCandidateActions.get(requestingCrawler);
		if (action != null) {
			return action;
		}
		action = candidateActions.pollFirst();
		if (action != null) {
			workInProgressCandidateActions.put(requestingCrawler, action);
			return action;
		} else {
			Crawler c = registeredCrawlers.pollFirst();
			if (c == null) {
				return null;
			}
			do {
				if (manager.removeWorkFromQueue(c)) {
					LOGGER.info("Crawler " + c + " REMOVED from Queue!");
					action = registerdCandidateActions.remove(c);
					if (action != null) {
						/*
						 * We got a action and removed the registeredCandidateActions for the
						 * crawler, remove the crawler from queue as the first thinng. As the
						 * crawler might just have started the run method of the crawler must also
						 * be added with a check hook.
						 */
						LOGGER.info("Stolen work from other Crawler");
						return action;
					} else {
						LOGGER.warn("Oh my! I just removed " + c
						        + " from the queue with no action!");
					}
				} else {
					LOGGER.warn("FAILED TO REMOVE " + c + " from Queue!");
				}
				c = registeredCrawlers.pollFirst();
			} while (c != null);
		}
		return null;
	}

	/**
	 * Register an assignment to the crawler.
	 * 
	 * @param newCrawler
	 *            the crawler that wants an assignment
	 * @return true if the crawler has an assignment false otherwise.
	 */
	public boolean registerCrawler(Crawler newCrawler) {
		CandidateCrawlAction action = candidateActions.pollLast();
		if (action == null) {
			return false;
		}
		registeredCrawlers.offerFirst(newCrawler);
		registerdCandidateActions.put(newCrawler, action);
		return true;
	}

	/**
	 * Register a Crawler that is going to work, tell if his must go on or abort.
	 * 
	 * @param crawler
	 *            the crawler to register
	 * @return true if the crawler is successfully registered
	 */
	public boolean startWorking(Crawler crawler) {
		CandidateCrawlAction action = registerdCandidateActions.remove(crawler);
		registeredCrawlers.remove(crawler);
		if (action == null) {
			return false;
		} else {
			workInProgressCandidateActions.put(crawler, action);
			return true;
		}
	}

	/**
	 * Notify the current StateVertex that the given crawler has finished working on the given
	 * action.
	 * 
	 * @param crawler
	 *            the crawler that is finished
	 * @param action
	 *            the action that have been examined
	 */
	public void finishedWorking(Crawler crawler, CandidateCrawlAction action) {
		candidateActions.remove(action);
		registerdCandidateActions.remove(crawler);
		workInProgressCandidateActions.remove(crawler);
		registeredCrawlers.remove(crawler);
	}
}

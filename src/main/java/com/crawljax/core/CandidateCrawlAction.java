package com.crawljax.core;

/**
 * This class corresponds the combination of a CandidateElement and a single eventType.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class CandidateCrawlAction {
	private final CandidateElement candidateElement;
	private final String eventType;

	/**
	 * The Constructor for the CandidateCrawlAction, build a new instance with the CandidateElement
	 * and the EventType.
	 * 
	 * @param candidateElement
	 *            the element to execute the eventType on
	 * @param eventType
	 *            the eventType to execute on the Candidate Element.
	 */
	public CandidateCrawlAction(CandidateElement candidateElement, String eventType) {
		this.candidateElement = candidateElement;
		this.eventType = eventType;
	}

	/**
	 * @return the candidateElement
	 */
	public final CandidateElement getCandidateElement() {
		return candidateElement;
	}

	/**
	 * @return the eventType
	 */
	public final String getEventType() {
		return eventType;
	}
}

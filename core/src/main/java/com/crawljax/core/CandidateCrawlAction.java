package com.crawljax.core;

import com.crawljax.core.state.Eventable.EventType;
import com.google.common.base.Objects;

/**
 * This class corresponds the combination of a CandidateElement and a single eventType.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public class CandidateCrawlAction {
	private final CandidateElement candidateElement;
	private final EventType eventType;

	/**
	 * The Constructor for the CandidateCrawlAction, build a new instance with the CandidateElement
	 * and the EventType.
	 * 
	 * @param candidateElement
	 *            the element to execute the eventType on
	 * @param eventType
	 *            the eventType to execute on the Candidate Element.
	 */
	public CandidateCrawlAction(CandidateElement candidateElement, EventType eventType) {
		this.candidateElement = candidateElement;
		this.eventType = eventType;
	}

	/**
	 * @return the candidateElement
	 */
	public CandidateElement getCandidateElement() {
		return candidateElement;
	}

	/**
	 * @return the eventType
	 */
	public EventType getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("candidateElement", candidateElement)
		        .add("eventType", eventType)
		        .toString();
	}

}

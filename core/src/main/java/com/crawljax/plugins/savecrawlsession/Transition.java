package com.crawljax.plugins.savecrawlsession;

/**
 * Alternative representation for Edge. Used for saving to XML.<br />
 * IMPORTANT: Should only be used by SaveCrawlSessionPlugin
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $id$
 */
public class Transition {

	private String fromState;
	private String toState;
	private long eventableId;

	/**
	 * Default constructor needed for saving to xml.
	 */
	public Transition() {

	}

	/**
	 * @param fromState
	 *            the name of the source state
	 * @param toState
	 *            the name of the target state
	 * @param eventableId
	 *            the unique id of an eventable. Is set by SaveCrawlSessionPlugin
	 */
	public Transition(String fromState, String toState, long eventableId) {
		super();
		this.fromState = fromState;
		this.toState = toState;
		this.eventableId = eventableId;
	}

	/**
	 * @return the name of the source state
	 */
	public String getFromState() {
		return fromState;
	}

	/**
	 * @return the name of the target state
	 */
	public String getToState() {
		return toState;
	}

	/**
	 * @return the id of the eventable
	 */
	public long getEventableId() {
		return eventableId;
	}

	/**
	 * @param fromState
	 *            the name of the source state
	 */
	public void setFromState(String fromState) {
		this.fromState = fromState;
	}

	/**
	 * @param toState
	 *            the name of the target state
	 */
	public void setToState(String toState) {
		this.toState = toState;
	}

	/**
	 * @param eventableId
	 *            the unique id of an eventable. Is set by SaveCrawlSessionPlugin
	 */
	public void setEventableId(long eventableId) {
		this.eventableId = eventableId;
	}

}

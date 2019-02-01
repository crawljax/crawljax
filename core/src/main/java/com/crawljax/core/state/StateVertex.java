package com.crawljax.core.state;

import com.crawljax.core.CandidateElement;
import com.google.common.collect.ImmutableList;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * A vertex in the {@link StateFlowGraph} representing a state in the web application.
 */
public interface StateVertex extends Serializable {

	/**
	 * The {@link #getId()} of the Index state.
	 */
	int INDEX_ID = 0;

	/**
	 * Retrieve the name of the StateVertex.
	 *
	 * @return the name of the StateVertex
	 */
	String getName();

	/**
	 * Retrieve the DOM String.
	 *
	 * @return the dom for this state
	 */
	String getDom();

	/**
	 * @return the stripped dom by the oracle comparators
	 */
	String getStrippedDom();

	/**
	 * @return the url
	 */
	String getUrl();

	/**
	 * @return the id. This is guaranteed to be unique per state.
	 */
	int getId();

	/**
	 * @return a Document instance of the dom string.
	 * @throws IOException if an exception is thrown.
	 */
	Document getDocument() throws IOException;

	/**
	 * @param elements Set the candidate elements for this state vertex that might be fired.
	 */
	void setElementsFound(LinkedList<CandidateElement> elements);

	/**
	 * @return A list of {@link CandidateElement} that might have been fired during the crawl. If an
	 * event was fired it is registered as an {@link Eventable} an can be retrieved from
	 * {@link StateFlowGraph#getAllEdges()}. If the candidates were not set because of an
	 * error it returns <code>null</code>.
	 */
	ImmutableList<CandidateElement> getCandidateElements();

	boolean hasNearDuplicate();

	void setHasNearDuplicate(boolean b);

	int getNearestState();

	boolean inThreshold(StateVertex vertexOfGraph);

	double getDistToNearestState();

	void setDistToNearestState(double distToNearestState);

	double getDist(StateVertex vertexOfGraph);

	void setNearestState(int vertex);

}

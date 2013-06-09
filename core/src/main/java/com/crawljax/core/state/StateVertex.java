package com.crawljax.core.state;

import java.io.IOException;
import java.io.Serializable;

import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;

public interface StateVertex extends Serializable {

	/**
	 * The {@link #getId()} of the Index state.
	 */
	public static final int INDEX_ID = 0;

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
	 * @throws IOException
	 *             if an exception is thrown.
	 */
	Document getDocument() throws IOException;

	ImmutableList<Eventable> getUsedEventables();

}
package com.crawljax.core.state;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * A factory that creates a {@link com.crawljax.core.state.StateVertex}. This factory can be
 * implemented if you want to use custom states that use a different {@link Object#hashCode()} or
 * {@link Object#equals(Object)} method.
 */
public abstract class StateVertexFactory {

	/**
	 * Defines a State.
	 *
	 * @param url         the current url of the state
	 * @param name        the name of the state
	 * @param dom         the current DOM tree of the browser
	 * @param strippedDom the stripped dom by the OracleComparators
	 */
	public abstract StateVertex newStateVertex(int id, String url, String name, String dom,
			String strippedDom, EmbeddedBrowser browser);

	/**
	 * @return The index {@link StateVertex}.
	 */
	public StateVertex createIndex(String url, String dom, String strippedDom,
			EmbeddedBrowser browser) {
		return newStateVertex(StateVertex.INDEX_ID, url, "index", dom, strippedDom, browser);
	}
}

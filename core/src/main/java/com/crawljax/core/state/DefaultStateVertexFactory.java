package com.crawljax.core.state;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * The default factory that creates State vertexes with a {@link Object#hashCode()} and
 * {@link Object#equals(Object)} function based on the Stripped dom.
 */
public class DefaultStateVertexFactory extends StateVertexFactory {

	@Override
	public StateVertex newStateVertex(int id, String url, String name, String dom,
			String strippedDom, EmbeddedBrowser browser) {
		return new StateVertexImpl(id, url, name, dom, strippedDom);
	}
}

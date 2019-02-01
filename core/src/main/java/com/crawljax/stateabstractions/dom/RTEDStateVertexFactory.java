package com.crawljax.stateabstractions.dom;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;

/**
 * The default factory that creates State vertexes with a {@link Object#hashCode()} and
 * {@link Object#equals(Object)} function based on the Stripped dom.
 */
public class RTEDStateVertexFactory extends StateVertexFactory {

	private static double threshold = 0.0;

	public RTEDStateVertexFactory() {
	}

	public RTEDStateVertexFactory(double threshold) {
		RTEDStateVertexFactory.threshold = threshold;
	}

	@Override
	public StateVertex newStateVertex(int id, String url, String name, String dom,
			String strippedDom,
			EmbeddedBrowser browser) {
		return new RTEDStateVertexImpl(id, url, name, dom, strippedDom, threshold);
	}

	@Override
	public String toString() {
		return "DOM-RTED_" + threshold;
	}

}

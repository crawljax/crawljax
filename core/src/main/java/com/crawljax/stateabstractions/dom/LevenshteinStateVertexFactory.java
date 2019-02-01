package com.crawljax.stateabstractions.dom;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.oraclecomparator.comparators.EditDistanceComparator;

/**
 * The default factory that creates State vertexes with a {@link Object#hashCode()} and
 * {@link Object#equals(Object)} function based on the Stripped dom.
 */
public class LevenshteinStateVertexFactory extends StateVertexFactory {

	private EditDistanceComparator editDistanceComparator;
	private static double threshold = 1.0;

	public LevenshteinStateVertexFactory() {
		this.editDistanceComparator = new EditDistanceComparator(threshold);
	}

	public LevenshteinStateVertexFactory(double threshold) {
		LevenshteinStateVertexFactory.threshold = threshold;
		this.editDistanceComparator =
				new EditDistanceComparator(LevenshteinStateVertexFactory.threshold);
	}

	@Override
	public StateVertex newStateVertex(int id, String url, String name, String dom,
			String strippedDom,
			EmbeddedBrowser browser) {

		return new LevenshteinStateVertexImpl(id, url, name, dom, strippedDom,
				editDistanceComparator);
	}

	@Override
	public String toString() {
		return "DOMLevenshtein_" + threshold;
	}

}

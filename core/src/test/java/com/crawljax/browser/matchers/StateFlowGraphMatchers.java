package com.crawljax.browser.matchers;

import static org.hamcrest.core.IsEqual.equalTo;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import com.crawljax.core.state.StateFlowGraph;

public class StateFlowGraphMatchers {

	/**
	 * @param edges
	 *            The number of expected edges.
	 * @return A {@link Matcher} that inspects if the number of edges.
	 */
	@Factory
	public static FeatureMatcher<StateFlowGraph, Integer> hasEdges(int edges) {
		return new FeatureMatcher<StateFlowGraph, Integer>(equalTo(edges),
		        "Stateflowgraph with number of edges", "number of edges") {

			@Override
			protected Integer featureValueOf(StateFlowGraph actual) {
				return actual.getAllEdges().size();
			}
		};
	}

	/**
	 * @param states
	 *            The number of expected states.
	 * @return A {@link Matcher} that inspects if the number of states.
	 */
	@Factory
	public static FeatureMatcher<StateFlowGraph, Integer> hasStates(int states) {
		return new FeatureMatcher<StateFlowGraph, Integer>(equalTo(states),
		        "Stateflowgraph with number of states", "number of states") {

			@Override
			protected Integer featureValueOf(StateFlowGraph actual) {
				return actual.getAllStates().size();
			}

		};
	}
}

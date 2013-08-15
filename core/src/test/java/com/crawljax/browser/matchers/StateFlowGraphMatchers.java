package com.crawljax.browser.matchers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;

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

	/**
	 * @param substring
	 *            A {@link String} that occurs in the DOM.
	 * @return A {@link Matcher} that inspects if the number of edges.
	 */
	@Factory
	public static FeatureMatcher<StateVertex, String> stateWithDomSubstring(String substring) {
		return new FeatureMatcher<StateVertex, String>(containsString(substring),
		        "StateVertex with in the DOM", "substring") {

			@Override
			protected String featureValueOf(StateVertex actual) {
				// System.out.println(actual.getDom());
				return actual.getDom();
			}

		};
	}

	@Test
	public void testStateWithDomSubstring() {
		StateVertex vertex = mock(StateVertex.class);
		when(vertex.getDom()).thenReturn("paap");
		assertThat(vertex, is(stateWithDomSubstring("aap")));
		assertThat(vertex, is(not(stateWithDomSubstring("bla"))));
	}
}

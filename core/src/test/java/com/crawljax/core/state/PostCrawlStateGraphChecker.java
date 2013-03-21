package com.crawljax.core.state;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.PostCrawlingPlugin;

/**
 * This {@link PostCrawlingPlugin} checks the {@link StateFlowGraph} for consistency after the crawl
 * is done.
 */
public class PostCrawlStateGraphChecker implements PostCrawlingPlugin {

	@Override
	public void postCrawling(CrawlSession session) {
		StateFlowGraph stateFlowGraph = session.getStateFlowGraph();

		allStatesHaveOneOreMoreIncomingEdges(stateFlowGraph);

		allEdgesConnectTwoStates(stateFlowGraph);
	}

	private void allEdgesConnectTwoStates(StateFlowGraph stateFlowGraph) {
		for (StateVertex state : stateFlowGraph.getAllStates()) {
			if (!stateFlowGraph.isInitialState(state)) {
				assertThat(stateFlowGraph.getIncomingClickable(state), is(not(empty())));
			}
		}
	}

	private void allStatesHaveOneOreMoreIncomingEdges(StateFlowGraph stateFlowGraph) {
		for (Eventable eventable : stateFlowGraph.getAllEdges()) {
			assertThat(eventable.getSourceStateVertex(), is(notNullValue()));
			assertThat(eventable.getTargetStateVertex(), is(notNullValue()));
		}
	}

}

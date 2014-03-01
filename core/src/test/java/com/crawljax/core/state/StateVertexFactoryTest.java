package com.crawljax.core.state;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.RunWithWebServer;
import org.junit.ClassRule;
import org.junit.Test;

public class StateVertexFactoryTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site");

	/**
	 * By creating a stat comparator that always returns true, there is only one state. This test asserts that only one
	 * state is added to the stateflowgraph.
	 *
	 * @throws Exception
	 */
	@Test
	public void whenStateVertexFactoryDefinedItIsUsedToCompareStates() throws Exception {
		CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("infinite.html");
		builder.setStateVertexFactory(new StateVertexFactory() {
			@Override
			public StateVertex newStateVertex(int id, String url, String name, String dom, String strippedDom) {
				return new StateVertexImpl(id, url, name, dom, strippedDom) {

					@Override
					public int hashCode() {
						return 1;
					}

					@Override
					public boolean equals(Object object) {
						return true;
					}
				};
			}
		});
		int depth = 3;

		CrawljaxRunner runner = new CrawljaxRunner(builder.setMaximumDepth(depth).build());
		CrawlSession session = runner.call();

		assertThat(session.getStateFlowGraph(), hasStates(1));
		assertThat(runner.getReason(), is(ExitNotifier.ExitStatus.EXHAUSTED));
	}
}

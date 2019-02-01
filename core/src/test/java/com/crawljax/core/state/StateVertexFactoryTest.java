package com.crawljax.core.state;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.RunWithWebServer;
import org.junit.ClassRule;
import org.junit.Test;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StateVertexFactoryTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site");

	/**
	 * By creating a stat comparator that always returns true, there is only one
	 * state. This test asserts that only one state is added to the state-flow graph.
	 */
	@Test
	public void whenStateVertexFactoryDefinedItIsUsedToCompareStates() {
		CrawljaxConfiguration.CrawljaxConfigurationBuilder builder =
				SERVER.newConfigBuilder("infinite.html");
		builder.setStateVertexFactory(new StateVertexFactory() {
			@Override
			public StateVertex newStateVertex(int id, String url, String name, String dom,
					String strippedDom,
					EmbeddedBrowser browser) {
				return new StateVertexImpl(id, url, name, dom, strippedDom) {

					private static final long serialVersionUID = 8037922282301084581L;

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

		builder.setBrowserConfig(new BrowserConfiguration(BrowserProvider.getBrowserType()));

		CrawljaxRunner runner = new CrawljaxRunner(builder.setMaximumDepth(depth).build());
		CrawlSession session = runner.call();

		assertThat(session.getStateFlowGraph(), hasStates(1));
		assertThat(runner.getReason(), is(ExitNotifier.ExitStatus.EXHAUSTED));
	}
}

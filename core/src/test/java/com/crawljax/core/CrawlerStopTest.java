package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;

@Category(BrowserTest.class)
public class CrawlerStopTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site");

	@Test
	public void maximumDepthIsOblidged() throws Exception {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("infinite.html");
		int depth = 3;

		CrawljaxRunner runner = new CrawljaxRunner(builder.setMaximumDepth(depth).build());
		CrawlSession session = runner.call();

		assertThat(session.getStateFlowGraph(), hasStates(depth + 1));
		assertThat(runner.getReason(), is(ExitStatus.EXHAUSTED));
	}

	@Test(timeout = 60_000)
	public void maximumTimeIsObliged() throws Exception {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("infinite.html");

		CrawljaxRunner runner = new CrawljaxRunner(builder.setUnlimitedCrawlDepth()
		        .setMaximumRunTime(25, TimeUnit.SECONDS)
		        .build());
		runner.call();
		assertThat(runner.getReason(), is(ExitStatus.MAX_TIME));

	}

	@Test(timeout = 60_000)
	public void maximumStatesIsObliged() throws Exception {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("infinite.html");

		CrawljaxRunner runner = new CrawljaxRunner(builder.setUnlimitedCrawlDepth()
		        .setMaximumStates(3)
		        .build());
		CrawlSession session = runner.call();
		assertThat(session.getStateFlowGraph(), hasStates(3));
		assertThat(runner.getReason(), is(ExitStatus.MAX_STATES));

	}

	@Test(timeout = 60_000)
	public void whenStopIsCalledTheCrawlerStopsGracefully() throws Exception {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("infinite.html");

		CrawljaxRunner runner = new CrawljaxRunner(builder.setUnlimitedCrawlDepth()
		        .setUnlimitedCrawlDepth()
		        .setUnlimitedStates()
		        .build());

		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(runner);
		Thread.sleep(TimeUnit.SECONDS.toMillis(15));
		runner.stop();
		executor.shutdown();
		executor.awaitTermination(30, TimeUnit.SECONDS);
		assertThat(runner.getReason(), is(ExitStatus.STOPPED));

	}

	@Test(timeout = 60_000)
	public void whenCrawljaxIsShutDownByAPluginItShutsDown() throws Exception {
		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("infinite.html");

		CrawljaxRunner runner = new CrawljaxRunner(builder.setUnlimitedCrawlDepth()
		        .addPlugin(new OnNewStatePlugin() {

			        private int count = 0;

			        @Override
			        public void onNewState(CrawlerContext context, StateVertex newState) {
				        if (count == 2) {
					        context.stop();
				        }
				        count++;
			        }

			        @Override
			        public String toString() {
				        return "Stop crawljax plugin";
			        }
		        })
		        .setUnlimitedCrawlDepth()
		        .setUnlimitedStates()
		        .build());

		CrawlSession session = runner.call();
		assertThat(session.getStateFlowGraph(), hasStates(3));
		assertThat(runner.getReason(), is(ExitStatus.STOPPED));

	}
}

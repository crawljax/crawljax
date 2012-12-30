package com.crawljax.core;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BrowserTest;

/**
 * Test class for the Crawler testing.
 */
@Category(BrowserTest.class)
public class CrawlerTest {

	private Collection<List<Eventable>> paths;
	private StateVertex index;

	private CrawljaxConfiguration buildController() throws ConfigurationException {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification spec =
		        new CrawlSpecification(
		                "file://"
		                        + new File("src/test/resources/site/crawler/index.html")
		                                .getAbsolutePath());
		spec.click("a");
		config.setCrawlSpecification(spec);
		return config;
	}

	@Before
	public void setupController() throws ConfigurationException, CrawljaxException {
		CrawljaxController controller = new CrawljaxController(buildController());
		controller.run();
		paths = controller.getSession().getCrawlPaths();
		index = controller.getSession().getInitialState();
	}

	@Test
	public void testCrawler() throws ConfigurationException {
		final TestController controller = new TestController(buildController(), index);

		// Prevent dead-lock
		// controller.getBrowserPool().freeBrowser(controller.getCrawler().getBrowser());

		for (final List<Eventable> path : paths) {
			Crawler c = new Crawler(controller, path, "Follow Path") {
				@Override
				public void run() {
					try {
						super.init();
					} catch (InterruptedException e) {
						Assert.fail(e.getMessage());
					}
					CrawlPath newPath = controller.getSession().getCurrentCrawlPath();
					Assert.assertEquals(
					        "Path found by Controller driven Crawling equals the path found in the Crawler",
					        path, newPath);
					super.shutdown();
				}
			};
			c.run();
		}

		controller.getBrowserPool().shutdown();
	}

	private static class TestController extends CrawljaxController {
		CrawlSession localSession;
		StateFlowGraph g;
		StateVertex i;

		public TestController(CrawljaxConfiguration config, StateVertex index)
		        throws ConfigurationException {
			super(config);
			i = index;
			g = new StateFlowGraph(i);
			localSession =
			        new CrawlSession(this.getBrowserPool(), g, i, System.currentTimeMillis());
		}

		@Override
		public CrawlSession getSession() {
			return localSession;
		}
	}
}

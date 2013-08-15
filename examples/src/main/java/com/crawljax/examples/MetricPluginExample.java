package com.crawljax.examples;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.StateVertex;

/**
 * Crawljax works together with metrics by codehale. There are some metrics pre-installed in
 * Crawljax and you can add your own. This example shows you an an example of both.
 * 
 * @see <a href="http://metrics.codahale.com/">http://metrics.codahale.com/</a>
 */
public class MetricPluginExample {

	public static void main(String[] args) {
		CrawljaxConfiguration config =
		        CrawljaxConfiguration.builderFor("http://demo.crawljax.com")
		                .addPlugin(new MetricPlugin())
		                .build();

		new CrawljaxRunner(config).call();
	}

	/**
	 * This plugins implements {@link OnNewStatePlugin} so that for every new state we can calculate
	 * the DOM size. It also implements the {@link PostCrawlingPlugin} allowing us to print the
	 * result when the crawl is done.
	 */
	private static class MetricPlugin implements OnNewStatePlugin, PostCrawlingPlugin {

		private AtomicBoolean firstState = new AtomicBoolean(true);

		private final String metricName = MetricRegistry.name(MetricPluginExample.class,
		        "domsize");

		private Slf4jReporter reporter;

		@Override
		public void onNewState(CrawlerContext context, StateVertex newState) {
			if (firstState.getAndSet(false)) {
				reporter = Slf4jReporter.forRegistry(context.getRegistry())
				        .outputTo(LoggerFactory.getLogger(MetricPluginExample.class))
				        .build();
			}

			int domSizeInKb =
			        context.getBrowser().getUnStrippedDom().getBytes().length / 1000;

			// Get the histogram or create one. After that update with the DOM value we found.
			context.getRegistry().histogram(metricName).update(domSizeInKb);
		}

		/*
		 * The crawl is done. Lets print the metrics to the logger.
		 */
		@Override
		public void postCrawling(CrawlSession session, ExitStatus exitReason) {
			reporter.report();
		}
	}
}

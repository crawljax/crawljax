package com.crawljax.core;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CoreModule.CrawlerDoneLatch;
import com.crawljax.di.CrawlSessionProvider;

/**
 * Starts and shuts down the crawl.
 */
@Singleton
public class CrawlController implements Callable<CrawlSession> {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlController.class);

	private final Provider<CrawlTaskConsumer> consumerFactory;
	private final ExecutorService executor;
	private final BrowserConfiguration config;
	private final CountDownLatch consumersDoneLatch;

	private final CrawlSessionProvider crawlSessionProvider;

	private final Plugins plugins;

	private final long maximumCrawlTime;

	@Inject
	CrawlController(ExecutorService executor, Provider<CrawlTaskConsumer> consumerFactory,
	        CrawljaxConfiguration config,
	        @CrawlerDoneLatch CountDownLatch consumersDoneLatch,
	        CrawlSessionProvider crawlSessionProvider) {
		this.executor = executor;
		this.consumerFactory = consumerFactory;
		this.config = config.getBrowserConfig();
		this.plugins = config.getPlugins();
		this.consumersDoneLatch = consumersDoneLatch;
		this.crawlSessionProvider = crawlSessionProvider;
		this.maximumCrawlTime = config.getMaximumRuntime();
	}

	/**
	 * Run the configured crawl.
	 * 
	 * @return
	 */
	@Override
	public CrawlSession call() {
		setMaximumCrawlTimeIfNeeded();
		CrawlTaskConsumer firstConsumer = consumerFactory.get();
		StateVertex firstState = firstConsumer.crawlIndex();
		crawlSessionProvider.setup(firstState);
		plugins.runOnNewStatePlugins(crawlSessionProvider.get(), firstState);
		executeConsumers(firstConsumer);
		return crawlSessionProvider.get();
	}

	private void setMaximumCrawlTimeIfNeeded() {
		if (maximumCrawlTime == 0) {
			return;
		}
		executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					LOG.debug("Waiting {} before killing the crawler", maximumCrawlTime);
					Thread.sleep(maximumCrawlTime);
					LOG.info("Time is up! Shutting down...");
					consumersDoneLatch.countDown();
				} catch (InterruptedException e) {
					LOG.debug("Crawler finished before maximum crawltime exceeded");
				}

			}
		});

	}

	private void executeConsumers(CrawlTaskConsumer firstConsumer) {
		LOG.debug("Starting {} consumers", config.getNumberOfBrowsers());
		executor.submit(firstConsumer);
		for (int i = 1; i < config.getNumberOfBrowsers(); i++) {
			executor.submit(consumerFactory.get());
		}
		try {
			consumersDoneLatch.await();
		} catch (InterruptedException e) {
			LOG.warn("The crawl was interrupted before it finished. Shutting down...");
		} finally {
			shutDown();
			plugins.runPostCrawlingPlugins(crawlSessionProvider.get());
		}
	}

	private void shutDown() {
		LOG.info("Received shutdown notice");
		executor.shutdownNow();
		try {
			LOG.debug("Waiting for task consumers to stop...");
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.warn("Interrupted before being able to shut down executor pool", e);
		}
		LOG.debug("terminated");
	}

}

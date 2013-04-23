package com.crawljax.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.ExitNotifier.Reason;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.StateVertex;
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

	private final CrawlSessionProvider crawlSessionProvider;

	private final Plugins plugins;

	private final long maximumCrawlTime;

	private final ExitNotifier exitNotifier;

	private Reason exitReason;

	@Inject
	CrawlController(ExecutorService executor, Provider<CrawlTaskConsumer> consumerFactory,
	        CrawljaxConfiguration config,
	        ExitNotifier exitNotifier,
	        CrawlSessionProvider crawlSessionProvider) {
		this.executor = executor;
		this.consumerFactory = consumerFactory;
		this.exitNotifier = exitNotifier;
		this.config = config.getBrowserConfig();
		this.plugins = config.getPlugins();
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

	/**
	 * @return The {@link Reason} crawljax stopped or <code>null</code> when it hasn't stopped yet.
	 */
	public Reason getReason() {
		return exitReason;
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
					exitNotifier.signalTimeIsUp();
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
			exitReason = exitNotifier.awaitTermination();
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

package com.crawljax.core;

import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CrawlSessionProvider;
import com.crawljax.forms.FormInputValueHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Starts and shuts down the crawl.
 */
@Singleton
public class CrawlController implements Callable<CrawlSession> {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlController.class);

	static {
		System.setProperty("webdriver.http.factory", "apache");
	}

	private final Provider<CrawlTaskConsumer> consumerFactory;
	private final ExecutorService executor;
	private final CrawljaxConfiguration config;

	private final CrawlSessionProvider crawlSessionProvider;

	private final Plugins plugins;

	private final long maximumCrawlTime;

	private final ExitNotifier exitNotifier;

	private ExitStatus exitReason;

	@Inject CrawlController(ExecutorService executor, Provider<CrawlTaskConsumer> consumerFactory,
			CrawljaxConfiguration config,
			ExitNotifier exitNotifier, CrawlSessionProvider crawlSessionProvider,
			Plugins plugins) {
		this.executor = executor;
		this.consumerFactory = consumerFactory;
		this.exitNotifier = exitNotifier;
		this.config = config;
		this.plugins = plugins;
		this.crawlSessionProvider = crawlSessionProvider;
		this.maximumCrawlTime = config.getMaximumRuntime();
	}

	/**
	 * Run the configured crawl. This method blocks until the crawl is done.
	 *
	 * @return the CrawlSession once the crawl is done.
	 */
	@Override
	public CrawlSession call() {
		setMaximumCrawlTimeIfNeeded();
		plugins.runPreCrawlingPlugins(config);
		CrawlTaskConsumer firstConsumer = consumerFactory.get();
		StateVertex firstState = firstConsumer.crawlIndex();
		crawlSessionProvider.setup(firstState, firstConsumer);
		plugins.runOnNewStatePlugins(firstConsumer.getContext(), firstState);
		executeConsumers(firstConsumer);
		return crawlSessionProvider.get();
	}

	/**
	 * @return Same as {@link #call()}
	 * @see #call()
	 */
	public CrawlSession run() {
		return call();
	}

	/**
	 * @return The {@link ExitStatus} crawljax stopped or <code>null</code> when it
	 * hasn't stopped yet.
	 */
	public ExitStatus getReason() {
		return exitReason;
	}

	private void setMaximumCrawlTimeIfNeeded() {
		if (maximumCrawlTime == 0) {
			return;
		}
		executor.submit(() -> {
			try {
				LOG.debug("Waiting {} before killing the crawler", maximumCrawlTime);
				Thread.sleep(maximumCrawlTime);
				LOG.info("Time is up! Shutting down...");
				exitNotifier.signalTimeIsUp();
			} catch (InterruptedException e) {
				LOG.debug("Crawler finished before maximum crawl time exceeded");
			}

		});

	}

	private void executeConsumers(CrawlTaskConsumer firstConsumer) {
		LOG.debug("Starting {} consumers", config.getBrowserConfig().getNumberOfBrowsers());
		executor.submit(firstConsumer);
		for (int i = 1; i < config.getBrowserConfig().getNumberOfBrowsers(); i++) {
			executor.submit(consumerFactory.get());
		}
		try {
			exitReason = exitNotifier.awaitTermination();
		} catch (InterruptedException e) {
			LOG.warn("The crawl was interrupted before it finished. Shutting down...");
			exitReason = ExitStatus.ERROR;
		} finally {
			shutDown();
			plugins.runPostCrawlingPlugins(crawlSessionProvider.get(), exitReason);
			LOG.info("Shutdown process complete");
		}
	}

	private void shutDown() {
		LOG.info("Received shutdown notice. Reason is {}", exitReason);
		executor.shutdownNow();
		try {
			LOG.debug("Waiting for task consumers to stop...");
			executor.awaitTermination(15, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOG.warn("Interrupted before being able to shut down executor pool", e);
			exitReason = ExitStatus.ERROR;
		} finally {
			/*
			 * If this was a training crawl, write the form inputs to the output directory.
			 */
			if (config.getCrawlRules().getFormFillMode().equals(FormFillMode.TRAINING)
					|| config.getCrawlRules().getFormFillMode()
					.equals(FormFillMode.XPATH_TRAINING)) {
				FormInputValueHelper.serializeFormInputs(this.config.getSiteDir());
			}
		}
		LOG.debug("terminated");
	}

	void stop() {
		exitNotifier.stop();
	}

}

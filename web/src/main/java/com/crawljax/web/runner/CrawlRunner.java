package com.crawljax.web.runner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.web.model.Configuration;
import com.crawljax.web.model.Configurations;
import com.crawljax.web.model.CrawlRecord;
import com.crawljax.web.model.CrawlRecords;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CrawlRunner {
	private final Configurations configurations;
	private final CrawlRecords crawlRecords;

	@Inject
	public CrawlRunner(Configurations configurations, CrawlRecords crawlRecords) {
		this.configurations = configurations;
		this.crawlRecords = crawlRecords;
	}

	public void queue(CrawlRecord record) {
		run(record);
	}

	private void run(CrawlRecord record) {
		Configuration config = configurations.findByID(record.getConfigurationId());

		// Set Timestamps
		Date timestamp = new Date();
		record.setStartTime(timestamp);
		config.setLastRun(timestamp);
		crawlRecords.update(record);
		configurations.update(config);

		// Build Configuration
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(config.getUrl());
		builder.setBrowserConfig(new BrowserConfiguration(config.getBrowser(), config
		        .getNumBrowsers()));

		if (config.getMaxDepth() > 0)
			builder.setMaximumDepth(config.getMaxDepth());
		else
			builder.setUnlimitedCrawlDepth();

		if (config.getMaxState() > 0)
			builder.setMaximumStates(config.getMaxState());
		else
			builder.setUnlimitedStates();

		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().clickOnce(config.isClickOnce());
		builder.crawlRules().insertRandomDataInInputForms(config.isRandomFormInput());
		builder.crawlRules().waitAfterEvent(config.getEventWaitTime(), TimeUnit.MILLISECONDS);
		builder.crawlRules()
		        .waitAfterReloadUrl(config.getReloadWaitTime(), TimeUnit.MILLISECONDS);

		// run Crawljax
		CrawljaxController crawljax = new CrawljaxController(builder.build());
		crawljax.run();
	}
}

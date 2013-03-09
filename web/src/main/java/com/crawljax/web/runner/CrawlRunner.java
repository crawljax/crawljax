package com.crawljax.web.runner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.crawljax.condition.Condition;
import com.crawljax.condition.JavaScriptCondition;
import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.NotUrlCondition;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.condition.RegexCondition;
import com.crawljax.condition.UrlCondition;
import com.crawljax.condition.VisibleCondition;
import com.crawljax.condition.XPathCondition;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.web.model.Configuration;
import com.crawljax.web.model.Configurations;
import com.crawljax.web.model.CrawlRecord;
import com.crawljax.web.model.CrawlRecords;
import com.crawljax.web.model.NameValuePair;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CrawlRunner {
	private static final int WORKERS = 2;
	private final Configurations configurations;
	private final CrawlRecords crawlRecords;
	private final ExecutorService pool;

	@Inject
	public CrawlRunner(Configurations configurations, CrawlRecords crawlRecords) {
		this.configurations = configurations;
		this.crawlRecords = crawlRecords;
		this.pool = Executors.newFixedThreadPool(WORKERS);
	}

	public void queue(int id) {
		pool.submit(new CrawlExecution(id));
	}

	private class CrawlExecution implements Runnable {
		private final int crawlId;

		public CrawlExecution(int id) {
			this.crawlId = id;
		}

		@Override
		public void run() {
			try {
				CrawlRecord record = crawlRecords.findByID(crawlId);
				Configuration config = configurations.findByID(record.getConfigurationId());

				// Set Timestamps
				Date timestamp = new Date();
				record.setStartTime(timestamp);
				config.setLastCrawl(timestamp);
				crawlRecords.update(record);
				configurations.update(config);

				// Build Configuration
				CrawljaxConfigurationBuilder builder =
				        CrawljaxConfiguration.builderFor(config.getUrl());
				builder.setBrowserConfig(new BrowserConfiguration(config.getBrowser(), config
				        .getNumBrowsers(), config.isBootBrowser()));

				if (config.getMaxDepth() > 0)
					builder.setMaximumDepth(config.getMaxDepth());
				else
					builder.setUnlimitedCrawlDepth();

				if (config.getMaxState() > 0)
					builder.setMaximumStates(config.getMaxState());
				else
					builder.setUnlimitedStates();

				if (config.getMaxDuration() > 0)
					builder.setMaximumRunTime(config.getMaxDuration(), TimeUnit.MINUTES);
				else
					builder.setUnlimitedRuntime();

				builder.crawlRules().clickOnce(config.isClickOnce());
				builder.crawlRules().insertRandomDataInInputForms(config.isRandomFormInput());
				builder.crawlRules().waitAfterEvent(config.getEventWaitTime(),
				        TimeUnit.MILLISECONDS);
				builder.crawlRules().waitAfterReloadUrl(config.getReloadWaitTime(),
				        TimeUnit.MILLISECONDS);

				// Click Rules
				builder.crawlRules().clickDefaultElements();

				// Form Input
				if (config.getFormInputValues().size() > 0) {
					InputSpecification input = new InputSpecification();
					for (NameValuePair p : config.getFormInputValues())
						input.field(p.getName()).setValue(p.getValue());
					builder.crawlRules().setInputSpec(input);
				}

				// Crawl Conditions
				if (config.getPageConditions().size() > 0) {
					for (com.crawljax.web.model.Condition c : config.getPageConditions()) {
						builder.crawlRules().addCrawlCondition(
						        c.getCondition().toString() + c.getExpression(),
						        getConditionFromConfig(c));
					}
				}

				// run Crawljax
				CrawljaxController crawljax = new CrawljaxController(builder.build());
				crawljax.run();

				// set duration
				long duration = (new Date()).getTime() - timestamp.getTime();
				record.setDuration(duration);
				config.setLastDuration(duration);
				crawlRecords.update(record);
				configurations.update(config);
			} catch (Exception e) {
				e.printStackTrace();
				pool.shutdown();
			}
		}

		private Condition getConditionFromConfig(com.crawljax.web.model.Condition c) {
			Condition condition = null;
			Identification id = null;
			switch (c.getCondition()) {
				case url:
					condition = new UrlCondition(c.getExpression());
					break;
				case notUrl:
					condition = new NotUrlCondition(c.getExpression());
					break;
				case javascript:
					condition = new JavaScriptCondition(c.getExpression());
					break;
				case regex:
					condition = new RegexCondition(c.getExpression());
					break;
				case notRegex:
					condition = new NotRegexCondition(c.getExpression());
					break;
				case visibleId:
					id = new Identification(How.id, c.getExpression());
					condition = new VisibleCondition(id);
					break;
				case notVisibleId:
					id = new Identification(How.id, c.getExpression());
					condition = new VisibleCondition(id);
					break;
				case visibleText:
					id = new Identification(How.text, c.getExpression());
					condition = new VisibleCondition(id);
					break;
				case notVisibleText:
					id = new Identification(How.text, c.getExpression());
					condition = new VisibleCondition(id);
					break;
				case visibleTag:
					id = new Identification(How.tag, c.getExpression());
					condition = new VisibleCondition(id);
					break;
				case notVisibleTag:
					id = new Identification(How.tag, c.getExpression());
					condition = new VisibleCondition(id);
					break;
				case xPath:
					condition = new XPathCondition(c.getExpression());
					break;
				case notXPath:
					condition = new NotXPathCondition(c.getExpression());
					break;
				default:
					break;
			}
			return condition;
		}
	}
}

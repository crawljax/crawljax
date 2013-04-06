package com.crawljax.web.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.MDC;

import com.crawljax.condition.Condition;
import com.crawljax.condition.JavaScriptCondition;
import com.crawljax.condition.NotRegexCondition;
import com.crawljax.condition.NotUrlCondition;
import com.crawljax.condition.NotVisibleCondition;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.condition.RegexCondition;
import com.crawljax.condition.UrlCondition;
import com.crawljax.condition.VisibleCondition;
import com.crawljax.condition.XPathCondition;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlElement;
import com.crawljax.core.configuration.CrawlRules.CrawlRulesBuilder;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.oraclecomparator.OracleComparator;
import com.crawljax.oraclecomparator.comparators.AttributeComparator;
import com.crawljax.oraclecomparator.comparators.DateComparator;
import com.crawljax.oraclecomparator.comparators.EditDistanceComparator;
import com.crawljax.oraclecomparator.comparators.PlainStructureComparator;
import com.crawljax.oraclecomparator.comparators.RegexComparator;
import com.crawljax.oraclecomparator.comparators.ScriptComparator;
import com.crawljax.oraclecomparator.comparators.SimpleComparator;
import com.crawljax.oraclecomparator.comparators.StyleComparator;
import com.crawljax.oraclecomparator.comparators.XPathExpressionComparator;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.crawljax.web.LogWebSocketServlet;
import com.crawljax.web.model.ClickRule;
import com.crawljax.web.model.ClickRule.RuleType;
import com.crawljax.web.model.Configuration;
import com.crawljax.web.model.Configurations;
import com.crawljax.web.model.CrawlRecord;
import com.crawljax.web.model.CrawlRecord.CrawlStatusType;
import com.crawljax.web.model.CrawlRecords;
import com.crawljax.web.model.NameValuePair;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CrawlRunner {
	private static final int WORKERS = 2;
	private final Configurations configurations;
	private final CrawlRecords crawlRecords;
	private final ObjectMapper mapper;
	private final ExecutorService pool;

	@Inject
	public CrawlRunner(Configurations configurations, CrawlRecords crawlRecords,
	        ObjectMapper mapper) {
		this.configurations = configurations;
		this.crawlRecords = crawlRecords;
		this.mapper = mapper;
		this.pool = Executors.newFixedThreadPool(WORKERS);
	}

	public void queue(CrawlRecord record) {
		int id = record.getId();
		String json = null;
		try {
			record.setCrawlStatus(CrawlStatusType.queued);
			json = mapper.writeValueAsString(record);
			LogWebSocketServlet.sendToAll("queue-" + json);
			pool.submit(new CrawlExecution(id));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class CrawlExecution implements Runnable {
		private final int crawlId;

		public CrawlExecution(int id) {
			this.crawlId = id;
		}

		@Override
		public void run() {
			Date timestamp = null;
			CrawlRecord record = crawlRecords.findByID(crawlId);
			MDC.put("crawl_record", Integer.toString(crawlId));
			try {
				Configuration config = configurations.findByID(record.getConfigurationId());
				record.setCrawlStatus(CrawlStatusType.initializing);
				LogWebSocketServlet.sendToAll("init-" + Integer.toString(crawlId));

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
				if (config.isClickDefault())
					builder.crawlRules().clickDefaultElements();
				else if (config.getClickRules().size() > 0) {
					for (ClickRule r : config.getClickRules()) {
						CrawlElement element;
						if (r.getRule() == RuleType.click)
							element = builder.crawlRules().click(r.getElementTag());
						else
							element = builder.crawlRules().dontClick(r.getElementTag());

						if (r.getConditions().size() > 0) {
							for (com.crawljax.web.model.Condition c : r.getConditions()) {
								if (c.getCondition().toString().startsWith("w")) {
									switch (c.getCondition()) {
										case wAttribute:
											String[] s =
											        c.getExpression().replace(" ", "").split("=");
											element.withAttribute(s[0], s[1]);
											break;
										case wText:
											element.withText(c.getExpression());
											break;
										case wXPath:
											element.underXPath(c.getExpression());
											break;
										default:
											break;
									}
								} else
									element.when(getConditionFromConfig(c));
							}
						}
					}
				}

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

				// Invariants
				if (config.getInvariants().size() > 0) {
					for (com.crawljax.web.model.Condition c : config.getInvariants()) {
						builder.crawlRules().addInvariant(
						        c.getCondition().toString() + c.getExpression(),
						        getConditionFromConfig(c));
					}
				}

				// Comparators
				if (config.getComparators().size() > 0)
					setComparatorsFromConfig(config.getComparators(), builder.crawlRules());

				// Add Crawl Overview
				builder.addPlugin(new CrawlOverview(new File(record.getOutputFolder()
				        + File.separatorChar + "crawloverview")));

				// Build Crawljax
				CrawljaxController crawljax = new CrawljaxController(builder.build());

				// Set Timestamps
				timestamp = new Date();
				record.setStartTime(timestamp);
				record.setCrawlStatus(CrawlStatusType.running);
				crawlRecords.update(record);
				LogWebSocketServlet.sendToAll("run-" + Integer.toString(crawlId));

				// run Crawljax
				crawljax.run();

				// set duration
				long duration = (new Date()).getTime() - timestamp.getTime();
				record.setDuration(duration);
				config.setLastCrawl(timestamp);
				config.setLastDuration(duration);
				record.setCrawlStatus(CrawlStatusType.success);
				crawlRecords.update(record);
				configurations.update(config);
				LogWebSocketServlet.sendToAll("success-" + Integer.toString(crawlId));
			} catch (Exception e) {
				e.printStackTrace();
				record.setCrawlStatus(CrawlStatusType.failure);
				crawlRecords.update(record);
				LogWebSocketServlet.sendToAll("fail-" + Integer.toString(crawlId));
				pool.shutdown();
			} finally {
				MDC.remove("crawl_record");
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
					condition = new NotVisibleCondition(id);
					break;
				case visibleText:
					id = new Identification(How.text, c.getExpression());
					condition = new VisibleCondition(id);
					break;
				case notVisibleText:
					id = new Identification(How.text, c.getExpression());
					condition = new NotVisibleCondition(id);
					break;
				case visibleTag:
					id = new Identification(How.tag, c.getExpression());
					condition = new VisibleCondition(id);
					break;
				case notVisibleTag:
					id = new Identification(How.tag, c.getExpression());
					condition = new NotVisibleCondition(id);
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

		private void setComparatorsFromConfig(List<com.crawljax.web.model.Comparator> list,
		        CrawlRulesBuilder rules) {
			List<String> attributes = new ArrayList<String>();
			List<String> regexs = new ArrayList<String>();
			List<String> xpaths = new ArrayList<String>();

			for (com.crawljax.web.model.Comparator c : list) {
				switch (c.getType()) {
					case attribute:
						attributes.add(c.getExpression());
						break;
					case date:
						rules.addOracleComparator(new OracleComparator(c.getType().toString()
						        + c.getExpression(), new DateComparator()));
						break;
					case regex:
						regexs.add(c.getExpression());
						break;
					case script:
						rules.addOracleComparator(new OracleComparator(c.getType().toString()
						        + c.getExpression(), new ScriptComparator()));
						break;
					case distance:
						rules.addOracleComparator(new OracleComparator(c.getType().toString()
						        + c.getExpression(), new EditDistanceComparator(Double
						        .parseDouble(c.getExpression()))));
						break;
					case simple:
						rules.addOracleComparator(new OracleComparator(c.getType().toString()
						        + c.getExpression(), new SimpleComparator()));
						break;
					case plain:
						rules.addOracleComparator(new OracleComparator(c.getType().toString()
						        + c.getExpression(), new PlainStructureComparator()));
						break;
					case style:
						rules.addOracleComparator(new OracleComparator(c.getType().toString()
						        + c.getExpression(), new StyleComparator()));
						break;
					case xpath:
						xpaths.add(c.getExpression());
						break;
				}
			}
			// create collection comparators
			if (attributes.size() > 0)
				rules.addOracleComparator(new OracleComparator(
				        "attribute",
				        new AttributeComparator(attributes.toArray(new String[attributes.size()]))));
			if (regexs.size() > 0)
				rules.addOracleComparator(new OracleComparator("regex", new RegexComparator(
				        regexs.toArray(new String[regexs.size()]))));
			if (xpaths.size() > 0)
				rules.addOracleComparator(new OracleComparator("xpath",
				        new XPathExpressionComparator(xpaths.toArray(new String[xpaths.size()]))));
		}
	}
}

package com.crawljax.crawltests;

import org.eclipse.jetty.util.resource.Resource;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputField;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.test.BaseCrawler;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleInputSiteCrawl extends BaseCrawler {

	public static final int NUMBER_OF_STATES = 2;
	public static final int NUMBER_OF_EDGES = 1;

	public SimpleInputSiteCrawl() {
		super(Resource.newClassPathResource("sites"), "simple-input-site");
	}

	@Override
	protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
		CrawljaxConfigurationBuilder builder = super.newCrawlConfigurationBuilder();
		builder.crawlRules().setInputSpec(getInputSpecification());
		return builder;
	}

	/**
	 * @return The {@link InputSpecification} for the input box in this crawl session. You can
	 *         override this methods to add more values. By default, it loads with two bad strings,
	 *         and one good string.
	 */
	public InputSpecification getInputSpecification() {
		InputSpecification inputSpecification = new InputSpecification();
		InputField field = inputSpecification.field("input");
		field.setValue("Good input");
		field.setValue("This doesnt work");
		field.setValue("Neither does this");

		return inputSpecification;
	}

}

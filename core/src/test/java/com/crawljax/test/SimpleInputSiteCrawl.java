package com.crawljax.test;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleInputSiteCrawl extends BaseCrawler {

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
	 * override this methods to add more values. By default, it loads with two bad strings,
	 * and one good string.
	 */
	public InputSpecification getInputSpecification() {
		InputSpecification inputSpecification = new InputSpecification();
		FormInput input = new FormInput(InputType.TEXT, new Identification(How.id, "input"));
		input.inputValues("Good input");
		inputSpecification.inputField(input);

		return inputSpecification;
	}

}

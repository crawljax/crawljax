package com.crawljax.crawltests;

import com.crawljax.core.configuration.InputField;
import com.crawljax.core.configuration.InputSpecification;

/**
 * Wraps a Crawljax instance the crawls the simplesite.
 */
public class SimpleInputSiteCrawl extends BaseCrawler {

	public static final int NUMBER_OF_STATES = 2;
	public static final int NUMBER_OF_EDGES = 1;

	public SimpleInputSiteCrawl() {
		super("simple-input-site");
	}

	@Override
	public void setup() {
		super.setup();
		getCrawlSpec().setInputSpecification(getInputSpecification());
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

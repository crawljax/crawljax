package com.crawljax.examples;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.crawljax.condition.NotXPathCondition;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.plugins.crawloverview.CrawlOverview;

/**
 * Example of running Crawljax with the CrawlOverview plugin on a single-page web app.
 */
public final class CrawljaxAdvancedExampleSettings {

	private static final long WAIT_TIME_AFTER_EVENT = 200;
	private static final long WAIT_TIME_AFTER_RELOAD = 20;
	private static final String //URL = "http://spci.st.ewi.tudelft.nl/demo/crawljax/";
	URL = "http://www.facebook.com/meeeeeeeeeeeeeeeeee";
	private static final String outputDir = "output";

	/**
	 * entry point
	 */
	public static void main(String[] args) {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		builder.crawlRules().insertRandomDataInInputForms(false);
		
		builder.setMaximumDepth(2);
		builder.setMaximumStates(25);
		
		//builder.setMaximumOutgoingEdgesPerState(3);
		builder.setMaximumStatesPerUrl(3);

		builder.crawlRules().click("a");

		// click these elements
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().click("div").withAttribute("class", "clickable");

		// but don't click these
		builder.crawlRules().dontClick("a").withAttribute("class", "ignore");
		builder.crawlRules().dontClick("a").underXPath("//DIV[@id='footer']");

		// Set timeouts
		builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

		// Add a condition that this XPath doesn't exits
		builder.crawlRules().addCrawlCondition("No spans with foo as class",
		        new NotXPathCondition(
		                "//*[@class='foo']"));

		// Set some input for fields
		builder.crawlRules().setInputSpec(getInputSpecification());

		// This will generate a nice output in the output directory.
		builder.addPlugin(new CrawlOverview(new File(outputDir)));

		CrawljaxController crawljax = new CrawljaxController(builder.build());
		crawljax.run();

	}

	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();
		Form contactForm = new Form();
		contactForm.field("male").setValues(true, false);
		contactForm.field("female").setValues(false, true);
		contactForm.field("name").setValues("Bob", "Alice", "John");
		contactForm.field("phone").setValues("1234567890", "1234888888", "");
		contactForm.field("mobile").setValues("123", "3214321421");
		contactForm.field("type").setValues("Student", "Teacher");
		contactForm.field("active").setValues(true);
		input.setValuesInForm(contactForm).beforeClickElement("button").withText("Save");
		return input;
	}

	private CrawljaxAdvancedExampleSettings() {

	}
}

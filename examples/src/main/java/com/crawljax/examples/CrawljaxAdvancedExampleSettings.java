package com.crawljax.examples;

import com.crawljax.condition.NotXPathCondition;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;

/**
 * Demo class for Crawljax.
 */
public final class CrawljaxAdvancedExampleSettings {

	private static final int WAIT_TIME_AFTER_EVENT = 200;
	private static final int WAIT_TIME_AFTER_RELOAD = 20;
	private static final String URL = "http://spci.st.ewi.tudelft.nl/demo/crawljax/";

	private CrawljaxAdvancedExampleSettings() {

	}

	private static CrawljaxConfiguration getCrawljaxConfiguration() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(getCrawlSpecification());
		return config;
	}

	private static CrawlSpecification getCrawlSpecification() {
		CrawlSpecification crawler = new CrawlSpecification(URL);

		// click these elements
		crawler.clickDefaultElements();
		crawler.click("div").withAttribute("class", "clickable");

		// but don't click these
		crawler.dontClick("a").withAttribute("class", "ignore");
		crawler.dontClick("a").underXPath("//DIV[@id='footer']");

		crawler.setWaitTimeAfterReloadUrl(WAIT_TIME_AFTER_RELOAD);
		crawler.setWaitTimeAfterEvent(WAIT_TIME_AFTER_EVENT);
		crawler.setInputSpecification(getInputSpecification());

		crawler.addCrawlCondition("No spans with foo as class", new NotXPathCondition(
		        "//*[@class='foo']"));
		return crawler;
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

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		CrawljaxController crawljax = new CrawljaxController(getCrawljaxConfiguration());
		crawljax.run();
	}

}

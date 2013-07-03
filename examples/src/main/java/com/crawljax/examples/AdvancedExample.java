package com.crawljax.examples;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.crawljax.core.plugin.HostInterfaceImpl;
import org.apache.commons.io.FileUtils;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.plugins.crawloverview.CrawlOverview;

/**
 * Example of running Crawljax with the CrawlOverview plugin on a single-page web app. The crawl
 * will produce output using the {@link CrawlOverview} plugin.
 */
public final class AdvancedExample {

	private static final long WAIT_TIME_AFTER_EVENT = 200;
	private static final long WAIT_TIME_AFTER_RELOAD = 20;
	private static final String URL = "http://demo.crawljax.com";

	/**
	 * Run this method to start the crawl.
	 * 
	 * @throws IOException
	 *             when the output folder cannot be created or emptied.
	 */
	public static void main(String[] args) throws IOException {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		builder.crawlRules().insertRandomDataInInputForms(false);

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
		        new NotXPathCondition("//*[@class='foo']"));

		// Set some input for fields
		builder.crawlRules().setInputSpec(getInputSpecification());

		// This will generate a nice output in the output directory.
		File outFolder = new File("output");
		if (outFolder.exists()) {
			FileUtils.deleteDirectory(outFolder);
		}
		builder.addPlugin(new CrawlOverview(new HostInterfaceImpl(outFolder, null)));

		// We want to use two browsers simultaneously.
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.firefox, 2));

		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();

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

}

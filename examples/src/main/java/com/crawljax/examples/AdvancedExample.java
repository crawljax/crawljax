package com.crawljax.examples;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.condition.NotXPathCondition;
import com.crawljax.condition.UrlCondition;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.plugins.crawloverview.CrawlOverview;

import crawljax.plugins.clickabledetector.ClickableDetectorPlugin;

public final class AdvancedExample {

	private static final long WAIT_TIME_AFTER_EVENT = 200;
	private static final long WAIT_TIME_AFTER_RELOAD = 20;
	private static final String URL = "http://www.yahoo.com";


	/**
	 * Run this method to start the crawl.
	 * 
	 * @throws IOException
	 *             when the output folder cannot be created or emptied.
	 */
	public static void main(String[] args) throws IOException {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
	//	builder.crawlRules().insertRandomDataInInputForms(false);

		builder.crawlRules().click("div");
		builder.setMaximumStates(3);
		builder.setMaximumDepth(3);


		try {
			ClickableDetectorPlugin.configure(builder,
			        ProxyConfiguration.manualProxyOn("127.0.0.1", 8084));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX, 1));
		System.setProperty("webdriver.firefox.bin",
		        "/ubc/ece/home/am/grads/janab/Firefox19/firefox/firefox");
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

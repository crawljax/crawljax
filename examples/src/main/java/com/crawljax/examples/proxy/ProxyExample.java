package com.crawljax.examples.proxy;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.plugins.proxy.WebScarabWrapper;

public class ProxyExample {

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

		// We are going to use the WebScarab proxy
		WebScarabWrapper proxy = new WebScarabWrapper();
		// Provide the JS file to be inserted
		proxy.addPlugin(new DOMInjector("inject.js"));
		builder.addPlugin(proxy);

		// Configure the proxy to use the port 8084 (you can change this of course)
		builder.setProxyConfig(ProxyConfiguration.manualProxyOn("127.0.0.1", 8084));

		// click these elements
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().click("div").withAttribute("class", "clickable");

		builder.setMaximumStates(4);

		// Set timeouts
		builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.firefox, 1));

		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();

	}

}

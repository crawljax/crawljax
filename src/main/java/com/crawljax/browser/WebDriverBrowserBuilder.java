package com.crawljax.browser;

import java.util.List;

import org.openqa.selenium.firefox.FirefoxProfile;

import com.crawljax.core.configuration.CrawljaxConfigurationReader;

/**
 * This class represents the default Crawljax used implementation of the BrowserBuilder. It's based
 * on the WebDriver implementations offered by Crawljax.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class WebDriverBrowserBuilder implements BrowserBuilder {

	/**
	 * Build a new WebDriver based EmbeddedBrowser.
	 * 
	 * @see com.crawljax.browser.BrowserBuilder#
	 *      buildEmbeddedBrowser(com.crawljax.browser.EmbeddedBrowser.BrowserType)
	 * @param configuration
	 *            the configuration object to read the config values from
	 * @return the new build WebDriver based embeddedBrowser
	 */
	@Override
	public EmbeddedBrowser buildEmbeddedBrowser(CrawljaxConfigurationReader configuration) {
		// Retrieve the config values used
		List<String> filterAttributes = configuration.getFilterAttributeNames();
		int crawlWaitReload = configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl();
		int crawlWaitEvent = configuration.getCrawlSpecificationReader().getWaitAfterEvent();

		// Determine the requested browser type
		switch (configuration.getBrowser()) {
			case firefox:
				if (configuration.getProxyConfiguration() != null) {
					return new WebDriverFirefox(configuration.getProxyConfiguration(),
					        filterAttributes, crawlWaitReload, crawlWaitEvent);
				}

				if (configuration.getThreadConfigurationReader().getUseFastBooting()) {
					FirefoxProfile fp = new FirefoxProfile();
					fp.setPort(configuration.getThreadConfigurationReader().getPortNumber());
					return new WebDriverFirefox(fp, filterAttributes, crawlWaitReload,
					        crawlWaitEvent);
				}

				return new WebDriverFirefox(filterAttributes, crawlWaitReload, crawlWaitEvent);

			case ie:
				return new WebDriverIE(filterAttributes, crawlWaitReload, crawlWaitEvent);

			case chrome:
				return new WebDriverChrome(filterAttributes, crawlWaitReload, crawlWaitEvent);

			case remote:
				return new WebDriverRemote(filterAttributes, crawlWaitReload, crawlWaitEvent,
				        configuration.getRemoteHubUrl());

			default:
				return new WebDriverFirefox(filterAttributes, crawlWaitReload, crawlWaitEvent);
		}
	}

}

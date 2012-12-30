package com.crawljax.browser;

import com.crawljax.core.configuration.CrawljaxConfigurationReader;

/**
 * This is the main interface for building a concrete EmbeddedBrowser implementation. By default
 * Crawljax uses and offers WebDriver implementation, but when other implementation is requested the
 * EmbeddedBrowser Interface must be implemented in a Class and a new class must be created
 * implementing the BrowserBuilder interface. This new class must be supplied as object to the
 * CrawljaxConfiguration.setBrowserBuilder(BrowserBuilder).
 * <p/>
 * This can be as simple as:
 * 
 * <pre>
 * config.setBrowserBuilder(new BrowserBuilder() {
 * 	&#064;Override
 * 	public EmbeddedBrowser; buildEmbeddedBrowser(CrawljaxConfigurationReader configuration) {
 * 		return new WebDriverFirefox(configuration.getFilterAttributeNames(),
 * 		        configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl(),
 * 		        configuration.getCrawlSpecificationReader().getWaitAfterEvent());
 * 	}
 * });
 * </pre>
 * 
 * This interface offers a great flexibility and the possibility to retrieve the WebDriver core
 * classes to so more browser specific manipulation in plugins.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public interface EmbeddedBrowserBuilder {

	/**
	 * Build a new EmbeddedBrowser to be used during Crawling. This call is made everytime a new
	 * Browser instance is requested. When running in Multi-Threaded modes with for example 4
	 * browsers 4 times a call will be made to this function.
	 * 
	 * @param configuration
	 *            The configuration reader object to read the specific configuration options form.
	 * @return the new created instance of a EmbeddedBrowser to be used.
	 */
	EmbeddedBrowser buildEmbeddedBrowser(CrawljaxConfigurationReader configuration);
}

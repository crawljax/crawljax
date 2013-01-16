/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.crawljax.examples;

import org.junit.experimental.categories.Category;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.test.BrowserTest;

/**
 * Test specification for crawling the wivet benchmark project.
 */
@Category(BrowserTest.class)
public class WivetTest {

	/**
	 * @return the crawljax specification
	 */
	private static CrawlSpecification getCrawlSpec() {
		CrawlSpecification crawler =
		        new CrawlSpecification("http://caos.uab.es/~gruiz/test/wivet/");
		crawler.setMaximumStates(0);
		crawler.setDepth(0);
		crawler.setRandomInputInForms(true);
		crawler.clickMoreElements();
		crawler.setClickOnce(true);
		crawler.setWaitTimeAfterReloadUrl(20);
		crawler.setWaitTimeAfterEvent(200);
		crawler.dontClick("a").withAttribute("href", "../innerpages/2_2.php");
		crawler.dontClick("a").withText("Logout");
		return crawler;
	}

	/**
	 * @return the crawljax thread configuration
	 */
	private static ThreadConfiguration getThreadConfiguration() {
		ThreadConfiguration tc = new ThreadConfiguration();
		tc.setBrowserBooting(true);
		tc.setNumberBrowsers(1);
		tc.setNumberThreads(1);
		return tc;
	}

	/**
	 * @return the crawljax configuration
	 */
	private static CrawljaxConfiguration getConfig() {
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setThreadConfiguration(getThreadConfiguration());
		crawljaxConfiguration.setBrowser(EmbeddedBrowser.BrowserType.firefox);
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpec());
		/* proxy stuff */
		/*
		 * ProxyConfiguration p = new ProxyConfiguration(); p.setHostname("localhost");
		 * p.setPort(8080); crawljaxConfiguration.setProxyConfiguration(p);
		 */
		return crawljaxConfiguration;
	}

	/**
	 * @param args
	 *            none.
	 */
	public static void main(String[] args) {
		CrawljaxController crawljax = new CrawljaxController(getConfig());
		crawljax.run();
	}
}

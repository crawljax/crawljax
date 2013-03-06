/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.crawljax.examples;

import java.util.concurrent.TimeUnit;

import org.junit.experimental.categories.Category;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;

/**
 * Test specification for crawling the wivet benchmark project.
 */
@Category(BrowserTest.class)
public class WivetTest {

	/**
	 * @return the crawljax specification
	 */
	private static CrawljaxConfiguration getCrawlConfig() {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor("http://caos.uab.es/~gruiz/test/wivet/");
		builder.setUnlimitedCrawlDepth();
		builder.setUnlimitedStates();
		builder.setUnlimitedRuntime();
		builder.crawlRules().insertRandomDataInInputForms(true);
		builder.crawlRules().click("span", "div", "ol", "center", "li", "radio", "non", "meta",
		        "refresh", "xhr", "relative", "link", "self", "form", "input", "option", "img",
		        "p", "td", "tr", "table", "tbody");
		builder.crawlRules().clickOnce(true);
		builder.crawlRules().waitAfterReloadUrl(20, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(200, TimeUnit.MILLISECONDS);
		builder.crawlRules().dontClick("a").withAttribute("href", "../innerpages/2_2.php");
		builder.crawlRules().dontClick("a").withText("Logout");
		return builder.build();
	}

	/**
	 * @return the crawljax configuration
	 */
	private static CrawljaxConfiguration getConfig() {
		CrawljaxConfiguration crawljaxConfiguration = getCrawlConfig();
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

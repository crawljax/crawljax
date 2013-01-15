package ${package};

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.condition.UrlCondition;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.InputSpecification;

/**
 * Use the sample plugin in combination with Crawljax.
 * 
 */
public class CrawljaxRunner {

	public static void main(String[] args) {

		try {
			// configure the crawling engine
			CrawljaxConfiguration config = getConfig();

			// add your plugin
			config.addPlugin(new SamplePlugin());

			// initilize and run Crawljax
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure Crawljax to crawl Google.
	 */
	private static CrawljaxConfiguration getConfig() {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setBrowser(BrowserType.firefox);

		CrawlSpecification crawler = new CrawlSpecification("http://www.google.com");
		crawler.setWaitTimeAfterEvent(500);
		crawler.setWaitTimeAfterReloadUrl(500);

		// click on all anchor tags
		crawler.click("a");
		// and all input tags with "submit"
		crawler.click("input").withAttribute("type", "submit");

		// exclude these
		crawler.dontClick("a").underXPath("//DIV[@id='guser']");
		crawler.dontClick("a").withText("Language Tools");

		crawler.setRandomInputInForms(false);
		
		InputSpecification inputSpec = new InputSpecification();
		inputSpec.field("q").setValue("Crawljax");
		crawler.setInputSpecification(inputSpec);

		// Constrain the crawl to Google (no other web sites)
		crawler.addCrawlCondition("Only crawl Google", new UrlCondition("google"));

		// limit the crawling scope
		crawler.setMaximumStates(6);
		crawler.setDepth(2);

		config.setCrawlSpecification(crawler);

		return config;
	}
}

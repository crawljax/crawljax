package com.crawljax.plugins.savecrawlsession.example;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.plugins.savecrawlsession.SaveCrawlSession;
import com.crawljax.plugins.savecrawlsession.Utils;

/**
 * Crawls a default URL and produces a session.xml file.
 */
public class SaveCrawlSessionExample {

	/**
	 * @param args
	 *            args are ignored
	 */
	public static void main(String[] args) {

		CrawlSpecification spec = new CrawlSpecification(Utils.URL);
		spec.setMaximumStates(5);
		spec.clickDefaultElements();

		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setBrowser(BrowserType.firefox);
		config.setCrawlSpecification(spec);

		SaveCrawlSession saveCrawlSessionPlugin = new SaveCrawlSession(Utils.SESSION_XML);
		saveCrawlSessionPlugin.setOutputFolder(Utils.OUTPUTFOLDER);
		config.addPlugin(saveCrawlSessionPlugin);

		try {
			CrawljaxController controller = new CrawljaxController(config);
			controller.run();
		} catch (CrawljaxException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}

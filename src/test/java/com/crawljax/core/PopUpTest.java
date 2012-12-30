package com.crawljax.core;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class PopUpTest {
	static CrawljaxController crawljax;

	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException {
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification());
		crawljax = new CrawljaxController(crawljaxConfiguration);
	}

	@Test
	public void run() throws ConfigurationException, CrawljaxException {
		try {
			crawljax.run();
			assertEquals("Clickables", 2, crawljax.getSession().getStateFlowGraph().getAllEdges()
			        .size());

			assertEquals("States", 3, crawljax.getSession().getStateFlowGraph().getAllStates()
			        .size());

		} finally {
			crawljax.terminate(true);
		}
	}

	private static CrawlSpecification getCrawlSpecification() {
		File index = new File("src/test/resources/site/popup/index.html");
		CrawlSpecification crawler = new CrawlSpecification("file://" + index.getAbsolutePath());
		crawler.setWaitTimeAfterEvent(100);
		crawler.setWaitTimeAfterReloadUrl(100);
		crawler.setDepth(3);
		crawler.click("a");

		return crawler;
	}

}

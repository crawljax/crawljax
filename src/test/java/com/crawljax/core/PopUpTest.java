package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;

public class PopUpTest {
	static CrawljaxController crawljax;

	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException {
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification());
		crawljax = new CrawljaxController(crawljaxConfiguration);
	}

	@Test
	public void run() {
		try {
			crawljax.run();
			assertEquals("Clickables", 2, crawljax.getSession().getStateFlowGraph().getAllEdges()
			        .size());

			assertEquals("States", 3, crawljax.getSession().getStateFlowGraph().getAllStates()
			        .size());

		} catch (ConfigurationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (CrawljaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			crawljax.terminate(true);
		}
	}

	private static CrawlSpecification getCrawlSpecification() {
		File index = new File("src/test/site/popup/index.html");
		CrawlSpecification crawler = new CrawlSpecification("file://" + index.getAbsolutePath());
		crawler.setWaitTimeAfterEvent(100);
		crawler.setWaitTimeAfterReloadUrl(100);
		crawler.setDepth(3);
		crawler.click("a");

		return crawler;
	}

}

package com.crawljax.core;

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.test.BrowserTest;

/**
 * This class test the correct behavior of iFrames.
 */
@Category(BrowserTest.class)
public class IFrameTest extends IFrameSuper {
	@Test
	public void testIFrameCrawlable() throws ConfigurationException, CrawljaxException {
		try {
			crawljax.run();
			assertEquals("Clickables", 13, crawljax.getSession().getStateFlowGraph()
			        .getAllEdges().size());
			assertEquals("States", 13, crawljax.getSession().getStateFlowGraph().getAllStates()
			        .size());
		} finally {
			crawljax.terminate(true);
		}
	}
}
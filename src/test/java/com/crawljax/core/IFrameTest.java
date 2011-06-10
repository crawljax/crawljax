package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

/**
 * This class test the correct behavior of iFrames.
 * 
 * @author mesbah
 * @version $Id$
 */
public class IFrameTest extends IFrameSuper {
	@Test
	public void testIFrameCrawlable() {
		try {
			crawljax.run();
			assertEquals("Clickables", 13, crawljax.getSession().getStateFlowGraph()
			        .getAllEdges().size());
			assertEquals("States", 13, crawljax.getSession().getStateFlowGraph().getAllStates()
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
}
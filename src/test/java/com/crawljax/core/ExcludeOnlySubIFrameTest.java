// Copyright 2010 Google Inc. All Rights Reserved.
package com.crawljax.core;

import static org.junit.Assert.assertEquals;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.test.BrowserTest;

/**
 * This test checks that only one sub-iframes is ignored.
 */
@Category(BrowserTest.class)
public class ExcludeOnlySubIFrameTest extends IFrameSuper {

	@Override
	protected CrawlSpecification getCrawlSpecification() {
		CrawlSpecification spec = super.getCrawlSpecification();
		spec.dontCrawlFrame("frame1.frame10");
		return spec;
	}

	@Test
	public void testIFramesNotCrawled() throws ConfigurationException, CrawljaxException {
		try {
			crawljax.run();
			assertEquals("Clickables", 12, crawljax.getSession().getStateFlowGraph()
			        .getAllEdges().size());
			assertEquals("States", 12, crawljax.getSession().getStateFlowGraph().getAllStates()
			        .size());
		} finally {
			crawljax.terminate(true);
		}
	}
}
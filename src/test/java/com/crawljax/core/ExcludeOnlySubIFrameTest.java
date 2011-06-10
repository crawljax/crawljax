// Copyright 2010 Google Inc. All Rights Reserved.
package com.crawljax.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;

/**
 * This test checks that only one sub-iframes is ignored.
 * 
 * @author Stefan Lenselink <slenselink@google.com>
 * @version $Id$
 */
public class ExcludeOnlySubIFrameTest extends IFrameSuper {

	@Override
	protected CrawlSpecification getCrawlSpecification() {
		CrawlSpecification spec = super.getCrawlSpecification();
		spec.dontCrawlFrame("frame1.frame10");
		return spec;
	}

	@Test
	public void testIFramesNotCrawled() {
		try {
			crawljax.run();
			assertEquals("Clickables", 12, crawljax.getSession().getStateFlowGraph()
			        .getAllEdges().size());
			assertEquals("States", 12, crawljax.getSession().getStateFlowGraph().getAllStates()
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
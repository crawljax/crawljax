// Copyright 2010 Google Inc. All Rights Reserved.
package com.crawljax.core;

import static com.crawljax.matchers.StateFlowGraphMatches.hasEdges;
import static com.crawljax.matchers.StateFlowGraphMatches.hasStates;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;

/**
 * This test checks that all iframes are ignored.
 */
public class ExcludeIFrameTest extends IFrameSuper {

	@Override
	protected CrawlSpecification getCrawlSpecification() {
		CrawlSpecification spec = super.getCrawlSpecification();
		spec.dontCrawlFrame("frame1");
		spec.dontCrawlFrame("sub");
		spec.dontCrawlFrame("frame0");
		return spec;
	}

	@Test
	public void testIFramesNotCrawled() throws ConfigurationException, CrawljaxException {
		crawljax.run();
		assertThat(crawljax.getSession().getStateFlowGraph(), hasEdges(3));
		assertThat(crawljax.getSession().getStateFlowGraph(), hasStates(4));
	}
}
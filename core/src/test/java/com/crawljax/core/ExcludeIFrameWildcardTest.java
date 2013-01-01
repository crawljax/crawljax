// Copyright 2010 Google Inc. All Rights Reserved.
package com.crawljax.core;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.test.BrowserTest;

/**
 * This test checks that all iframes are ignored specified with a wild card.
 */
@Category(BrowserTest.class)
public class ExcludeIFrameWildcardTest extends IFrameSuper {

	@Override
	protected CrawlSpecification getCrawlSpecification() {
		CrawlSpecification spec = super.getCrawlSpecification();
		spec.dontCrawlFrame("frame%");
		spec.dontCrawlFrame("sub");
		return spec;
	}

	@Test
	public void testIFramesNotCrawled() throws ConfigurationException, CrawljaxException {
		crawljax.run();
		assertThat(crawljax.getSession().getStateFlowGraph(), hasEdges(3));
		assertThat(crawljax.getSession().getStateFlowGraph(), hasStates(4));
	}
}
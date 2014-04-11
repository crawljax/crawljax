package com.crawljax.condition;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.domcomparators.WhiteSpaceStripper;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.CrawlSession;
import com.crawljax.test.BaseCrawler;
import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class BrowserDoesntLeaveUrlTest {

	@Test
	public void whenJavaScriptNavigatesAwayFromPageItIsBlocked() throws Exception {
		BaseCrawler crawler = new BaseCrawler("navigate_other_urls.html") {
			@Override
			protected CrawljaxConfiguration.CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
				return super.newCrawlConfigurationBuilder()
						.addDomStripper(new WhiteSpaceStripper());
			}
		};
		// crawler.showWebSite();
		CrawlSession session = crawler.crawl();
		assertThat(session.getStateFlowGraph(), hasStates(5));
	}
}

package com.crawljax.core.configuration;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.Condition;
import com.crawljax.condition.UrlCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;

public class CrawljaxConfigurationBuilderTest {

	@Test
	public void whenBuilderCreatedTheJailUrlRuleIsAdded() {
		CrawljaxConfiguration config =
		        CrawljaxConfiguration.builderFor("https://www.example.com:8080/a/b?x=y#test")
		                .build();
		List<CrawlCondition> crawlConditions =
		        config.getCrawlRules().getPreCrawlConfig().getCrawlConditions();
		assertThat(crawlConditions, hasSize(1));
		Condition condition = crawlConditions.iterator().next().getCondition();
		assertThat(condition, instanceOf(UrlCondition.class));
		EmbeddedBrowser browser = Mockito.mock(EmbeddedBrowser.class);
		Mockito.when(browser.getCurrentUrl()).thenReturn(
		        "https://www.example.com:8080/c/f?df=ee#bla");
		assertThat(((UrlCondition) condition).check(browser), is(true));
	}
}

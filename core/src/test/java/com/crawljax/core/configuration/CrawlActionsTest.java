package com.crawljax.core.configuration;

import static com.crawljax.core.configuration.CrawlElementMatcher.withXpath;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CrawlActionsTest {

	private CrawlActionsBuilder actions;

	@Before
	public void setup() {
		actions = new CrawlActionsBuilder();
	}

	@Test
	public void testIncludesWork() {
		actions.click("a");
		actions.click("b").underXPath("123");
		actions.click("b").underXPath("sdfsdf");
		List<CrawlElement> crawlElements = actions.build().getLeft();
		assertThat(crawlElements, hasSize(3));
	}

	@Test
	public void testExcludesWork() {
		actions.dontClick("a");
		actions.dontClick("b").underXPath("123");
		actions.dontClick("b").underXPath("sdfsdf");
		List<CrawlElement> crawlElements = actions.build().getRight();
		assertThat(crawlElements, hasSize(3));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExcludeParents() {
		actions.click("a");
		actions.click("button");
		actions.dontClickChildrenOf("b").withId("someId");
		actions.dontClickChildrenOf("b").withClass("someClass");
		List<CrawlElement> crawlElements = actions.build().getRight();
		assertThat(crawlElements, hasSize(4));
		assertThat(
		        crawlElements,
		        containsInAnyOrder(withXpath("//B[@id='someId']//*"),
		                withXpath("//B[@id='someId']//*"),
		                withXpath("//B[@class='someClass']//*"),
		                withXpath("//B[@class='someClass']//*")));
	}
}

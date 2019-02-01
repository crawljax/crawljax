package com.crawljax.test;

public class SimpleInputSiteCrawlTest extends SimpleCrawlTest {

	private static final int NUMBER_OF_STATES = 2;
	private static final int NUMBER_OF_EDGES = 1;

	public SimpleInputSiteCrawlTest() {
		super(NUMBER_OF_STATES, NUMBER_OF_EDGES);
	}

	@Override
	public BaseCrawler getCrawler() {
		return new SimpleInputSiteCrawl();
	}

}

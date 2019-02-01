package com.crawljax.test;

public class SimpleJsSiteCrawlTest extends SimpleCrawlTest {

	private static final int NUMBER_OF_STATES = 11;
	private static final int NUMBER_OF_EDGES = 10;

	public SimpleJsSiteCrawlTest() {
		super(NUMBER_OF_STATES, NUMBER_OF_EDGES);
	}

	@Override
	public BaseCrawler getCrawler() {
		return new SimpleJsSiteCrawl();
	}

}

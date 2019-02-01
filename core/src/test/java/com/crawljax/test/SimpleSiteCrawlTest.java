package com.crawljax.test;

public class SimpleSiteCrawlTest extends SimpleCrawlTest {

	private static final int NUMBER_OF_STATES = 4;
	private static final int NUMBER_OF_EDGES = 5;

	public SimpleSiteCrawlTest() {
		super(NUMBER_OF_STATES, NUMBER_OF_EDGES);
	}

	@Override
	public BaseCrawler getCrawler() {
		return new SimpleSiteCrawl();
	}

}

package com.crawljax.test;

public class SimpleXpathCrawlTest extends SimpleCrawlTest {

    private static final int NUMBER_OF_STATES = 2;
    private static final int NUMBER_OF_EDGES = 2;

    public SimpleXpathCrawlTest() {
        super(NUMBER_OF_STATES, NUMBER_OF_EDGES);
    }

    @Override
    public BaseCrawler getCrawler() {
        return new SimpleXpathCrawl();
    }
}

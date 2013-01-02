Crawloverview plugin [![Build Status](https://travis-ci.org/crawljax/crawloverview-plugin.png?branch=master)](https://travis-ci.org/crawljax/crawloverview-plugin)
====================

Generates an HTML report with a snapshot overview of what is crawled by Crawljax.

Maven
-----

    <dependency>
      <groupId>com.crawljax.plugins</groupId>
      <artifactId>crawloverview</artifactId>
      <version>1.2</version>
    </dependency>

Using the plugin
----------------
    public class CrawlOverviewExample {

    private static final String URL = "http://google.com";

	public static void main(String[] args) {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification crawler = new CrawlSpecification(URL);
		crawler.setMaximumStates(5);
		crawler.clickDefaultElements();
		config.setCrawlSpecification(crawler);
		config.addPlugin(new CrawlOverview());
		try {
			CrawljaxController crawljax = new CrawljaxController(config);
			crawljax.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }
    }

The result will be generated in a folder called "crawloverview".

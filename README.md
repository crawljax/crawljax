Crawloverview plugin
====================

Generates an HTML report with a snapshot overview of what is crawled by Crawljax.



Using the plugin
================
    public class CrawlOverviewExample {

    private static final String URL = "http://google.com";

	public static void main(String[] args) {
		CrawljaxConfiguration config = new CrawljaxConfiguration();
		CrawlSpecification crawler = new CrawlSpecification(URL);
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

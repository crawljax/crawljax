package com.crawljax.core.plugin;

import com.crawljax.core.CrawlSession;

/**
 * Plugin type that is called after the crawling phase is finished. Examples: report generation,
 * test generation
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public interface PostCrawlingPlugin extends Plugin {

	/**
	 * Method that is called after the crawling is finished. Warning: changing the session can
	 * change the behavior of other post crawl plugins. It is not a copy!
	 * 
	 * @param session
	 *            the crawl session.
	 */
	void postCrawling(CrawlSession session);

}

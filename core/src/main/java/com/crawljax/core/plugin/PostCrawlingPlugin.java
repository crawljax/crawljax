package com.crawljax.core.plugin;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.ExitNotifier.ExitStatus;

/**
 * Plugin type that is called after the crawling phase is finished. Examples: report generation,
 * test generation
 */
public interface PostCrawlingPlugin extends Plugin {

	/**
	 * Method that is called after the crawling is finished. Warning: changing the session can
	 * change the behavior of other post crawl plugins. It is not a copy!
	 * 
	 * @param session
	 *            the crawl session.
	 * @param exitReason
	 *            The {@link ExitStatus} Crawljax stopped.
	 */
	void postCrawling(CrawlSession session, ExitStatus exitReason);

}

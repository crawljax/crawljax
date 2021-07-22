package com.crawljax.core.plugin;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlerContext;

/**
 * This interface denotes the Plugin type that is executed every time when a new Browser is created.
 * This can be used to do login, database changes, statistics etc. every time a new browser is
 * started.
 *
 * @author Stefan Lenselink &lt;S.R.Lenselink@student.tudelft.nl&gt;
 */
public interface OnBrowserClosePlugin extends Plugin {

	/**
	 * This method is executed when a new browser has been created and ready to be used by the
	 * Crawler. The PreCrawling plugins are executed before these plugins are executed except that
	 * the pre-crawling plugins are only executed on the first created browser. while this plugin is
	 * executed on every new browser.
	 *
	 * @param newBrowser the new created browser object
	 */

	void onBrowserClose(CrawlerContext context);

}

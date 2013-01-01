package com.crawljax.core.plugin;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Plugin type that is called before the crawling starts and before the initial URL has been loaded.
 * This kind of plugins can be used to do for example 'once in a crawlsession' operations like
 * logging in a web application or reset the database to a 'clean' state. The argument offered to
 * the Plugin is a the current running instance of EmbeddedBrowser.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public interface PreCrawlingPlugin extends Plugin {

	/**
	 * Method that is called before Crawljax loads the initial url and before the core starts
	 * crawling. Warning the instance of the browser offered is not a clone but the current and
	 * after wards used browser instance, changes and operations may cause 'strange' behaviour.
	 * 
	 * @param browser
	 *            the current browser loaded with the initial url
	 */
	void preCrawling(EmbeddedBrowser browser);
}

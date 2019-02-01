package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;

/**
 * Plugin type that is called after the initial URL is (re)loaded. Example: refreshing the page
 * (clear the browser cache). The OnUrlLoadPlugins are run just after the Browser has gone to the
 * initial URL. Not only the first time but also every time the Core navigates back (back-tracking).
 */
public interface OnUrlFirstLoadPlugin extends Plugin {

	/**
	 * Method that is called when the url is loaded the first time. Can be used to clean application
	 * state before beginning the crawl Responsible to return the browser to the correct URL. The
	 * state left by this plugin is used to create index state
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 *
	 * @param context the current crawler context.
	 */
	void onUrlFirstLoad(CrawlerContext context);

}
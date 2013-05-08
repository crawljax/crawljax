package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;

/**
 * Plugin type that is called after the initial URL is (re)loaded. Example: refreshing the page
 * (clear the browser cache). The OnURLloadPlugins are run just after the Browser has gone to the
 * initial URL. Not only the first time but also every time the Core navigates back (back-tracking).
 */
public interface OnUrlLoadPlugin extends Plugin {

	/**
	 * Method that is called after the url is (re) loaded. Warning: changing the browser can change
	 * the behaviour of Crawljax. It is not a copy!
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 * 
	 * @param context
	 *            the current crawler context.
	 */
	void onUrlLoad(CrawlerContext context);

}
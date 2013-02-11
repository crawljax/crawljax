package com.crawljax.core.plugin;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * This interface denotes the Plugin type that is executed everytime when a new Browser is created.
 * This can be used to do login, database changes, statistics etc. everytime a new browser is
 * started.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public interface OnBrowserCreatedPlugin extends Plugin {

	/**
	 * This method is executed when a new browser has been created and ready to be used by the
	 * Crawler. The PreCrawling plugins are executed before these plugins are executed except that
	 * the precrawling plugins are only executed on the first created browser. while this plugin is
	 * executed on every new browser.
	 * 
	 * @param newBrowser
	 *            the new created browser object
	 */
	void onBrowserCreated(EmbeddedBrowser newBrowser);

}

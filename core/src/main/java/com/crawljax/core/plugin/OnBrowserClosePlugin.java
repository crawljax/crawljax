package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;

/**
 * This interface denotes the Plugin type that is executed every time browser is closed
 *
 * @author Stefan Lenselink &lt;S.R.Lenselink@student.tudelft.nl&gt;
 */
public interface OnBrowserClosePlugin extends Plugin {

    /**
     * This method is executed when the browser is closed
     *
     * @param context the crawler context
     */
    void onBrowserClose(CrawlerContext context);
}

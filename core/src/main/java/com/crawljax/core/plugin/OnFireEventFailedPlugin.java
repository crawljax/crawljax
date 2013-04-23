package com.crawljax.core.plugin;

import java.util.List;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.state.Eventable;

/**
 * Plugin type that is called every time event that was requested to fire failed firing.
 */
public interface OnFireEventFailedPlugin extends Plugin {

	/**
	 * Method that is called when an event that was requested to fire failed firing.
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 * 
	 * @param context
	 *            The per crawler context.
	 * @param eventable
	 *            the eventable that failed to execute
	 * @param pathToFailure
	 *            the list of eventable lead TO this failed eventable, the eventable excluded.
	 */
	void onFireEventFailed(CrawlerContext context, Eventable eventable,
	        List<Eventable> pathToFailure);

}

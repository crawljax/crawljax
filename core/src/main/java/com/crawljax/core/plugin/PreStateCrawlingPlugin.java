package com.crawljax.core.plugin;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableList;

/**
 * Plugin type that is called before firing events on the current DOM state.
 */
public interface PreStateCrawlingPlugin extends Plugin {

	/**
	 * Method that is called before firing events on the current DOM state. Warning the session and
	 * candidateElements are not clones, changes will result in changed behavior.
	 * <p>
	 * This method can be called from multiple threads with different {@link CrawlerContext}
	 * </p>
	 * 
	 * @param context
	 *            the current session data.
	 * @param candidateElements
	 *            the candidates for the current state.
	 * @param state
	 *            The state being crawled
	 */
	void preStateCrawling(CrawlerContext context,
	        ImmutableList<CandidateElement> candidateElements, StateVertex state);

}

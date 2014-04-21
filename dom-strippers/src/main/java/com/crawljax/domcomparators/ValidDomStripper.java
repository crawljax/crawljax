package com.crawljax.domcomparators;

import com.google.common.base.Function;
import org.jsoup.nodes.Document;

/**
 * A DOM stripper that accepts <i>and</i> returns a valid DOM.
 * <p>{@link com.crawljax.domcomparators.ValidDomStripper} are executed <i>before</i> the {@link
 * com.crawljax.domcomparators.DomStripper} because regular {@link com.crawljax.domcomparators.DomStripper} can return
 * an invalid DOM</p>
 *
 * <p>Not that browser interaction is always done on the original DOM, not the modified dom return by a stripper</p>
 *
 * <p>DomStrippers should be stateless as they can be called from multiple threads at once.</p>
 */
public interface ValidDomStripper extends Function<Document, Document> {

	/**
	 * @param input The DOM that you can strip.
	 * @return the stripped DOM. Either the original string or a new string.
	 */
	@Override
	Document apply(Document input);
}

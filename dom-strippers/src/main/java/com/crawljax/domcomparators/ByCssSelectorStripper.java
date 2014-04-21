package com.crawljax.domcomparators;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Strips nodes from the DOM based on the given CSS selector. If the node wit the attribute has children, those are
 * deleted too.
 *
 * <p>For example, you can filter all nodes with id "someId" by invoking: {@code new ByAttributeStripper(".myClass");}
 * which will delete all elements with class {@code myClass}. </p>
 *
 * @see <a href="http://jsoup.org/apidocs/org/jsoup/select/Selector.html">JSoup Selector documentation for usage on
 * the CSS selector.</a>
 */
public class ByCssSelectorStripper implements ValidDomStripper {

	private final String selector;

	/**
	 * @param selector The CSS Selector.
	 */
	public ByCssSelectorStripper(String selector) {
		this.selector = selector;
	}

	@Override
	public Document apply(Document doc) {
		Elements elementsByAttribute = doc.select(selector);
		for (Element element : elementsByAttribute) {
			element.remove();
		}
		return doc;
	}
}

/**
 * 
 */
package com.crawljax.condition;

import net.jcip.annotations.Immutable;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxException;

/**
 * A condition in the form of a JavaScript expression which returns true if the expression return
 * true.
 * 
 * @author Danny
 * @version $Id$
 */
@Immutable
public class JavaScriptCondition extends AbstractCondition {

	private final String expression;

	/**
	 * Construct a JavaScript condition check.
	 * 
	 * @param expression
	 *            The actual Javascript to check.
	 */
	public JavaScriptCondition(String expression) {
		this.expression = expression;
	}

	/**
	 * Check invariant.
	 * 
	 * @param browser
	 *            The browser.
	 * @return Whether the condition is statisfied.
	 */
	@Override
	public boolean check(EmbeddedBrowser browser) {
		String js =
		        "try{ if(" + expression + "){return '1';}else{" + "return '0';}}catch(e){"
		                + " return '0';}";
		try {
			Object object = browser.executeJavaScript(js);
			if (object == null) {
				return false;
			}
			return object.toString().equals("1");
		} catch (CrawljaxException e) {
			// Exception is catched, check failed so return false;
			return false;
		}
	}

}

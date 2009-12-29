/**
 * 
 */
package com.crawljax.condition;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.util.Helper;


/**
 * @author Danny
 * @version $Id: JavaScriptCondition.java 6234 2009-12-18 13:46:37Z mesbah $ A condition in the form
 *          of a JavaScript expression which returns true if the expression return true
 */
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
		        "try{ if(" + expression + "){" + Helper.useJSReturn(browser) + "'1';}else{"
		                + Helper.useJSReturn(browser) + "'0';}}catch(e){"
		                + Helper.useJSReturn(browser) + "'0';}";
		try {
			Object object = browser.executeJavaScript(js);
			if (object == null) {
				return false;
			}
			return object.toString().equals("1");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

package com.crawljax.plugins.clickabledetector;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.util.List;

import com.crawljax.core.configuration.CrawlRules;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

/**
 * A filter that inserts JavaScript into every HTML file that intercepts clickables. When an element
 * is clickable, it receives the <code>data-cj-clickable</code> attribute which can be picked up by
 * Crawljax.
 *
 * <p>
 * Note that this Clickable detector is currently not compatible with the PhantomJS browser.
 * </p>
 */
public class ClickableDetectorFilter extends JavaScriptInjectorFilterSource {

	private static final String JS_FILE = "clickable-detector-pre.js";
	private static final String CSS_FILE = "clickable-detected.css";
	private static final String MUTATION_LIBRARY =
	        "<SCRIPT src=\"http://rawgit.com/rafaelw/mutation-summary/master/src/mutation-summary.js\"></SCRIPT>";

	public static final String[] TAGS = { "A", "ADDRESS", "AREA", "ARTICLE", "ASIDE", "BIG",
	        "BLOCKQUOTE", "BODY", "BUTTON", "CANVAS", "CITE", "CODE", "COL", "COLGROUP", "DD",
	        "DL", "DT", "DIV", "FIELDSET", "FIGCAPTION", "FIGURE", "H1", "H2", "H3", "H4", "H5",
	        "H6", "HEADER", "IMG", "INPUT", "LABEL", "LEGEND", "LI", "NAV", "OBJECT", "P", "PRE",
	        "SECTION", "SELECT", "SMALL", "SPAN", "TABLE", "TBODY", "TD", "TH", "TITLE", "TR",
	        "TT", "UL" };

	public static ClickableDetectorFilter withoutCss() {
		String javaScript = wrapAs("SCRIPT", readFile(JS_FILE));
		return new ClickableDetectorFilter(ImmutableList.of(MUTATION_LIBRARY, javaScript));
	}

	private static String wrapAs(String tag, String content) {
		return System.lineSeparator() + '<' + tag + ">" + System.lineSeparator() + content
		        + System.lineSeparator() + "</" + tag + '>';
	}

	public static ClickableDetectorFilter withCss() {
		String javaScript = wrapAs("SCRIPT", readFile(JS_FILE));
		String css = wrapAs("STYLE", readFile(CSS_FILE));
		return new ClickableDetectorFilter(ImmutableList.of(MUTATION_LIBRARY, javaScript, css));
	}

	private static String readFile(String file) {
		try {
			return Resources.toString(Resources.getResource(file), UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected exception while reading the JavaScript", e);
		}
	}

	/**
	 * Set the click handler so it clicks everything with the attribute.
	 * <p>
	 * Unfortunately crawljax does not (yet) support setting a wildcard for the elements. Until
	 * then, this method helps you setup the clickable detector for all (sane) elements where a
	 * click action might be registered to.
	 * </p>
	 *
	 * @param rules
	 *            The rule builder whe the click rules will be set.
	 */
	public static void configureCrawlRules(CrawlRules.CrawlRulesBuilder rules) {
		rules.click("A").withAttribute("href", "true");
		for (String tag : TAGS) {
			rules.click(tag).withAttribute("data-cj-clickable", "true");
		}
	}

	private ClickableDetectorFilter(List<String> files) {
		super(files);
	}

}

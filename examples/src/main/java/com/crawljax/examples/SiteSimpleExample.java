package com.crawljax.examples;

import static java.lang.System.out;

import java.util.Iterator;

import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.state.SpecificationMetricState;

/**
 * Crawls google.com in IE.
 */
public final class SiteSimpleExample {

	private static final String URL = "http://www.google.com";

	private static final String ALL_ANCHORS = "a";
	private static final String LANGUAGE_TOOLS = "Language Tools";

	private static final String HEADER_XPATH = "//DIV[@id='guser']";

	private static final int MAX_CRAWL_DEPTH = 1;
	private static final int MAX_STATES = 10;

	/**
	 * Entry point
	 */
	public static void main(String[] args) {
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().dontClick(ALL_ANCHORS).underXPath(HEADER_XPATH);
		builder.crawlRules().dontClick(ALL_ANCHORS).withText(LANGUAGE_TOOLS);

		// limit the crawling scope
		builder.setMaximumStates(MAX_STATES);
		builder.setMaximumDepth(MAX_CRAWL_DEPTH);

		builder.crawlRules().setInputSpec(getInputSpecification());

		CrawljaxController crawljax = new CrawljaxController(builder.build());
		crawljax.run();
		SpecificationMetricState state;
		Iterator<SpecificationMetricState> includedIt=CandidateElementExtractor.includedSpecsChecked.iterator();
		Iterator<SpecificationMetricState> excludedIt=CandidateElementExtractor.excludedSpecsChecked.iterator();
		while(includedIt.hasNext() || excludedIt.hasNext()){
			state=includedIt.next();
			state.printState();
			out.println("\nIncluded Tags and the Elements they matched:");
			state.printReport();
			state=excludedIt.next();
			out.println("\nExcluded Tags and the Elements they matched:");
			state.printReport();
		}
	}

	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();

		// enter "Crawljax" in the search field
		input.field("q").setValue("Crawljax");
		return input;
	}

	private SiteSimpleExample() {
		// Utility class
	}
}

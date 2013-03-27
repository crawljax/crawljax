package ca.ubc.eece310.groupL2C1;

import ca.ubc.eece310.groupL2C1.Specification_Metrics_Plugin;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;

public class Specification_Metrics_Plugin_Tests{
	
	private static final String URL = "http://www.google.com";

	private static final String ALL_ANCHORS = "a";
	private static final String LANGUAGE_TOOLS = "Language Tools";

	private static final String HEADER_XPATH = "//DIV[@id='guser']";

	private static final int MAX_CRAWL_DEPTH = 1;
	private static final int MAX_STATES = 2;
	
	@Test
	public void testOutputFileCreated(){
		
		testRun();
		File f = new File("output.txt");
		assertTrue(f.exists());
	}
	
	private void testRun(){
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().dontClick(ALL_ANCHORS).underXPath(HEADER_XPATH);
		builder.crawlRules().dontClick(ALL_ANCHORS).withText(LANGUAGE_TOOLS);

		// limit the crawling scope
		builder.setMaximumStates(MAX_STATES);
		builder.setMaximumDepth(MAX_CRAWL_DEPTH);

		builder.crawlRules().setInputSpec(getInputSpecification());

		builder.addPlugin(new Specification_Metrics_Plugin(new File("specs_outPut")));
		
		CrawljaxController crawljax = new CrawljaxController(builder.build());
		crawljax.run();
	}
	
	private static InputSpecification getInputSpecification() {
		InputSpecification input = new InputSpecification();

		// enter "Crawljax" in the search field
		input.field("q").setValue("Crawljax");
		return input;
	}
}
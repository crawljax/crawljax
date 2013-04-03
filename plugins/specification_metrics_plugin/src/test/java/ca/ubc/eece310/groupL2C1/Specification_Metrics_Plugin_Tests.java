package ca.ubc.eece310.groupL2C1;

import ca.ubc.eece310.groupL2C1.Specification_Metrics_Plugin;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;


import com.crawljax.core.CrawljaxController;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;

public class Specification_Metrics_Plugin_Tests{
	
	private static final String URL = "http://www.google.ca/";

	private static final String ALL_ANCHORS = "a";
	private static final String LANGUAGE_TOOLS = "Language Tools";

	private static final String HEADER_XPATH = "//DIV[@id='guser']";

	private static final int MAX_CRAWL_DEPTH = 1;
	private static final int MAX_STATES = 2;
	
	private String fileOutputDirectory = "specification_metrics_output";
	
	@Test
	public void testOutputFileCreated()
	{
		testRun();
		File f = new File(fileOutputDirectory + "/specification_metric_plugin.txt");	
		assertTrue(f.exists());		
	}
	
	@Test
	public void testOutputFileNotEmpty(){
		testRun();
		File f = new File(fileOutputDirectory + "/specification_metric_plugin.txt");
		
		assertTrue(f.length()>0);
	}
	
	/*
	@Test
	public void testLine(){
		testRun();
		int i;
		String line = null;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader("output.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(i=0;i<3;i++) //3 is the Line Number
			try {
				line = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		assertEquals(line, "BUTTON:              3"); // Line Content
	}
	*/
	
	
	private void testRun(){
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);
		builder.crawlRules().clickDefaultElements();
		builder.crawlRules().dontClick(ALL_ANCHORS).underXPath(HEADER_XPATH);
		builder.crawlRules().dontClick(ALL_ANCHORS).withText(LANGUAGE_TOOLS);

		// limit the crawling scope
		builder.setMaximumStates(MAX_STATES);
		builder.setMaximumDepth(MAX_CRAWL_DEPTH);

		builder.crawlRules().setInputSpec(getInputSpecification());

		//builder.addPlugin(new CrawlOverview(new File("outPut")));
		Specification_Metrics_Plugin SMP= new Specification_Metrics_Plugin();
		SMP.setOutputFolder(fileOutputDirectory);
		builder.addPlugin(SMP);
		
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
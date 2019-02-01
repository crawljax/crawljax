/**
 * Created Apr 17, 2008
 */
package com.crawljax.plugins.testcasegenerator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.plugin.HostInterface;
import com.crawljax.core.plugin.HostInterfaceImpl;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.plugins.testcasegenerator.TestConfiguration.StateEquivalenceAssertionMode;
import com.crawljax.util.DomUtils;
import com.crawljax.util.FSUtils;

/**
 * Test suite generator for crawljax. IMPORTANT: only works with CrawljaxConfiguration TODO: Danny,
 * also make sure package name is correct
 * 
 * @author danny
 * @version $Id: TestSuiteGenerator.java 6276 2009-12-23 15:37:09Z frank $
 */
public class TestSuiteGenerator implements PostCrawlingPlugin {

	private static final String TEST_SUITE_SRC_FOLDER = "src/test/java/";
	private static final String TEST_SUITE_PACKAGE_NAME = "generated";
	private static final String TEST_SUITE_PATH = TEST_SUITE_SRC_FOLDER + TEST_SUITE_PACKAGE_NAME.replace(".", File.separator) + File.separator;
	private static final String CLASS_NAME = "GeneratedTests";
	private static final String FILE_NAME_TEMPLATE = "TestCaseNG.vm";

	private final String JSON_STATES = TEST_SUITE_PATH + "states.json";
	private final String JSON_EVENTABLES = TEST_SUITE_PATH + "eventables.json";

	private CrawlSession session;
	private HostInterface overviewHostInterface;
	private HostInterface testgenHostInterface;
	private String absPath;
	private TestConfiguration testConfiguration;

	private static final Logger LOGGER =
	        LoggerFactory.getLogger(TestSuiteGenerator.class.getName());

	public TestSuiteGenerator() {
		this.testgenHostInterface = null;
		this.overviewHostInterface = null;
		this.testConfiguration = null;
		absPath = "";
		LOGGER.info("Initialized the Test Suite Generator plugin");
	}

	public TestSuiteGenerator(TestConfiguration testConfiguration) {
		this();
		this.testConfiguration = testConfiguration;
	}

	public TestSuiteGenerator(HostInterface overviewHostInterface,
	        HostInterface testgenHostInterface) {
		this.testgenHostInterface = testgenHostInterface;
		this.overviewHostInterface = overviewHostInterface;
		absPath =
		        this.testgenHostInterface.getOutputDirectory().getAbsolutePath() + File.separator;
		LOGGER.info("Initialized the Test Suite Generator plugin");
	}

	@Override
	public void postCrawling(CrawlSession session, ExitStatus exitReason) {

		/*
		 * Set up the input and output directories for the test suite, if not specified.
		 */

		if (overviewHostInterface == null) {
			overviewHostInterface =
			        new HostInterfaceImpl(session.getConfig().getOutputDir(), null);
		}

		if (testgenHostInterface == null) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("testRecordsDir",
			        new File(session.getConfig().getOutputDir(), "test-results")
			                .getAbsolutePath());
			testgenHostInterface =
			        new HostInterfaceImpl(session.getConfig().getOutputDir(), params);
			absPath =
			        testgenHostInterface.getOutputDirectory().getAbsolutePath() + File.separator;
		}

		/*
		 * Set Browser configuration and assertion mode if not passed as parameters to the
		 * constructor
		 */
		if (testConfiguration == null) {
			this.testConfiguration = new TestConfiguration(StateEquivalenceAssertionMode.ALL,
			        session.getConfig().getBrowserConfig());
		} else {
			if (testConfiguration.getBrowserConfig() == null) {
				testConfiguration.setBrowserConfig(session.getConfig().getBrowserConfig());
			}
			if (testConfiguration.getAssertionMode() == null) {
				testConfiguration.setAssertionMode(StateEquivalenceAssertionMode.ALL);
			}
		}

		this.session = session;
		try {
			FSUtils.directoryCheck(absPath + TEST_SUITE_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("Generating tests in " + absPath + TEST_SUITE_PATH);
		String fileName = generateTestCases();
		if (fileName != null) {
			LOGGER.info("Tests generated in " + fileName);
		} else {
			LOGGER.error("Failed to generate test cases");
		}
	}

	/**
	 * @return the filename of the generated java test class, null otherwise
	 */
	public String generateTestCases() {
		TestSuiteGeneratorHelper testSuiteGeneratorHelper = new TestSuiteGeneratorHelper(session);
		List<TestMethod> testMethods = testSuiteGeneratorHelper.getTestMethods();

		try {
			JavaTestGenerator generator =
			        new JavaTestGenerator(CLASS_NAME, session.getInitialState().getUrl(),
			                testMethods, session.getConfig(),
			                absPath + TEST_SUITE_PATH,
			                this.overviewHostInterface.getOutputDirectory().getAbsolutePath(),
			                this.testgenHostInterface.getParameters().get("testRecordsDir"),
			                testConfiguration);
			testSuiteGeneratorHelper.writeStateVertexTestDataToJSON(absPath + JSON_STATES);
			testSuiteGeneratorHelper.writeEventableTestDataToJSON(absPath + JSON_EVENTABLES);
			generator.useJsonInsteadOfDB(absPath + JSON_STATES, absPath + JSON_EVENTABLES);
			String generatedFileName = generator.generate(DomUtils.addFolderSlashIfNeeded(absPath + TEST_SUITE_PATH),
					FILE_NAME_TEMPLATE);
			if (null != generatedFileName) {
				generator.copyExecutionScripts(absPath, TEST_SUITE_SRC_FOLDER, TEST_SUITE_PACKAGE_NAME, CLASS_NAME);
			}
			return generatedFileName;

		} catch (Exception e) {
			System.out.println("Error generating testsuite: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "Test Suite Generator plugin";
	}
}

package com.crawljax.plugins.testcasegenerator;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.opencv.imgcodecs.Imgcodecs;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.Element;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.di.CoreModule;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.oraclecomparator.StateComparator;
import com.crawljax.plugins.testcasegenerator.report.MethodResult;
import com.crawljax.plugins.testcasegenerator.report.ReportBuilder;
import com.crawljax.plugins.testcasegenerator.report.TestRecord;
import com.crawljax.plugins.testcasegenerator.report.TestRecord.TestStatusType;
import com.crawljax.plugins.testcasegenerator.util.GsonUtils;
import com.crawljax.plugins.testcasegenerator.util.WorkDirManager;
import com.crawljax.plugins.testcasegenerator.visualdiff.ObjectDetection;
import com.crawljax.plugins.testcasegenerator.visualdiff.ObjectDiff;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.AveragePageObjectFactory;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.IPageObjectFactory;
import com.crawljax.util.DomUtils;
import com.crawljax.util.ElementResolver;
import com.crawljax.util.FSUtils;
import com.crawljax.util.UrlUtils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Guice;

/**
 * Helper for the test suites.
 */
public class TestSuiteHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteHelper.class.getName());

	private EmbeddedBrowser browser;
	private String url;

	private StateComparator oracleComparator;
	private ConditionTypeChecker<Invariant> invariantChecker;
	private WaitConditionChecker waitConditionChecker;
	private final ArrayList<Eventable> eventables = new ArrayList<Eventable>();
	private FormHandler formHandler;
	private Plugins plugins;

	private Map<Long, StateVertex> mapStateVertices;
	private Map<Long, Eventable> mapEventables;
	private CrawlerContext context;

	private String outputPath;
	private String tmpPath;

	private ReportBuilder reportBuilder;

	/*
	 * private EmbeddedBrowser oldPage; private EmbeddedBrowser newPage;
	 */
	/* Screenshot stuff. */
	private boolean firstState = true;

	private final File screenshotsOutputFolder;
	private final File screenshotsInputFolder;

	// public final File diffOutputFolder;

	private String testRecordsFolder;

	private WorkDirManager manager;

	private String currTestRunFolderPath;

	private List<TestRecord> testRecords = new ArrayList<TestRecord>();

	private TestRecord currTestRecord;

	/**
	 * @param config
	 *            Configuration to use.
	 * @param jsonStates
	 *            The json states.
	 * @param jsonEventables
	 *            The json eventables.
	 * @throws Exception
	 *             On error.
	 */
	public TestSuiteHelper(CrawljaxConfiguration config,
	        String jsonStates,
	        String jsonEventables,
	        String crawlScreenshots,
	        String testRecords,
	        String url,
	        String outputPath) throws Exception {
		LOGGER.info("Loading needed json files for States and Eventables");

		Gson gson = (new GsonBuilder())
		        .registerTypeAdapter(ImmutableMap.class, new GsonUtils.ImmutableMapDeserializer())
		        .create();
		// TODO We might want to parameterize this class to accept specific StateVertex
		mapStateVertices = gson.fromJson(new BufferedReader(new FileReader(jsonStates)),
		        new TypeToken<Map<Long, StateVertexImpl>>() {
		        }.getType());
		mapEventables = gson.fromJson(new BufferedReader(new FileReader(jsonEventables)),
		        new TypeToken<Map<Long, Eventable>>() {
		        }.getType());

		this.url = url;
		this.context =
		        Guice.createInjector(new CoreModule(config)).getInstance(CrawlerContext.class);
		this.plugins = new Plugins(config, new MetricRegistry());
		this.browser = context.getBrowser();

		this.formHandler =
		        new FormHandler(browser, config.getCrawlRules());

		this.oracleComparator = new StateComparator(config.getCrawlRules());
		this.invariantChecker =
		        new ConditionTypeChecker<Invariant>(config.getCrawlRules().getInvariants());
		this.waitConditionChecker = new WaitConditionChecker(config.getCrawlRules());

		this.outputPath = outputPath;
		String outputDiff = outputPath + "diffs" + File.separator;
		FileUtils.deleteDirectory(new File(outputDiff));
		FSUtils.checkFolderForFile(outputDiff);
		reportBuilder = new ReportBuilder(outputDiff);

		this.tmpPath = outputDiff + "tmp" + File.separator;
		FileUtils.deleteDirectory(new File(this.tmpPath));
		FSUtils.checkFolderForFile(this.tmpPath);
		/*
		 * this.oldPage = Guice.createInjector(new
		 * CoreModule(config)).getInstance(EmbeddedBrowser.class); this.newPage =
		 * Guice.createInjector(new CoreModule(config)).getInstance(EmbeddedBrowser.class);
		 */
		LOGGER.info("Loading plugins...");
		plugins.runPreCrawlingPlugins(config);

		/* The folder where we will temporarily store screenshots. */
		screenshotsOutputFolder = new File(this.tmpPath);

		/* The folder where the oracle screenshots are stored. */
		screenshotsInputFolder = new File(crawlScreenshots);

		/* The folder where the image diffs are stored. */
		testRecordsFolder = testRecords;

		manager = new WorkDirManager();

		int newID = manager.getNumTestRecords(new File(testRecordsFolder));

		currTestRunFolderPath = testRecordsFolder + File.separator + newID;

		File currTestRunFolder = new File(currTestRunFolderPath);

		if (!currTestRunFolder.exists()) {
			currTestRunFolder.mkdirs();
		}
	}

	private TestRecord newTestRecord(String methodName) {
		String methodRecordFolderPath = currTestRunFolderPath + File.separator + methodName;

		File methodRecordFolder = new File(methodRecordFolderPath);
		if (!methodRecordFolder.exists())
			methodRecordFolder.mkdir();

		TestRecord r = new TestRecord();
		String srcFilePath = outputPath + "GeneratedTests.java";
		r.setTestSrcPath(srcFilePath);
		r.setMethodName(methodName);
		// testList.add(0, r);
		manager.saveTestRecord(r, methodRecordFolderPath);

		MethodResult methodResult = reportBuilder.newMethod(methodName);
		r.setMethodResult(methodResult);

		testRecords.add(r);
		return r;
	}

	/**
	 * Loads start url and checks initialUrlConditions.
	 * 
	 * @throws Exception
	 *             On error.
	 */
	public void goToInitialUrl() throws Exception {
		browser.goToUrl(new URI(url));
		waitConditionChecker.wait(browser);
		plugins.runOnUrlLoadPlugins(context);
	}

	/**
	 * Closes browser and writes report.
	 * 
	 * @throws Exception
	 *             On error.
	 */
	public void tearDown() throws Exception {
		Thread.sleep(400);

		// if there's still a method it failed
		reportBuilder.methodFail();
		//reportBuilder.build();

		for (TestRecord testRecord : testRecords) {
			manager.saveTestRecord(testRecord,
			        currTestRunFolderPath + File.separator + testRecord.getMethodName());
		}

		manager.saveTestRecordMap(testRecords, manager.getNumTestRecords(new File(testRecordsFolder)), url, currTestRunFolderPath);

		LOGGER.info("Report generated in " + this.currTestRunFolderPath);

		browser.close();
		/*
		 * oldPage.close(); newPage.close();
		 */
	}

	/**
	 * Fill in form inputs.
	 * 
	 * @param formInputs
	 *            The form inputs to handle.
	 * @throws Exception
	 *             On error.
	 */
	public void handleFormInputs(List<FormInput> formInputs) throws Exception {
		formHandler.handleFormElements(formInputs);
	}

	/**
	 * Run the InCrawling plugins.
	 */
	public void runInCrawlingPlugins(long stateId) {
		plugins.runOnNewStatePlugins(context, getStateVertex(stateId));
	}

	private Eventable getEventable(Long eventableId) {
		return mapEventables.get(eventableId);
	}

	private boolean visitAnchorHrefIfPossible(Eventable eventable) {
		Element element = eventable.getElement();
		String href = element.getAttributeOrNull("href");
		if (href == null) {
			LOGGER.info("Anchor {} has no href and is invisble so it will be ignored", element);
		} else {
			LOGGER.info("Found an invisible link with href={}", href);
			URI url = UrlUtils.extractNewUrl(browser.getCurrentUrl(), href);
			browser.goToUrl(url);
			return true;
		}
		return false;
	}
	
	/**
	 * @param eventableId
	 *            Id of the eventable.
	 * @return whether the event is fired
	 */
	public boolean fireEvent(long eventableId) {
		try {
			// browser.closeOtherWindows();
			Eventable eventable = getEventable(eventableId);
			eventables.add(eventable);
			reportBuilder.addEventable(eventable);
			String xpath = eventable.getIdentification().getValue();

			ElementResolver er = new ElementResolver(eventable, browser);
			String newXPath = er.resolve();
			boolean fired = false;
			if (newXPath != null) {
				if (!xpath.equals(newXPath)) {
					LOGGER.info("XPath of \"" + eventable.getElement().getText()
					        + "\" changed from " + xpath + " to " + newXPath);
				}
				eventable.setIdentification(new Identification(How.xpath, newXPath));
				LOGGER.info("Firing: " + eventable);
				
				try {
					fired = browser.fireEventAndWait(eventable);
				} catch (ElementNotVisibleException | NoSuchElementException e) {
					if (eventable.getElement() != null
					        && "A".equals(eventable.getElement().getTag())) {
						fired = visitAnchorHrefIfPossible(eventable);
					} else {
						LOGGER.debug("Ignoring invisble element {}", eventable.getElement());
					}
				} 
			}
			if (!fired) {
				// String orgDom = "";
				// try {
				// orgDom = eventable.getEdge().getFromStateVertex().getDom();
				// } catch (Exception e) {
				// // TODO: Danny fix
				// orgDom = "<html>todo: fix</html>";
				// // LOGGER.info("Warning, could not get original DOM");
				// }
				// reportBuilder.addFailure(new EventFailure(browser, currentTestMethod, eventables,
				// orgDom, browser.getDom()));

				reportBuilder.markLastEventableFailed();
			}
			waitConditionChecker.wait(browser);
			return fired;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * @param StateVertexId
	 *            The id of the state vertix.
	 * @return the State with id StateVertex Id
	 */
	public StateVertex getStateVertex(Long StateVertexId) {
		return mapStateVertices.get(StateVertexId);
	}

	/**
	 * @param StateVertexId
	 *            The id of the state vertex.
	 * @return return where the current DOM in the browser is equivalent with the state with
	 *         StateVertexId
	 */
	public boolean compareCurrentScreenshotWithState(long StateVertexId) {

		/* The screenshots from before and after the change. */
		StateVertex vertex = getStateVertex(StateVertexId);
		File oldScreenshot = oldScreenShotFile(vertex.getName());
		File newScreenshot = newScreenShotFile(vertex.getName());

		/* Save the current screenshot to a temporary folder. */
		saveScreenshot(getBrowser(), vertex.getName(), newScreenshot);

		/* Create a visual diff of the screenshots. */
		ObjectDiff diff = visualDiff(oldScreenshot, newScreenshot);

		/* Build a report if the states differ. */
		if (diff.hasChanges()) {

			/* Write the annotated file to disk. */
			File oldFile = oldDiffFile(vertex.getName(), true);
			File newFile = newDiffFile(vertex.getName(), true);
			try {
				ObjectDetection.directoryCheck(currTestRunFolderPath);
				Imgcodecs.imwrite(oldFile.getAbsolutePath(), diff.annotateOldPage());
				Imgcodecs.imwrite(newFile.getAbsolutePath(), diff.annotateNewPage());
				currTestRecord.writeDiffToTestRecord(vertex.getName(), oldFile.getName(),
				        newFile.getName());
			} catch (IOException e) {
				LOGGER.debug("Annotated files not written to disk because {}", e.getMessage(), e);
			}
			reportBuilder.markLastStateDifferent();
			return false;
		}

		return true;
	}

	/**
	 * Annotate two versions of a screenshot with diff info.
	 * 
	 * @param oldScreenshot
	 * @param newScreenshot
	 */
	private ObjectDiff visualDiff(File oldScreenshot, File newScreenshot) {

		IPageObjectFactory pageObjectFactory = new AveragePageObjectFactory();

		/* Run the detection algorithm. */
		ObjectDetection srcDetection =
		        new ObjectDetection(pageObjectFactory, oldScreenshot.getAbsolutePath());
		ObjectDetection dstDetection =
		        new ObjectDetection(pageObjectFactory, newScreenshot.getAbsolutePath());

		srcDetection.detectObjects();
		dstDetection.detectObjects();

		/* Do the visual diff. */
		ObjectDiff diff = new ObjectDiff(srcDetection.getPage(), dstDetection.getPage(), false);
		diff.diff();

		return diff;

	}

	private void saveScreenshot(EmbeddedBrowser browser, String name, File newScreenshot) {

		if (firstState) {
			firstState = false;
			// check if screenshots folder is already created by core
			File screenshotsFolder = getScreenshotsOutputFolder();
			if (!screenshotsFolder.exists()) {
				// screenshots already taken, no need to retake here
				LOGGER.debug("Screenshot folder does not exist yet, creating...");
				boolean created = screenshotsFolder.mkdir();
				checkArgument(created, "Could not create screenshotsFolder dir");
			}
		}

		LOGGER.debug("Saving screenshot for state {}", name);

		try {
			BufferedImage screenshot = browser.getScreenShotAsBufferedImage(500);
			ImageWriter.writeScreenShotAndThumbnail(screenshot, newScreenshot);
		} catch (CrawljaxException | WebDriverException e) {
			LOGGER.warn(
			        "Screenshots are not supported or not functioning for {}. Exception message: {}",
			        browser, e.getMessage());
			LOGGER.debug("Screenshot not made because {}", e.getMessage(), e);
		}
		LOGGER.trace("Screenshot saved");

	}

	File newScreenShotFile(String name) {
		return new File(screenshotsOutputFolder, name + ".png");
	}

	public File getScreenshotsOutputFolder() {
		return screenshotsOutputFolder;
	}

	File oldScreenShotFile(String name) {
		return new File(screenshotsInputFolder, name + ".png");
	}

	public File getScreenshotsInputFolder() {
		return screenshotsInputFolder;
	}

	private File oldDiffFile(String name, boolean isImage) {
		if (isImage) {
			return new File(
			        manager.getDiffsFolder(this.currTestRunFolderPath, this.currTestRecord),
			        name + "_old.png");
		} else {
			return new File(
			        manager.getDiffsFolder(this.currTestRunFolderPath, this.currTestRecord),
			        name + "_old.html");
		}
	}

	private File newDiffFile(String name, boolean isImage) {
		if (isImage) {
			return new File(
			        manager.getDiffsFolder(this.currTestRunFolderPath, this.currTestRecord),
			        name + "_new.png");
		} else {
			return new File(
			        manager.getDiffsFolder(this.currTestRunFolderPath, this.currTestRecord),
			        name + "_new.html");
		}
	}

	/**
	 * @param StateVertexId
	 *            The id of the state vertex.
	 * @return return where the current DOM in the browser is equivalent with the state with
	 *         StateVertexId
	 */
	public boolean compareCurrentDomWithState(long StateVertexId) {
		StateVertex vertex = getStateVertex(StateVertexId);
		String stateDom = vertex.getStrippedDom();
		String newDom = oracleComparator.getStrippedDom(browser);
		Diff diff;
		Document oldDoc, newDoc;
		try {
			oldDoc = DomUtils.asDocument(stateDom);
			newDoc = DomUtils.asDocument(newDom);
			diff = DiffBuilder
			        .compare(oldDoc)
			        .withTest(newDoc)
			        .ignoreWhitespace()
			        .build();

		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		if (diff.hasDifferences()) {
			// make sure there is at least one visible difference
			boolean visibleDiff = false;
			for (Difference currDiff : diff.getDifferences()) {
				try {
					String xPathNew =
					        currDiff.getComparison().getControlDetails().getParentXPath();
					WebElement elemNew = browser.getWebDriver().findElement(By.xpath(xPathNew));

					if (elemNew.isDisplayed()) {
						visibleDiff = true;
						break;
					}
				} catch (Exception e) {
					// ignore differences that aren't on valid elements
					// (on the document element for instance)
				}
			}
			if (!visibleDiff) {
				return true;
			}

			LOGGER.info("Not Equivalent with state" + StateVertexId);

			try {

				/* Write the annotated file to disk. */
				File oldFile = oldDiffFile(vertex.getName(), false);
				File newFile = newDiffFile(vertex.getName(), false);
				try {
					FileWriter writer = new FileWriter(oldFile);
					writer.write(stateDom);
					writer.flush();
					writer.close();
					writer = new FileWriter(newFile);
					writer.write(newDom);
					writer.flush();
					writer.close();
					currTestRecord.writeDiffToTestRecord(vertex.getName(), stateDom, newDom);
				} catch (IOException e) {
					LOGGER.debug("Annotated files not written to disk because {}", e.getMessage(), e);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			reportBuilder.markLastStateDifferent();
			return false;
		} else {
			return true;
		}
	}

	public StateVertex addStateToReportBuilder(long StateVertexId) {
		StateVertex vertex = getStateVertex(StateVertexId);
		reportBuilder.addState(vertex);
		return vertex;
	}

	/**
	 * @return whether all the invariants are satisfied
	 */
	public boolean checkInvariants() {
		List<Invariant> failedInvariants = invariantChecker.getFailedConditions(browser);
		try {
			for (Invariant failedInvariant : failedInvariants) {
				// reportBuilder.addFailure(new InvariantFailure(browser, currentTestMethod
				// + " - " + failedInvariant.getDescription(), eventables, browser
				// .getDom(), failedInvariant.getDescription(), failedInvariant
				// .getInvariantCondition().getAffectedNodes()));
				LOGGER.info("Invariant failed: " + failedInvariant.toString());
			}
		} catch (Exception e) {
			LOGGER.error("Error with adding failure: " + e.getMessage(), e);
		}
		if (failedInvariants.size() > 0) {
			reportBuilder.markLastStateFailed(failedInvariants);
		}
		return failedInvariants.size() == 0;
	}

	/**
	 * @param currentTestMethod
	 *            The current method that is used for testing
	 */
	public void newCurrentTestMethod(String currentTestMethod) {
		LOGGER.info("New test: " + currentTestMethod);
		currTestRecord = newTestRecord(currentTestMethod);
		eventables.clear();
	}

	/**
	 * Marks the current method as successfully run by JUnit.
	 * 
	 * @param nanos
	 */
	public void markLastMethodAsSucceeded(long nanos) {
		reportBuilder.methodSuccess();
		currTestRecord.setTestRecordStatus(TestStatusType.success, "none");
		currTestRecord.setDuration((TimeUnit.NANOSECONDS.toSeconds(nanos)));
	}

	/**
	 * Marks the current method as having a failure.
	 */
	public void markLastMethodAsFailed(String message, long nanos) {
		reportBuilder.methodFail();
		currTestRecord.setTestRecordStatus(TestStatusType.failure, message);
		currTestRecord.setDuration((TimeUnit.NANOSECONDS.toSeconds(nanos)));
	}

	public void markLastMethodAsSkipped(long nanos) {
		// reportBuilder.methodSkipped();
		currTestRecord.setTestRecordStatus(TestStatusType.skipped, "none");
		currTestRecord.setDuration((TimeUnit.NANOSECONDS.toSeconds(nanos)));
	}

	/**
	 * @return the browser
	 */
	public EmbeddedBrowser getBrowser() {
		return browser;
	}

}

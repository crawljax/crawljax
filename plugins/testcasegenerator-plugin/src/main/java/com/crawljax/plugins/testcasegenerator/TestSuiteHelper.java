package com.crawljax.plugins.testcasegenerator;

import static com.google.common.base.Preconditions.checkArgument;

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
import com.crawljax.core.state.StatePair;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.di.CoreModule;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.fragmentation.FragmentationPlugin;
import com.crawljax.oraclecomparator.StateComparator;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.Serializer;
import com.crawljax.plugins.testcasegenerator.fragDiff.FragDiff;
import com.crawljax.plugins.testcasegenerator.fragDiff.ImageAnnotations;
import com.crawljax.plugins.testcasegenerator.report.MethodResult;
import com.crawljax.plugins.testcasegenerator.report.MethodResult.WarnLevel;
import com.crawljax.plugins.testcasegenerator.report.ReportBuilder;
import com.crawljax.plugins.testcasegenerator.report.TestRecord;
import com.crawljax.plugins.testcasegenerator.report.TestRecord.TestStatusType;
import com.crawljax.plugins.testcasegenerator.util.GsonUtils;
import com.crawljax.plugins.testcasegenerator.util.WorkDirManager;
import com.crawljax.plugins.testcasegenerator.visualdiff.ObjectDetection;
import com.crawljax.plugins.testcasegenerator.visualdiff.ObjectDiff;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.AveragePageObjectFactory;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.IPageObjectFactory;
import com.crawljax.stateabstractions.hybrid.FragGenStateVertexFactory;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.crawljax.util.DomUtils;
import com.crawljax.util.ElementResolver;
import com.crawljax.util.FSUtils;
import com.crawljax.util.UrlUtils;
import com.crawljax.util.XPathHelper;
import com.crawljax.vips_selenium.VipsUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Guice;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FileUtils;
import org.opencv.imgcodecs.Imgcodecs;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/**
 * Helper for the test suites.
 */
public class TestSuiteHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestSuiteHelper.class.getName());
    /*
     * private static final ObjectMapper MAPPER; static { MAPPER = new
     * ObjectMapper(); MAPPER.getSerializationConfig().getDefaultVisibilityChecker()
     * .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
     * .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
     * .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
     * .withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
     * MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
     *
     * MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z",
     * Locale.getDefault()));
     *
     * MAPPER.registerModule(new GuavaModule()); SimpleModule testModule = new
     * SimpleModule("Plugin serialiezr"); testModule.addSerializer(new
     * JsonSerializer<Plugin>() {
     *
     * @Override public void serialize(Plugin plugin, JsonGenerator jgen,
     * SerializerProvider provider) throws IOException, JsonProcessingException {
     * jgen.writeString(plugin.getClass().getSimpleName()); }
     *
     * @Override public Class<Plugin> handledType() { return Plugin.class; } });
     *
     * MAPPER.registerModule(testModule);
     *
     * }
     */
    public static boolean lazyLoad = false;
    // TODO: Set this through testclass and preserve during testgenerator
    public static boolean APTED_VISUAL_DATA = false;
    private static boolean OUTPUT_DIFF_IMAGE = false;
    private static boolean deferOracleComparison = true;
    private final ArrayList<Eventable> eventables = new ArrayList<Eventable>();
    private final AtomicInteger nextStateNameCounter = new AtomicInteger();
    private EmbeddedBrowser browser;
    private EmbeddedBrowser oldPage;
    private String url;
    private StateComparator oracleComparator;
    private ConditionTypeChecker<Invariant> invariantChecker;
    private WaitConditionChecker waitConditionChecker;
    private FormHandler formHandler;
    private Plugins plugins;
    private Map<Long, StateVertex> mapStateVertices;
    private Map<Long, Eventable> mapEventables;
    private CrawlerContext context;
    private String outputPath;
    private String tmpPath;
    private ReportBuilder reportBuilder;

    // public final File diffOutputFolder;
    /*
     * private EmbeddedBrowser oldPage; private EmbeddedBrowser newPage;
     */
    /* Screenshot stuff. */
    private boolean firstState = true;
    private File screenshotsOutputFolder;
    private File screenshotsInputFolder;
    private String testRecordsFolder;
    private WorkDirManager manager;
    private String currTestRunFolderPath;
    private List<TestRecord> testRecords = new ArrayList<TestRecord>();
    private TestRecord currTestRecord;
    private FragmentManager fragmentManager;
    private FragGenStateVertexFactory stateVertexFactory;
    private String crawlPath;
    private ArrayList<Long> verticeKeySet;
    private boolean isHybridState;

    /**
     * @param config         Configuration to use.
     * @param jsonStates     The json states.
     * @param jsonEventables The json eventables.
     * @throws Exception On error.
     */
    public TestSuiteHelper(
            CrawljaxConfiguration config,
            String jsonStates,
            String jsonEventables,
            String crawlPath,
            String testRecords,
            String url,
            String outputPath,
            boolean mutation)
            throws Exception {

        init(config, jsonStates, jsonEventables, crawlPath, testRecords, url);
    }

    public TestSuiteHelper(CrawljaxConfiguration config, String crawlPath, String url, boolean mutate)
            throws Exception {
        String jsonEventables =
                Paths.get(crawlPath, TestSuiteGenerator.JSON_EVENTABLES).toString();
        String jsonStates = Paths.get(crawlPath, TestSuiteGenerator.JSON_STATES).toString();
        String testRecordsFolder =
                Paths.get(crawlPath, TestSuiteGenerator.TEST_RESULTS).toString();
        // String outputPath = Paths.get(crawlPath,
        // TestSuiteGenerator.TEST_SUITE_SRC_FOLDER).toString();
        init(config, jsonStates, jsonEventables, crawlPath, testRecordsFolder, url);
    }

    /**
     * For offline oracles
     */
    public TestSuiteHelper() {}

    public void init(
            CrawljaxConfiguration config,
            String jsonStates,
            String jsonEventables,
            String crawlPath,
            String testRecords,
            String url)
            throws Exception {

        this.crawlPath = crawlPath;

        /********************************************************************************/
        /* Parsing crawl jsons */

        File resultJson = new File(crawlPath + File.separator + "result.json");
        if (Files.notExists(resultJson.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            LOGGER.error("Error finding result json : " + resultJson.toPath());
            System.exit(-1);
        }

        File configJson = new File(crawlPath + File.separator + "config.json");
        if (Files.notExists(configJson.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            LOGGER.error("Error finding config : " + configJson.toPath());
            System.exit(-1);
        }

        OutPutModel result = Serializer.read(resultJson);

        String configString = FileUtils.readFileToString(configJson, Charset.defaultCharset());

        JsonParser parser = new JsonParser();
        JsonObject configObject = parser.parse(configString).getAsJsonObject();

        JsonArray pluginArray = configObject.getAsJsonArray("plugins");

        isHybridState = false;
        for (JsonElement element : pluginArray) {
            if (element.getAsString().equalsIgnoreCase("fragmentationPlugin")) {
                isHybridState = true;
            }
        }

        LOGGER.info("Loading needed json files for States and Eventables");

        Gson gson = (new GsonBuilder())
                .registerTypeAdapter(ImmutableMap.class, new GsonUtils.ImmutableMapDeserializer())
                .registerTypeAdapter(ImmutableList.class, new GsonUtils.ImmutableListDeserializer())
                .create();

        // TODO We might want to parameterize this class to accept specific StateVertex
        if (isHybridState) {
            mapStateVertices = gson.fromJson(
                    new BufferedReader(new FileReader(jsonStates)),
                    new TypeToken<Map<Long, HybridStateVertexImpl>>() {}.getType());
        } else {
            mapStateVertices = gson.fromJson(
                    new BufferedReader(new FileReader(jsonStates)),
                    new TypeToken<Map<Long, StateVertexImpl>>() {}.getType());
        }
        mapEventables = gson.fromJson(
                new BufferedReader(new FileReader(jsonEventables)), new TypeToken<Map<Long, Eventable>>() {}.getType());

        /**********************************************************************************/
        /* Setting up Files and Crawljax Core */
        setGlobalFileVars(config, crawlPath, testRecords);

        try {
            loadCrawljaxCore(config, url);
        } catch (Exception ex) {
            LOGGER.error("Error loading crawljax core");
            LOGGER.debug(ex.getMessage());
            System.exit(-1);
        }

        /********************************************************************************/
        /* Mutation Handling */
        verticeKeySet = new ArrayList<>();
        verticeKeySet.addAll(mapStateVertices.keySet());

        /********************************************************************************/
        /* Hybrid State Handling */

        if (isHybridState) {
            cleanEventables();
            stateVertexFactory =
                    new FragGenStateVertexFactory(0, CrawljaxConfiguration.builderFor(url), APTED_VISUAL_DATA);

            if (!deferOracleComparison) {
                this.fragmentManager = new FragmentManager(null);
            }

            for (Long stateId : verticeKeySet) {
                StateVertex state = mapStateVertices.get(stateId);
                int id = state.getId();

                if (id > nextStateNameCounter.get()) {
                    nextStateNameCounter.set(id);
                }

                if (!lazyLoad) {
                    loadHybridState(state, fragmentManager);
                }
            }
        }

        /********************************************************************************/
        /* Reporting */

        reportBuilder = new ReportBuilder();

        // Running cleanup Plugins
        goToInitialUrl();
        plugins.runOnUrlFirstLoadPlugins(context);
    }

    private void loadCrawljaxCore(CrawljaxConfiguration config, String url) {
        this.url = url;
        this.context = Guice.createInjector(new CoreModule(config)).getInstance(CrawlerContext.class);
        this.plugins = new Plugins(config, new MetricRegistry());
        this.browser = context.getBrowser();

        LOGGER.info("Loading plugins...");
        plugins.runPreCrawlingPlugins(config);

        oldPage = Guice.createInjector(new CoreModule(config)).getInstance(EmbeddedBrowser.class);

        this.formHandler = new FormHandler(browser, config.getCrawlRules());

        this.oracleComparator = new StateComparator(config.getCrawlRules());
        this.invariantChecker =
                new ConditionTypeChecker<Invariant>(config.getCrawlRules().getInvariants());
        this.waitConditionChecker = new WaitConditionChecker(config.getCrawlRules());
    }

    private void setGlobalFileVars(CrawljaxConfiguration config, String crawlPath, String testRecords)
            throws IOException {
        testRecordsFolder = testRecords;

        manager = new WorkDirManager();

        int newID = manager.getNumTestRecords(new File(testRecordsFolder));

        currTestRunFolderPath = testRecordsFolder + File.separator + newID;

        File currTestRunFolder = new File(currTestRunFolderPath);

        if (!currTestRunFolder.exists()) {
            currTestRunFolder.mkdirs();
        }

        this.outputPath = testRecordsFolder + File.separator + newID;

        String outputDiff = outputPath + "diffs" + File.separator;
        FileUtils.deleteDirectory(new File(outputDiff));
        FSUtils.checkFolderForFile(outputDiff);

        this.tmpPath = outputDiff + "tmp" + File.separator;
        FileUtils.deleteDirectory(new File(this.tmpPath));
        FSUtils.checkFolderForFile(this.tmpPath);

        /* The folder where we will temporarily store screenshots. */
        screenshotsOutputFolder = new File(this.tmpPath);

        /* The folder where the oracle screenshots are stored. */
        String crawlScreenshots = (new File(crawlPath, "screenshots")).getAbsolutePath();
        screenshotsInputFolder = new File(crawlScreenshots);
    }

    private void loadHybridState(StateVertex state, FragmentManager fragmentManager) throws IOException {

        File fragDom =
                new File(crawlPath + File.separator + "doms" + File.separator + "frag_" + state.getName() + ".html");
        if (Files.notExists(fragDom.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            System.err.println("No Frag Dom found for Hybrid State : " + fragDom.toPath());
            return;
        }
        String domString = FileUtils.readFileToString(fragDom, Charset.defaultCharset());

        File screenshotFile = getOldScreenshotFile(state.getName());
        if (Files.notExists(screenshotFile.toPath(), LinkOption.NOFOLLOW_LINKS)) {
            System.err.println("No screenshotFile found for Hybrid State : " + screenshotFile.toPath());
            return;
        }
        BufferedImage screenshot = ImageIO.read(screenshotFile);
        FragmentationPlugin.loadFragmentState(state, fragmentManager, DomUtils.asDocument(domString), screenshot);
        LOGGER.info("Loaded {} offline", state.getName());
    }

    private File getOldScreenshotFile(String stateName) {
        File screenshotFile =
                new File(crawlPath + File.separator + "screenshots" + File.separator + stateName + ".png");
        return screenshotFile;
    }

    private File getNewScreenshotFile(StateVertex state) {
        File screenshotFile =
                new File(outputPath + File.separator + "screenshots" + File.separator + state.getName() + ".png");
        return screenshotFile;
    }

    private File getNewDomFile(StateVertex state) {
        File screenshotFile =
                new File(outputPath + File.separator + "doms" + File.separator + state.getName() + ".html");
        return screenshotFile;
    }

    private void cleanEventables() {
        for (Eventable eventable : mapEventables.values()) {
            Builder<String, String> builder = ImmutableMap.builder();
            for (String attribute : eventable.getElement().getAttributes().keySet()) {
                boolean filter = false;
                for (String attr : VipsUtils.getVipsAttributes()) {
                    if (attr.equalsIgnoreCase(attribute)) {
                        filter = true;
                        break;
                    }
                }
                if (!filter) {
                    builder.put(
                            attribute, eventable.getElement().getAttributes().get(attribute));
                }
            }
            ImmutableMap<String, String> attributes = builder.build();
            Element element = new Element(
                    eventable.getElement().getTag(), eventable.getElement().getText(), attributes);
            eventable.setElement(element);
        }
    }

    TestRecord newTestRecord(String methodName, List<TestRecord> testRecords, ReportBuilder reportBuilder) {
        String methodRecordFolderPath = currTestRunFolderPath + File.separator + methodName;

        File methodRecordFolder = new File(methodRecordFolderPath);
        if (!methodRecordFolder.exists()) {
            methodRecordFolder.mkdir();
        }

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
     * @throws Exception On error.
     */
    public void goToInitialUrl() throws Exception {
        // browser.getWebDriver().manage().window().setSize(new Dimension(1200, 890));
        browser.goToUrl(new URI(url));
        waitConditionChecker.wait(browser);
        plugins.runOnUrlLoadPlugins(context);
    }

    /**
     * Closes browser and writes report.
     *
     * @throws Exception On error.
     */
    public void tearDown() throws Exception {
        Thread.sleep(400);

        // if there's still a method it failed
        reportBuilder.methodFail();
        // reportBuilder.build();

        for (TestRecord testRecord : testRecords) {
            manager.saveTestRecord(testRecord, currTestRunFolderPath + File.separator + testRecord.getMethodName());
        }

        String templateFile = null;
        //				WorkDirManager.TEST_RESULTS_HTML_VM;

        if (isHybridState) {
            templateFile = "TestResults2.html.vm";
        } else {
            templateFile = "TestResults.html.vm";
        }

        manager.saveTestRecordMap(
                testRecords,
                manager.getNumTestRecords(new File(testRecordsFolder)),
                url,
                currTestRunFolderPath,
                "testRun.json",
                "TestResults.html",
                templateFile);

        LOGGER.info("Report generated in " + this.currTestRunFolderPath);

        browser.close();
        /*
         * oldPage.close(); newPage.close();
         */
        oldPage.close();
    }

    /**
     * Fill in form inputs.
     *
     * @param formInputs The form inputs to handle.
     * @throws Exception On error.
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

    private boolean fireEvent(Eventable eventable) {
        Eventable eventToFire = eventable;

        boolean isFired = false;
        try {
            isFired = browser.fireEventAndWait(eventToFire);
        } catch (ElementNotInteractableException | NoSuchElementException e) {
            if (eventToFire.getElement() != null
                    && "A".equals(eventToFire.getElement().getTag())) {
                isFired = visitAnchorHrefIfPossible(eventToFire);
            } else {
                LOGGER.debug("Ignoring invisible element {}", eventToFire.getElement());
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Interrupted during fire event");
            return false;
        }

        LOGGER.debug("Event fired={} for eventable {}", isFired, eventable);

        if (isFired) {
            // Let the controller execute its specified wait operation on the browser thread
            // safe.
            waitConditionChecker.wait(browser);
            browser.closeOtherWindows();
            return true;
        } else {
            /*
             * Execute the OnFireEventFailedPlugins with the current crawlPath with the
             * crawlPath removed 1 state to represent the path TO here.
             */
            // plugins.runOnFireEventFailedPlugins(context, eventable,
            // crawlpath.immutableCopyWithoutLast());
            return false; // no event fired
        }
    }

    public Eventable getValidXpath(Eventable eventable) {

        Eventable newEventable = null;
        String xpath = eventable.getIdentification().getValue();

        Node old = null;
        try {
            Document dom = DomUtils.asDocument(browser.getStrippedDom());
            old = DomUtils.getElementByXpath(dom, xpath);

        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (old != null) {
            newEventable = new Eventable(old, eventable.getEventType());
        } else {
            newEventable = eventable;
        }

        ElementResolver er = new ElementResolver(eventable, browser);
        String newXPath = er.resolve();
        if (newXPath != null) {
            newEventable.getIdentification().setValue(newXPath);
            // return newXPath;
        } else {
            if (old != null) {
                newEventable.getIdentification().setValue(xpath);
                ;
            } else {
                return null;
            }
        }
        return newEventable;
    }

    // public boolean fireEvent(long eventableId, List<FormInput> inputs) {
    // Eventable eventable = getEventable(eventableId);
    //// eventables.add(eventable);
    //// reportBuilder.addEventable(eventable);
    //// String xpath = eventable.getIdentification().getValue();
    //// ElementResolver er = new ElementResolver(eventable, browser);
    //// String newXPath = er.resolve();
    // Eventable newEventable = getValidXpath(eventable);
    //// if(newXPath == null)
    //// return false;
    // if(newEventable!=null)
    // eventable = newEventable;
    //
    //// eventable.setIdentification(new Identification(How.xpath, newXPath));
    // if(inputs!=null) {
    // try {
    // handleFormInputs(inputs);
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    //// String afterFormHandling = getValidXpath(eventable);
    ////
    //// // check if inputs broke the eventable
    //// if(newXPath!=null && afterFormHandling ==null) {
    //// LOGGER.info("Resetting form Inputs");
    //// formHandler.resetFormInputs(inputs);
    //// }
    //// else if(afterFormHandling!=null) {
    //// eventable.setIdentification(new Identification(How.xpath,
    // afterFormHandling));
    //// }
    // }
    // return fireEvent2(eventable);
    // }
    //
    // public boolean fireEvent2(Eventable eventable) {
    // eventables.add(eventable);
    // reportBuilder.addEventable(eventable);
    // String xpath = eventable.getIdentification().getValue();
    // boolean fired = false;
    // if(xpath!=null) {
    // fired = fireEvent(eventable);
    // browser.handlePopups();
    // }
    // if (!fired) {
    // // String orgDom = "";
    // // try {
    // // orgDom = eventable.getEdge().getFromStateVertex().getDom();
    // // } catch (Exception e) {
    // // // TODO: Danny fix
    // // orgDom = "<html>todo: fix</html>";
    // // // LOGGER.info("Warning, could not get original DOM");
    // // }
    // // reportBuilder.addFailure(new EventFailure(browser, currentTestMethod,
    // eventables,
    // // orgDom, browser.getDom()));
    // LOGGER.error("Could not fire the event {}", eventable.getId());
    // LOGGER.info("Tried xpath {}", xpath);
    // reportBuilder.markLastEventableFailed();
    // }
    // waitConditionChecker.wait(browser);
    // return fired;
    // }

    /**
     * @param eventableId Id of the eventable.
     * @return whether the event is fired
     */
    public boolean fireEvent(long eventableId) {
        try {
            // browser.closeOtherWindows();
            Eventable eventable = getEventable(eventableId);
            eventables.add(eventable);
            reportBuilder.addEventable(eventable);
            String xpath = eventable.getIdentification().getValue();

            // ElementResolver er = new ElementResolver(eventable, browser);
            // String newXPath = er.resolve();
            Eventable newEventable = getValidXpath(eventable);
            if (newEventable != null) {
                eventable = newEventable;
                LOGGER.info("Updated eventable ");
            }
            boolean fired = false;

            // if (newXPath != null) {
            // if (!xpath.equals(newXPath)) {
            // LOGGER.info("XPath of \"" + eventable.getElement().getText()
            // + "\" changed from " + xpath + " to " + newXPath);
            // }
            // eventable.setIdentification(new Identification(How.xpath, newXPath));
            LOGGER.info("Firing: " + eventable);

            // try {
            // fired = browser.fireEventAndWait(eventable);
            // } catch (ElementNotVisibleException | NoSuchElementException e) {
            // if (eventable.getElement() != null
            // && "A".equals(eventable.getElement().getTag())) {
            // fired = visitAnchorHrefIfPossible(eventable);
            // } else {
            // LOGGER.debug("Ignoring invisble element {}", eventable.getElement());
            // }
            // }
            fired = fireEvent(eventable);
            browser.handlePopups();
            // }
            if (!fired) {
                // String orgDom = "";
                // try {
                // orgDom = eventable.getEdge().getFromStateVertex().getDom();
                // } catch (Exception e) {
                // // TODO: Danny fix
                // orgDom = "<html>todo: fix</html>";
                // // LOGGER.info("Warning, could not get original DOM");
                // }
                // reportBuilder.addFailure(new EventFailure(browser, currentTestMethod,
                // eventables,
                // orgDom, browser.getDom()));
                LOGGER.error("Could not fire the event {}", eventableId);
                LOGGER.info("Tried xpath {}", eventable.getIdentification().getValue());
                reportBuilder.markLastEventableFailed();
            }
            waitConditionChecker.wait(browser);
            return fired;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private boolean checkLocator(String xpath, ReportBuilder reportBuilder) {
        StateVertex state = getStateVertex((long) reportBuilder.getLastState().getStateVertex());
        if (state instanceof HybridStateVertexImpl) {
            try {
                XPathHelper.evaluateXpathExpression(state.getDocument(), xpath);
                return false;
            } catch (XPathExpressionException | IOException e) {
                LOGGER.error("Locator will fail for {}", state.getId());
                return true;
            }
        } else {
            try {
                XPathHelper.evaluateXpathExpression(state.getStrippedDom(), xpath);
                return false;
            } catch (XPathExpressionException | IOException e) {
                LOGGER.error("Locator will fail for {}", state.getId());
                return true;
            }
        }
    }

    public boolean loadOldPage(String domString, String url) {
        WebDriver driver = this.oldPage.getWebDriver();
        driver.navigate().to(url);
        try {
            ((JavascriptExecutor) driver)
                    .executeScript("document.getElementsByTagName('html')[0].innerHTML=arguments[0]", domString);
        } catch (Exception ex) {
            LOGGER.error("Could not load the old page for the given dom");
            return false;
        }
        return true;
    }

    /**
     * @param StateVertexId The id of the state vertix.
     * @return the State with id StateVertex Id
     */
    public StateVertex getStateVertex(Long StateVertexId) {
        StateVertex stateVertex = mapStateVertices.get(StateVertexId);
        // if(stateVertex instanceof HybridStateVertexImpl) {
        // if(lazyLoad) {
        // try {
        // loadHybridState(stateVertex, fragmentManager);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // if(mutationRecords!=null &&
        // mutationRecords.containsKey(stateVertex.getName())) {
        // transferMutation(stateVertex);
        // }
        // }
        // }
        return stateVertex;
    }

    public boolean compareCurrentScreenshotWithState(long StateVertexId) {
        StateVertex vertex = getStateVertex(StateVertexId);
        return compareCurrentScreenshotWithState(vertex, reportBuilder, currTestRecord);
    }

    /**
     * @param vertex The state vertex.
     * @return return where the current DOM in the browser is equivalent with the state with
     * StateVertexId
     */
    public boolean compareCurrentScreenshotWithState(
            StateVertex vertex, ReportBuilder reportBuilder, TestRecord currTestRecord) {

        /* The screenshots from before and after the change. */
        // StateVertex vertex = getStateVertex(StateVertexId);
        File oldScreenshot = oldScreenShotFile(vertex.getName());
        File newScreenshot = newScreenShotFile(vertex.getName());

        /* Save the current screenshot to a temporary folder. */
        saveScreenshot(getBrowser(), vertex.getName(), newScreenshot);

        /* Create a visual diff of the screenshots. */
        ObjectDiff diff = visualDiff(oldScreenshot, newScreenshot);

        /* Build a report if the states differ. */
        if (diff.hasChanges()) {

            /* Write the annotated file to disk. */
            File oldFile = oldDiffFile(vertex.getName(), "png");
            File newFile = newDiffFile(vertex.getName(), "png");
            try {
                ObjectDetection.directoryCheck(currTestRunFolderPath);
                Imgcodecs.imwrite(oldFile.getAbsolutePath(), diff.annotateOldPage());
                Imgcodecs.imwrite(newFile.getAbsolutePath(), diff.annotateNewPage());
                currTestRecord.writeDiffToTestRecord(
                        vertex.getName(), oldFile.getName(), newFile.getName(), "different");
            } catch (IOException e) {
                LOGGER.debug("Annotated files not written to disk because {}", e.getMessage(), e);
            }
            reportBuilder.markLastStateDifferent();
            return false;
        }

        return true;
    }

    /*
     * public boolean uniqueMapping(List<Fragment> newFragments, List<Fragment>
     * expectedFragments, FragmentManager manager) { if(newFragments.isEmpty() &&
     * expectedFragments.isEmpty()) return true;
     *
     * if(newFragments.size() != expectedFragments.size()) return false;
     *
     * HashMap<Integer, Integer> mapping = new HashMap<>();
     *
     * boolean uniqueMap = true; boolean allMappingFound = true;
     *
     * for(Fragment newFragment : newFragments) { // System.out.println("new" +
     * newFragment.getId()); boolean mappingFound = false; boolean unique = true;
     * for(Fragment oldFragment: expectedFragments) { // FragmentComparision comp =
     * oldFragment.compare(newFragment); // System.out.println(" old " +
     * oldFragment.getId()); // System.out.println("compared " + comp); //
     * List<Fragment> related = getRelatedFragments(newFragment); // List<Fragment>
     * related2 = getRelatedFragments(expected);
     * if(manager.getRelatedFragments(newFragment).contains(oldFragment)) {
     * if(mapping.containsValue(oldFragment.getId())) { //
     * System.out.println("Repeat of mapping"); mappingFound = true; unique = false;
     * mapping.put(newFragment.getId(), oldFragment.getId()); } else { if(!unique) {
     * unique = true; mapping.remove(newFragment.getId()); } mappingFound = true;
     * System.out.println("mapped useless: " + newFragment.getId() + " " +
     * oldFragment.getId()); mapping.put(newFragment.getId(), oldFragment.getId());
     * break; } } } if(!mappingFound) { //
     * System.out.println("No mapping found for {}" + newFragment.getId()); }
     * allMappingFound = mappingFound && allMappingFound; uniqueMap = unique &&
     * uniqueMap; }
     *
     * if(!allMappingFound) return false;
     *
     * if(!uniqueMap) return false; else return true; }
     */

    /*
     * public boolean areND2(Fragment frag1, Fragment frag2, FragmentManager
     * manager) { List<Fragment> newFragments = frag1.getDomChildren();
     *
     * List<Fragment> expectedFragments = frag2.getDomChildren();
     *
     * if (newFragments.size() == expectedFragments.size()) { //
     * System.out.println("May be near duplicates of type 1"); }
     *
     * HashMap<Integer, Integer> mapping = new HashMap<>();
     *
     *
     *
     * boolean uniqueMap = true; boolean allMappingFound = true; List<Fragment>
     * uselessFragments1 = new ArrayList<Fragment>(); List<Fragment>
     * uselessFragments2 = new ArrayList<Fragment>();
     *
     * for(Fragment newFragment : newFragments) {
     * if(!FragmentManager.usefulFragment(newFragment)) {
     * if(!uselessFragments1.contains(newFragment))
     * uselessFragments1.add(newFragment); continue; }
     *
     * // System.out.println("new" + newFragment.getId()); boolean mappingFound =
     * false; boolean unique = true; for(Fragment oldFragment: expectedFragments) {
     * if(!FragmentManager.usefulFragment(oldFragment)) {
     * if(!uselessFragments2.contains(oldFragment))
     * uselessFragments2.add(oldFragment); continue; }
     *
     * if(manager.getRelatedFragments(newFragment).contains(oldFragment)) {
     * if(mapping.containsValue(oldFragment.getId())) {
     * System.out.println("Repeat of mapping"); mappingFound = true; unique = false;
     * mapping.put(newFragment.getId(), oldFragment.getId()); } else { if(!unique) {
     * unique = true; mapping.remove(newFragment.getId()); } mappingFound = true;
     * System.out.println("mapped : " + newFragment.getId() + " " +
     * oldFragment.getId()); mapping.put(newFragment.getId(), oldFragment.getId());
     * break; } } } if(!mappingFound) { //
     * System.out.println("No mapping found for {}" + newFragment.getId()); }
     * allMappingFound = mappingFound && allMappingFound; uniqueMap = unique &&
     * uniqueMap; }
     *
     * boolean nd2 = false;
     *
     * if(!allMappingFound) { for(Fragment newFragment: newFragments) {
     * if(!mapping.containsKey(newFragment.getId()) &&
     * FragmentManager.usefulFragment(newFragment)){ //
     * System.out.println("Trying ND2  : " + newFragment.getId() ); for(Fragment
     * oldFragment: expectedFragments) { boolean areND2 = areND2(newFragment,
     * oldFragment, manager); if(areND2) { mapping.put(newFragment.getId(),
     * oldFragment.getId()); System.out.println("ND2 Mapped : " +
     * newFragment.getId() + " :  " + oldFragment.getId()); nd2 = true; } } } }
     *
     * } // return false;
     *
     * if(mapping.isEmpty()) { return false; } boolean uselessMapped = false;
     * if(uselessFragments1.size() == uselessFragments2.size()) { uselessMapped =
     * uniqueMapping(uselessFragments1, uselessFragments2, manager);
     * if(uselessMapped) { System.out.println("Useless mapped for "+ frag1.getId() +
     * frag2.getId()); // return true; } }
     *
     * if(!uniqueMap) return true; else return true;
     *
     * }
     */

    public StateComparision fragDiff(StateVertex oldState, StateVertex newState, FragmentManager fragmentManager) {
        // Do not assign dynamic fragments during test run.
        boolean assignDynamic = false;
        // StateComparision comp2 = fragmentManager.cacheStateComparision(oldState,
        // newState, assignDynamic);
        StateComparision comp = fragmentManager.cacheStateComparision(newState, oldState, assignDynamic);

        LOGGER.info("Mapping result  {}", fragmentManager.areND2(newState, oldState));
        // LOGGER.info("Tree Diff {}", areND2(newState.getRootFragment(),
        // oldState.getRootFragment(), fragmentManager));
        LOGGER.info("Using Different Nodes {} : {}", comp);
        return comp;
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
        ObjectDetection srcDetection = new ObjectDetection(pageObjectFactory, oldScreenshot.getAbsolutePath());
        ObjectDetection dstDetection = new ObjectDetection(pageObjectFactory, newScreenshot.getAbsolutePath());

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
                    browser,
                    e.getMessage());
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

    private File oldDiffFile(String name, String extension) {

        return new File(
                manager.getDiffsFolder(this.currTestRunFolderPath, this.currTestRecord), name + "_old." + extension);
    }

    private File newDiffFile(String name, String extension) {

        return new File(
                manager.getDiffsFolder(this.currTestRunFolderPath, this.currTestRecord), name + "_new." + extension);
    }

    public StateVertex getHybridStateVertex() {

        int id = nextStateNameCounter.incrementAndGet();
        StateVertex state = stateVertexFactory.newStateVertex(
                id,
                browser.getCurrentUrl(),
                "state" + id,
                browser.getStrippedDom(),
                browser.getStrippedDomWithoutIframeContent(),
                browser);

        FragmentationPlugin.fragmentState(state, fragmentManager, browser, new File(outputPath), true);

        return state;
    }

    public boolean fragDiffBool(long stateVertexId) {
        LOGGER.info("");
        StateVertex vertex = null;
        try {
            vertex = getStateVertex(stateVertexId);
        } catch (Exception ex) {
            LOGGER.error("Could nt load state {}", stateVertexId);
            return false;
        }
        if (vertex == null) {
            LOGGER.error("State is null {}", stateVertexId);
            return false;
        }

        StateVertex newVertex = getHybridStateVertex();

        try {
            recordNewState(newVertex);
        } catch (IOException e) {
            LOGGER.error("Could not record new vertex {}", newVertex.getName());
        }

        boolean comp = setStateComparison(vertex, newVertex, fragmentManager, reportBuilder, currTestRecord, false);

        return comp;
    }

    private boolean setStateComparison(
            StateVertex vertex,
            StateVertex newVertex,
            FragmentManager fragmentManager,
            ReportBuilder reportBuilder,
            TestRecord currTestRecord,
            boolean mutant) {
        reportBuilder.getLastState().setTraceState(newVertex.getId());
        StateComparision comparision =
                fragDiff(vertex, newVertex, fragmentManager, reportBuilder, currTestRecord, mutant);

        boolean comp = false;
        switch (comparision) {
            case DUPLICATE:
                comp = true;
                break;
            case NEARDUPLICATE1:
                comp = true;
                break;
            case NEARDUPLICATE2:
                comp = true;
                break;
            case DIFFERENT:
                comp = false;
                break;
            default:
                comp = false;
                break;
        }

        reportBuilder.setLastStateComparison(comparision.name());
        return comp;
    }

    public StateComparision fragDiff(
            StateVertex vertex,
            StateVertex newVertex,
            FragmentManager fragmentManager,
            ReportBuilder reportBuilder,
            TestRecord currTestRecord,
            boolean mutant) {
        try {
            return fragDiffExcept(vertex, fragmentManager, reportBuilder, currTestRecord, mutant, newVertex);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return StateComparision.ERRORCOMPARING;
    }

    private void recordNewState(StateVertex newVertex) throws IOException {
        File traceImage = getNewScreenshotFile(newVertex);
        /* Save the current screenshot to a temporary folder. */
        ImageIO.write(((HybridStateVertexImpl) newVertex).getImage(), "png", traceImage);

        File traceDom = getNewDomFile(newVertex);

        try {
            FileOutputStream fe = new FileOutputStream(traceDom.getAbsolutePath());
            fe.write((new Gson()).toJson(newVertex.getStrippedDom()).getBytes());
            fe.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StateComparision fragDiffExcept(
            StateVertex vertex,
            FragmentManager fragmentManager,
            ReportBuilder reportBuilder,
            TestRecord currTestRecord,
            boolean mutant,
            StateVertex newVertex)
            throws Exception {
        StateComparision comp = null;

        if (fragmentManager == null) {
            fragmentManager = new FragmentManager(null);
            FragmentationPlugin.addFragments(vertex, fragmentManager);
            FragmentationPlugin.addFragments(newVertex, fragmentManager);
        }

        comp = fragDiff(newVertex, vertex, fragmentManager);

        StatePair compPair = fragmentManager.getCachedComparision(vertex, newVertex);

        FragDiff diff = new FragDiff(compPair, fragmentManager, comp);

        try {
            ObjectDetection.directoryCheck(currTestRunFolderPath);

            String oldFilePath, newFilePath;
            File oldFile, newFile;
            /*
             * if(mutant && mutation) { // Mutation frag diff oldFile =
             * oldDiffFile(vertex.getName()+"_mut", "png"); newFile =
             * newDiffFile(newVertex.getName()+"_mut", "png");
             *
             * oldFilePath = new
             * File(currTestRunFolderPath).toURI().relativize(oldFile.toURI()).getPath();
             * newFilePath = new
             * File(currTestRunFolderPath).toURI().relativize(newFile.toURI()).getPath();
             *
             * }
             */
            if (OUTPUT_DIFF_IMAGE) {

                oldFile = oldDiffFile(vertex.getName(), "png");
                newFile = newDiffFile(newVertex.getName(), "png");
                oldFilePath = new File(currTestRunFolderPath)
                        .toURI()
                        .relativize(oldFile.toURI())
                        .getPath();
                newFilePath = new File(currTestRunFolderPath)
                        .toURI()
                        .relativize(newFile.toURI())
                        .getPath();

            } else {

                oldFile = getOldScreenshotFile(vertex.getName());
                oldFilePath = "../../screenshots/" + vertex.getName() + ".png";

                newFile = getNewScreenshotFile(newVertex);
                newFilePath = new File(currTestRunFolderPath)
                        .toURI()
                        .relativize(newFile.toURI())
                        .getPath();
            }

            diff.setOldFile(oldFilePath);
            diff.setNewFile(newFilePath);

            diff.annotateOldPage(OUTPUT_DIFF_IMAGE, oldFile);
            diff.annotateNewPage(OUTPUT_DIFF_IMAGE, newFile);

            String oldAnnotName = vertex.getName();
            String newAnnotName = newVertex.getName();
            if (mutant) {
                oldAnnotName += "_mut";
                newAnnotName += "_mut";
            }

            ImageAnnotations annotation = diff.getOldPageAnnotation();
            File output = oldDiffFile(oldAnnotName, "json");
            exportAnnotations(annotation, output);
            ImageAnnotations annotation2 = diff.getNewPageAnnotation();
            File output2 = newDiffFile(newAnnotName, "json");
            exportAnnotations(annotation2, output2);

            // currTestRecord.writeDiffToTestRecord(vertex.getName(), oldFile.getName(),
            // newFile.getName(), comp.name());
            currTestRecord.writeDiffToTestRecord(vertex.getName(), diff);
        } catch (IOException e) {
            LOGGER.debug("Annotated files not written to disk because {}", e.getMessage(), e);
        }

        switch (comp) {
            case DIFFERENT:
                LOGGER.info("State {} is different from cached state {}", newVertex.getId(), vertex.getId());
                reportBuilder.markLastStateDifferent();
                break;
            case DUPLICATE:
            case NEARDUPLICATE1:
                // Warn Level set in fragDiff page annotation
                reportBuilder.setWarnLevel(diff.getLevel());
                break;
            case NEARDUPLICATE2:
                reportBuilder.setWarnLevel(WarnLevel.LEVEL3);
                LOGGER.info("States are not different");
                break;
            default:
                LOGGER.error("COMP is not handled" + comp);
        }

        return comp;
    }

    private void exportAnnotations(ImageAnnotations annotation, File output) throws FileNotFoundException, IOException {
        FileOutputStream file = new FileOutputStream(output);
        String json = new Gson().toJson(annotation);
        file.write(json.getBytes());
        file.close();
    }

    public boolean compareCurrentDomWithState(long StateVertexId) {

        StateVertex vertex = getStateVertex(StateVertexId);

        return compareCurrentDomWithState(vertex, reportBuilder, currTestRecord);
    }

    /**
     * @param vertex The state vertex.
     * @return return where the current DOM in the browser is equivalent with the state with
     * StateVertexId
     */
    public boolean compareCurrentDomWithState(
            StateVertex vertex, ReportBuilder reportBuilder, TestRecord currTestRecord) {
        // StateVertex vertex = getStateVertex(StateVertexId);
        String stateDom = vertex.getStrippedDom();
        String newDom = oracleComparator.getStrippedDom(browser);
        Diff diff;
        Document oldDoc, newDoc;
        try {
            oldDoc = DomUtils.asDocument(stateDom);
            newDoc = DomUtils.asDocument(newDom);
            diff = DiffBuilder.compare(oldDoc)
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

            LOGGER.info("Not Equivalent with state" + vertex.getId());

            try {

                /* Write the annotated file to disk. */
                File oldFile = oldDiffFile(vertex.getName(), "html");
                File newFile = newDiffFile(vertex.getName(), "html");
                try {
                    FileWriter writer = new FileWriter(oldFile);
                    writer.write(stateDom);
                    writer.flush();
                    writer.close();
                    writer = new FileWriter(newFile);
                    writer.write(newDom);
                    writer.flush();
                    writer.close();
                    currTestRecord.writeDiffToTestRecord(vertex.getName(), stateDom, newDom, "different");
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
     * @param currentTestMethod The current method that is used for testing
     */
    public void newCurrentTestMethod(String currentTestMethod) {
        LOGGER.info("New test: " + currentTestMethod);
        currTestRecord = newTestRecord(currentTestMethod, testRecords, reportBuilder);
        eventables.clear();
    }

    /**
     * @param currentTestMethod The current method that is used for testing
     */
    public void newCurrentTestMethod(String currentTestMethod, int methodNumber) {
        LOGGER.info("New test {} {}", methodNumber, currentTestMethod);
        currTestRecord = newTestRecord(currentTestMethod, testRecords, reportBuilder);
        currTestRecord.setMethodNumber(methodNumber);

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

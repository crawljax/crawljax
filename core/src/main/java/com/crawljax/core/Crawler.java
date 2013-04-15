// package com.crawljax.core;
//
// import java.net.MalformedURLException;
// import java.net.URL;
// import java.util.List;
// import java.util.Map.Entry;
// import java.util.concurrent.CopyOnWriteArrayList;
// import java.util.concurrent.TimeUnit;
// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
//
// import org.openqa.selenium.ElementNotVisibleException;
// import org.openqa.selenium.NoSuchElementException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// import com.crawljax.browser.EmbeddedBrowser;
// import com.crawljax.core.configuration.CrawljaxConfiguration;
// import com.crawljax.core.exception.BrowserConnectionException;
// import com.crawljax.core.exception.CrawlPathToException;
// import com.crawljax.core.plugin.OnUrlLoadPlugin;
// import com.crawljax.core.plugin.Plugins;
// import com.crawljax.core.state.CrawlPath;
// import com.crawljax.core.state.Element;
// import com.crawljax.core.state.Eventable;
// import com.crawljax.core.state.Eventable.EventType;
// import com.crawljax.core.state.Identification;
// import com.crawljax.core.state.StateFlowGraph;
// import com.crawljax.core.state.StateMachine;
// import com.crawljax.core.state.StateVertex;
// import com.crawljax.forms.FormHandler;
// import com.crawljax.forms.FormInput;
// import com.crawljax.util.ElementResolver;
// import com.crawljax.util.UrlUtils;
//
// /**
// * Class that performs crawl actions. It is designed to run inside a Thread.
// */
// public class Crawler implements Runnable {
//
// private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);
//
// /**
// * Enum for describing what has happened after a {@link Crawler#clickTag(Eventable)} has been
// * performed.
// *
// * @see Crawler#clickTag(Eventable)
// */
// private static enum ClickResult {
// CLONE_DETECTED, NEW_STATE, DOM_UNCHANGED, LEFT_DOMAIN
// }
//
// /**
// * The central DataController. This is a multiple to 1 relation Every Thread shares an instance
// * of the same controller! All operations / fields used in the controller should be checked for
// * thread safety.
// */
// private final CrawljaxController controller;
//
// /**
// * Depth register.
// */
// private final AtomicInteger depth = new AtomicInteger();
//
// /**
// * The path followed from the index to the current state.
// */
// private final CrawlPath backTrackPath;
//
// /**
// * The utility which is used to extract the candidate clickables.
// */
// private CandidateElementExtractor candidateExtractor;
//
// /**
// * The name of this Crawler when not default (automatic) this will be added to the Thread name
// * in the thread as (name). In the {@link CrawlerExecutor#beforeExecute(Thread, Runnable)} the
// * name is retrieved using the {@link #toString()} function.
// *
// * @see Crawler#toString()
// * @see CrawlerExecutor#beforeExecute(Thread, Runnable)
// */
// private final String name;
//
// /**
// * The sateMachine for this Crawler, keeping track of the path crawled by this Crawler.
// */
// private final StateMachine stateMachine;
//
// private final CrawljaxConfiguration config;
//
// /**
// * The object to places calls to add new Crawlers or to remove one.
// */
// private final CrawlQueueManager crawlQueueManager;
//
// private final Plugins plugins;
//
// private FormHandler formHandler;
//
// private boolean fired = false;
//
// /**
// * The main browser window 1 to 1 relation; Every Thread will get on browser assigned in the run
// * function.
// */
// private EmbeddedBrowser browser;
//
// /**
// * @param mother
// * the main CrawljaxController
// * @param exactEventPath
// * the event path up till this moment.
// * @param name
// * a name for this crawler (default is empty).
// */
// public Crawler(CrawljaxController mother, List<Eventable> exactEventPath, String name) {
// this(mother, new CrawlPath(exactEventPath), name);
// }
//
// /**
// * Private Crawler constructor for a 'reload' crawler. Only used internally.
// *
// * @param mother
// * the main CrawljaxController
// * @param returnPath
// * the path used to return to the last state, this can be a empty list
// */
// protected Crawler(CrawljaxController mother, CrawlPath returnPath, String name) {
// this.backTrackPath = returnPath;
// this.name = name;
// this.controller = mother;
// this.config = controller.getConfiguration();
// this.plugins = config.getPlugins();
// this.crawlQueueManager = mother.getCrawlQueueManager();
// if (controller.getSession() != null) {
// this.stateMachine =
// new StateMachine(controller.getSession().getStateFlowGraph(), controller
// .getSession().getInitialState(), controller.getInvariantList(),
// plugins);
// } else {
// /*
// * Reset the state machine to null, because there is no session where to load the
// * stateFlowGraph from.
// */
// this.stateMachine = null;
// }
// }
//
// /**
// * The main function stated by the ExecutorService. Crawlers add themselves to the list by
// * calling {@link CrawlQueueManager#addWorkToQueue(Crawler)}. When the ExecutorService finds a
// * free thread this method is called and when this method ends the Thread is released again and
// * a new Thread is started
// *
// * @see java.util.concurrent.Executors#newFixedThreadPool(int)
// * @see java.util.concurrent.ExecutorService
// */
// @Override
// public void run() {
// if (!shouldContinueCrawling()) {
// return;
// }
//
// try {
// if (backTrackPath.last() != null
// && !backTrackPath.last().getTargetStateVertex().startWorking(this)) {
// LOG.warn("Could not register crawler. Quitting");
// return;
// }
//
// try {
// this.init();
// } catch (InterruptedException e) {
// LOG.debug("Could not fetch a browser from the pool");
// return;
// }
//
// // Hand over the main crawling
// if (!this.crawl()) {
// controller.terminate(false);
// }
//
// // Crawling is done; so the crawlPath of the current branch is known
// if (!fired) {
// controller.getSession().removeCrawlPath();
// }
// } catch (BrowserConnectionException e) {
// // The connection of the browser has gone down, most of the times it
// // means that the browser process has crashed.
// LOG.error("Crawler failed because the used browser died during Crawling",
// new CrawlPathToException("Crawler failed due to browser crash", controller
// .getSession().getCurrentCrawlPath(), e));
// // removeBrowser will throw a RuntimeException if the current browser
// // is the last
// // browser in the pool.
// this.controller.getBrowserPool().removeBrowser(this.getBrowser(),
// this.controller.getCrawlQueueManager());
// return;
// } catch (CrawljaxException e) {
// LOG.error("Crawl failed!", e);
// }
// /**
// * At last failure or non shutdown the Crawler.
// */
// this.shutdown();
// }
//
// /**
// * Initialize the Crawler, retrieve a Browser and go to the initial URL when no browser was
// * present. rewind the state machine and goBack to the state if there is exactEventPath is
// * specified.
// *
// * @throws InterruptedException
// * when the request for a browser is interrupted.
// */
// public void init() throws InterruptedException {
// // Start a new CrawlPath for this Crawler
// controller.getSession().startNewPath();
//
// this.browser = this.getBrowser();
// if (this.browser == null) {
// /*
// * As the browser is null, request one and got to the initial URL, if the browser is
// * Already set the browser will be in the initial URL.
// */
// this.browser = controller.getBrowserPool().requestBrowser();
// LOG.debug("Reloading page to start a new crawl path");
// this.goToInitialURL(true);
// }
// // TODO Stefan ideally this should be placed in the constructor
// this.formHandler =
// new FormHandler(getBrowser(), config.getCrawlRules().getInputSpecification(),
// config.getCrawlRules().isRandomInputInForms());
//
// this.candidateExtractor =
// new CandidateElementExtractor(controller.getElementChecker(), this.getBrowser(),
// formHandler, config);
// /**
// * go back into the previous state.
// */
// try {
// this.goBackExact(backTrackPath);
// } catch (CrawljaxException e) {
// LOG.error("Failed to backtrack", e);
// }
// }
//
// /**
// * Brings the browser to the initial state.
// *
// * @param runPlugins
// * To run the {@link OnUrlLoadPlugin} plugins.
// */
// public void goToInitialURL(boolean runPlugins) {
// LOG.info("Loading Page {}", config.getUrl());
// getBrowser().goToUrl(config.getUrl());
// controller.doBrowserWait(getBrowser());
// if (runPlugins) {
// plugins.runOnUrlLoadPlugins(getBrowser());
// }
// }
//
// /**
// * Reload the browser following the {@link #backTrackPath} to the given currentEvent.
// *
// * @param b
// * @param backTrackPath2
// * @throws CrawljaxException
// * if the {@link Eventable#getTargetStateVertex()} encounters an error.
// */
// private void goBackExact(CrawlPath path) throws CrawljaxException {
// StateVertex curState = controller.getSession().getInitialState();
//
// for (Eventable clickable : path) {
//
// if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
// return;
// }
//
// LOG.debug("Backtracking by executing {} on element: {}", clickable.getEventType(),
// clickable);
//
// this.getStateMachine().changeState(clickable.getTargetStateVertex());
//
// curState = clickable.getTargetStateVertex();
//
// controller.getSession().addEventableToCrawlPath(clickable);
//
// this.handleInputElements(clickable);
//
// if (this.fireEvent(clickable)) {
//
// int d = depth.incrementAndGet();
// LOG.debug("Crawl depth now {}", d);
//
// /*
// * Run the onRevisitStateValidator(s)
// */
// plugins.runOnRevisitStatePlugins(this.controller.getSession(), curState);
// }
//
// if (!controller.getElementChecker().checkCrawlCondition(getBrowser())) {
// return;
// }
// }
// }
//
// /**
// * Try to fire a given event on the Browser.
// *
// * @param eventable
// * the eventable to fire
// * @return true iff the event is fired
// */
// private boolean fireEvent(Eventable eventable) {
// Eventable eventToFire = eventable;
// if (eventable.getIdentification().getHow().toString().equals("xpath")
// && eventable.getRelatedFrame().equals("")) {
// eventToFire = resolveByXpath(eventable, eventToFire);
// }
// boolean isFired = false;
// try {
// isFired = getBrowser().fireEventAndWait(eventToFire);
// } catch (ElementNotVisibleException | NoSuchElementException e) {
// if (config.getCrawlRules().isCrawlHiddenAnchors() && eventToFire.getElement() != null
// && "A".equals(eventToFire.getElement().getTag())) {
// isFired = visitAnchorHrefIfPossible(eventToFire);
// } else {
// LOG.debug("Ignoring invisble element {}", eventToFire.getElement());
// }
// }
//
// LOG.debug("Event fired={} for eventable {}", isFired, eventable);
//
// if (isFired) {
//
// /*
// * Let the controller execute its specified wait operation on the browser thread safe.
// */
// controller.doBrowserWait(getBrowser());
//
// getBrowser().closeOtherWindows();
//
// return true; // An event fired
// } else {
// /*
// * Execute the OnFireEventFailedPlugins with the current crawlPath with the crawlPath
// * removed 1 state to represent the path TO here.
// */
// plugins.runOnFireEventFailedPlugins(eventable, controller.getSession()
// .getCurrentCrawlPath().immutableCopy(true));
// return false; // no event fired
// }
// }
//
// private Eventable resolveByXpath(Eventable eventable, Eventable eventToFire) {
// // The path in the page to the 'clickable' (link, div, span, etc)
// String xpath = eventable.getIdentification().getValue();
//
// // The type of event to execute on the 'clickable' like onClick,
// // mouseOver, hover, etc
// EventType eventType = eventable.getEventType();
//
// // Try to find a 'better' / 'quicker' xpath
// String newXPath = new ElementResolver(eventable, getBrowser()).resolve();
// if (newXPath != null && !xpath.equals(newXPath)) {
// LOG.debug("XPath changed from {} to {} relatedFrame: {}", xpath, newXPath,
// eventable.getRelatedFrame());
// eventToFire =
// new Eventable(new Identification(Identification.How.xpath, newXPath),
// eventType);
// }
// return eventToFire;
// }
//
// private boolean visitAnchorHrefIfPossible(Eventable eventable) {
// Element element = eventable.getElement();
// String href = element.getAttributeOrNull("href");
// if (href == null) {
// LOG.info("Anchor {} has no href and is invisble so it will be ignored", element);
// } else {
// LOG.info("Found an invisible link with href={}", href);
// try {
// URL url = UrlUtils.extractNewUrl(getBrowser().getCurrentUrl(), href);
// getBrowser().goToUrl(url);
// return true;
// } catch (MalformedURLException e) {
// LOG.info("Could not visit invisible illegal URL {}", e.getMessage());
// }
// }
// return false;
// }
//
// /**
// * Enters the form data. First, the related input elements (if any) to the eventable are filled
// * in and then it tries to fill in the remaining input elements.
// *
// * @param eventable
// * the eventable element.
// */
// private void handleInputElements(Eventable eventable) {
// CopyOnWriteArrayList<FormInput> formInputs = eventable.getRelatedFormInputs();
//
// for (FormInput formInput : formHandler.getFormInputs()) {
// if (!formInputs.contains(formInput)) {
// formInputs.add(formInput);
// }
// }
// formHandler.handleFormElements(formInputs);
// }
//
// private boolean domChanged(final Eventable eventable, StateVertex newState) {
// return plugins.runDomChangeNotifierPlugins(this.getStateMachine().getCurrentState(),
// eventable, newState, getBrowser());
// }
//
// private ClickResult crawlAction(CandidateCrawlAction action) throws CrawljaxException {
// CandidateElement candidateElement = action.getCandidateElement();
// EventType eventType = action.getEventType();
//
// if (candidateElement.allConditionsSatisfied(getBrowser())) {
//
// ClickResult clickResult = clickTag(new Eventable(candidateElement, eventType));
//
// return clickResult;
// } else {
// LOG.info("Conditions not satisfied for element: {}; State: {}", candidateElement,
// this.getStateMachine().getCurrentState().getName());
// return ClickResult.DOM_UNCHANGED;
// }
// }
//
// /**
// * @param eventable
// * the element to execute an action on.
// * @return the result of the click operation
// * @throws CrawljaxException
// * an exception.
// */
// private ClickResult clickTag(final Eventable eventable) throws CrawljaxException {
// StateVertex orrigionalState = this.getStateMachine().getCurrentState();
// // load input element values
// handleInputElements(eventable);
//
// // support for meta refresh tags
// waitForRefreshTagIfAny(eventable);
//
// LOG.debug("Executing {} on element: {}; State: {}", eventable.getEventType(),
// eventable, this.getStateMachine().getCurrentState().getName());
// if (fireEvent(eventable)) {
// return inspectPotentialNewStatus(eventable, orrigionalState);
// } else {
// return ClickResult.DOM_UNCHANGED;
// }
// }
//
// private void waitForRefreshTagIfAny(final Eventable eventable) {
// if (eventable.getElement().getTag().equalsIgnoreCase("meta")) {
// Pattern p = Pattern.compile("(\\d+);\\s+URL=(.*)");
// for (Entry<String, String> e : eventable.getElement().getAttributes().entrySet()) {
// Matcher m = p.matcher(e.getValue());
// long waitTime = parseWaitTimeOrReturnDefault(m);
// try {
// Thread.sleep(waitTime);
// } catch (InterruptedException ex) {
// LOG.info("Crawler timed out while waiting for page to reload");
// }
// }
// }
// }
//
// private long parseWaitTimeOrReturnDefault(Matcher m) {
// long waitTime = TimeUnit.SECONDS.toMillis(10);
// if (m.find()) {
// LOG.debug("URL: {}", m.group(2));
// try {
// waitTime = Integer.parseInt(m.group(1)) * 1000;
// } catch (NumberFormatException ex) {
// LOG.info("Could parse the amount of time to wait for a META tag refresh. Waiting 10 seconds...");
// }
// }
// return waitTime;
// }
//
// private ClickResult inspectPotentialNewStatus(final Eventable eventable,
// StateVertex orrigionalState) {
// if (crawlerLeftDomain()) {
// LOG.debug("The browser left the domain");
// return ClickResult.LEFT_DOMAIN;
// }
// StateVertex newState =
// new StateVertex(getBrowser().getCurrentUrl(), controller.getSession()
// .getStateFlowGraph().getNewStateName(), getBrowser().getDom(),
// this.controller.getStrippedDom(getBrowser()));
// if (domChanged(eventable, newState)) {
// controller.getSession().addEventableToCrawlPath(eventable);
// if (this.getStateMachine().swithToStateAndCheckIfClone(eventable, newState,
// this.getBrowser(), this.controller.getSession())) {
// fired = true;
// // Recurse because new state found
// spawnThreads(orrigionalState);
// return ClickResult.NEW_STATE;
// } else {
// fired = false;
// // We are in the clone state so we continue with the cloned
// // version to search for work.
// this.controller.getSession().branchCrawlPath();
// spawnThreads(orrigionalState);
// return ClickResult.CLONE_DETECTED;
// }
// } else {
// return ClickResult.DOM_UNCHANGED;
// }
// }
//
// private boolean crawlerLeftDomain() {
// return !getBrowser().getCurrentUrl().toLowerCase()
// .contains(config.getUrl().getHost().toLowerCase());
// }
//
// private void spawnThreads(StateVertex state) {
// Crawler crawler = null;
// do {
// if (crawler != null) {
// this.crawlQueueManager.addWorkToQueue(crawler);
// }
// crawler = new Crawler(this.controller, controller.getSession().getCurrentCrawlPath()
// .immutableCopy(true), "");
// } while (state.registerCrawler(crawler));
// }
//
// /**
// * Crawl through the clickables.
// *
// * @throws CrawljaxException
// * if an exception is thrown.
// */
// private boolean crawl() throws CrawljaxException {
// if (depthLimitReached()) {
// return true;
// }
//
// if (!shouldContinueCrawling()) {
// return false;
// }
//
// // Store the currentState to be able to 'back-track' later.
// StateVertex originalState = this.getStateMachine().getCurrentState();
//
// if (originalState.searchForCandidateElements(candidateExtractor)) {
// // Only execute the preStateCrawlingPlugins when it's the first time
// LOG.info("Starting preStateCrawlingPlugins...");
// List<CandidateElement> candidateElements =
// originalState.getUnprocessedCandidateElements();
// plugins.runPreStateCrawlingPlugins(controller.getSession(), candidateElements);
// // update crawlActions
// originalState.filterCandidateActions(candidateElements);
// }
//
// return crawlThroughActions(originalState);
// }
//
// private boolean crawlThroughActions(StateVertex originalState) {
// CandidateCrawlAction action =
// originalState.pollCandidateCrawlAction(this, crawlQueueManager);
// while (action != null) {
// if (depthLimitReached()) {
// return true;
// }
//
// if (!shouldContinueCrawling()) {
// return false;
// }
// ClickResult result = this.crawlAction(action);
// originalState.markAsFinished(this, action);
//
// switch (result) {
// case NEW_STATE:
// return newStateDetected(originalState);
// case CLONE_DETECTED:
// return true;
// case LEFT_DOMAIN:
// goBackOneState();
// default:
// break;
// }
// action = originalState.pollCandidateCrawlAction(this, crawlQueueManager);
// }
// return true;
// }
//
// private void goBackOneState() {
// LOG.debug("Going back one state");
// CrawlPath currentPath =
// controller.getSession().getCurrentCrawlPath().immutableCopy(false);
// goToInitialURL(false);
// if (stateMachine != null) {
// stateMachine.rewind();
// }
// controller.getSession().startNewPath();
// goBackExact(currentPath);
// }
//
// /**
// * Have we reached the depth limit?
// *
// * @param depth
// * the current depth. Added as argument so this call can be moved out if desired.
// * @return true if the limit has been reached
// */
// private boolean depthLimitReached() {
// int maxDepth = config.getMaximumDepth();
// if (this.depth.get() >= maxDepth && maxDepth != 0) {
// LOG.info("DEPTH {} reached returning from rec call. Given depth: {}", maxDepth,
// depth);
// return true;
// } else {
// return false;
// }
// }
//
// /**
// * A new state has been found!
// *
// * @param orrigionalState
// * the current state
// * @return true if crawling must continue false otherwise.
// * @throws CrawljaxException
// */
// private boolean newStateDetected(StateVertex orrigionalState) throws CrawljaxException {
//
// // An event has been fired so we are one level deeper
// int d = depth.incrementAndGet();
// LOG.info("Found a new state. Crawl depth is now {}", d);
// if (!this.crawl()) {
// // Crawling has stopped
// controller.terminate(false);
// return false;
// }
// this.getStateMachine().changeState(orrigionalState);
// return true;
// }
//
// /**
// * Terminate and clean up this Crawler, release the acquired browser. Notice that other Crawlers
// * might still be active. So this function does NOT shutdown all Crawlers active that should be
// * done with {@link CrawlerExecutor#shutdown()}
// */
// public void shutdown() {
// controller.getBrowserPool().freeBrowser(this.getBrowser());
// }
//
// /**
// * Return the browser used in this Crawler Thread.
// *
// * @return the browser used in this Crawler Thread
// */
// public EmbeddedBrowser getBrowser() {
// return browser;
// }
//
// @Override
// public String toString() {
// return this.name;
// }
//
// /**
// * @return the state machine.
// */
// public StateMachine getStateMachine() {
// return stateMachine;
// }
//
// private boolean shouldContinueCrawling() {
// return !maximumCrawlTimePassed() && !maximumStatesReached();
// }
//
// private boolean maximumCrawlTimePassed() {
// long timePassed = System.currentTimeMillis() - controller.getSession().getStartTime();
// long maxCrawlTime = config.getMaximumRuntime();
// if (maxCrawlTime != 0 && timePassed > maxCrawlTime) {
// LOG.info("Max time {} seconds passed!",
// TimeUnit.MILLISECONDS.toSeconds(maxCrawlTime));
// return true;
// } else {
// return false;
// }
// }
//
// private boolean maximumStatesReached() {
// StateFlowGraph graph = controller.getSession().getStateFlowGraph();
// int maxNumberOfStates = config.getMaximumStates();
// if ((maxNumberOfStates != 0) && (graph.getNumberOfStates() >= maxNumberOfStates)) {
// LOG.info("Max number of states {} reached!", maxNumberOfStates);
// return true;
// } else {
// return false;
// }
// }
//
// }

package com.crawljax.core;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawlScope;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Element;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.di.CoreModule.CandidateElementExtractorFactory;
import com.crawljax.di.CoreModule.FormHandlerFactory;
import com.crawljax.di.CoreModule.TrainingFormHandlerFactory;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.fragmentation.FragmentationPlugin;
import com.crawljax.oraclecomparator.StateComparator;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.crawljax.util.ElementResolver;
import com.crawljax.util.UrlUtils;
import com.crawljax.util.XPathHelper;
import com.crawljax.vips_selenium.VipsUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.xpath.XPathExpressionException;
import org.jheaps.annotations.VisibleForTesting;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class Crawler {

    private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);
    private static final int DUPLICATE_EVENT_SEED = 100000;
    private final AtomicInteger crawlDepth = new AtomicInteger();
    private final int maxDepth;
    private final EmbeddedBrowser browser;
    private final CrawlerContext context;
    private final StateComparator stateComparator;
    private final URI url;
    private final URI basicAuthUrl;
    private final CrawlScope crawlScope;
    private final Plugins plugins;
    private final FormHandler formHandler;
    private final CrawlRules crawlRules;
    private final WaitConditionChecker waitConditionChecker;
    private final CandidateElementExtractor candidateExtractor;
    // private final UnfiredCandidateActions candidateActionCache;
    private final UnfiredFragmentCandidates candidateActionCache;
    private final Provider<InMemoryStateFlowGraph> graphProvider;
    private final StateVertexFactory vertexFactory;
    private final FragmentManager fragmentManager;
    private CrawlPath crawlpath;

    @VisibleForTesting
    void setStateMachine(StateMachine sm) {
        stateMachine = sm;
    }

    private StateMachine stateMachine;
    private long nextEventableId = 0;
    private boolean avoidUnrelatedBacktracking = false;
    private boolean UseEquivalentReset = false;
    private boolean avoidDifferentBacktracking = false;
    private final boolean pairwiseFormHandling = false;
    private static final long BACKTRACKING_SEED = 1000000;
    private long nextBackTrackingId = BACKTRACKING_SEED;

    @Inject
    Crawler(
            CrawlerContext context,
            CrawljaxConfiguration config,
            StateComparator stateComparator,
            // UnfiredCandidateActions candidateActionCache,
            UnfiredFragmentCandidates candidateActionCache,
            FormHandlerFactory formHandlerFactory,
            TrainingFormHandlerFactory trainingFormHandlerFactory,
            WaitConditionChecker waitConditionChecker,
            CandidateElementExtractorFactory elementExtractor,
            Provider<InMemoryStateFlowGraph> graphProvider,
            Plugins plugins,
            StateVertexFactory vertexFactory) {
        this.context = context;
        this.graphProvider = graphProvider;
        this.vertexFactory = vertexFactory;
        this.browser = context.getBrowser();
        this.url = config.getUrl();
        this.basicAuthUrl = config.getBasicAuthUrl();
        this.crawlScope = config.getCrawlScope();
        this.plugins = plugins;
        this.crawlRules = config.getCrawlRules();
        this.maxDepth = config.getMaximumDepth();
        this.stateComparator = stateComparator;
        this.candidateActionCache = candidateActionCache;
        this.waitConditionChecker = waitConditionChecker;
        this.candidateExtractor = elementExtractor.newExtractor(browser);
        switch (crawlRules.getFormFillMode()) {
            case XPATH_TRAINING:
            case TRAINING:
                this.formHandler = trainingFormHandlerFactory.newTrainingFormHandler(browser);
                break;
            default:
                this.formHandler = formHandlerFactory.newFormHandler(browser);
                break;
        }

        this.fragmentManager = new FragmentManager(graphProvider);
        FragmentManager.setThresholds(crawlRules.getUsefulFragmentRules());

        this.context.setFragmentManager(fragmentManager);
        this.avoidUnrelatedBacktracking = crawlRules.isAvoidUnrelatedBacktracking();
        this.avoidDifferentBacktracking = crawlRules.isAvoidDifferentBacktracking();
        this.UseEquivalentReset = crawlRules.isUseEquivalentReset();
    }

    public static String printCrawlPath(List<Eventable> path, boolean print) {
        StringBuilder builder = new StringBuilder();

        if (path instanceof CrawlPath) {
            addPathInfo((CrawlPath) path, builder);
        }

        String target = null;
        String source = null;
        for (Eventable event : path) {
            try {
                source = event.getSourceStateVertex().getName();
                if (target != null && !source.equalsIgnoreCase(target)) {
                    LOG.error("Something wrong with crawlpath {} != {}", target, source);
                }
            } catch (Exception ex) {
                LOG.error("Exception while printing crawlPath", ex.getMessage());
            }
            try {
                builder.append(event.getSourceStateVertex().getName());
            } catch (Exception ex) {
                LOG.error("Exception while printing crawlPath", ex.getMessage());
            }
            builder.append("_");

            builder.append(event.getId());
            builder.append("_");
            try {
                builder.append(event.getTargetStateVertex().getName());
            } catch (Exception ex) {
                LOG.error("Exception while printing crawlPath", ex.getMessage());
            }
        }
        String pathString = builder.toString();
        if (print) {
            LOG.info(pathString);
        }

        return pathString;
    }

    private static void addPathInfo(CrawlPath path, StringBuilder builder) {
        builder.append("BT-state").append(path.getBacktrackTarget());
        builder.append(path.isBacktrackSuccess() ? ":" : ":failed:");
        builder.append(path.isReachedNearDup());
    }

    /**
     * Close the browser.
     */
    public void close() {
        interruptThread();
        LOG.info("Closing browser");
        plugins.runOnBrowserClosingPlugins(context);
        browser.close();
    }

    private Eventable getResetEquivalent() {
        if (stateMachine == null) {
            return null;
        }
        try {
            if (stateMachine
                    .getStateFlowGraph()
                    .canGoTo(
                            stateMachine.getCurrentState(),
                            stateMachine.getStateFlowGraph().getInitialState())) {
                Eventable returnEvent = stateMachine
                        .getStateFlowGraph()
                        .getShortestPath(
                                stateMachine.getCurrentState(),
                                stateMachine.getStateFlowGraph().getInitialState())
                        .get(0);
                LOG.error(
                        "Found direct edge between current {} and index :  {} ",
                        stateMachine.getCurrentState().getName(),
                        returnEvent.getId());
                return returnEvent;
            }
        } catch (Exception ex) {
            LOG.error("No direct edge between current and initial states");
        }

        ImmutableSet<Eventable> toReachIndex = stateMachine
                .getStateFlowGraph()
                .getIncomingClickable(stateMachine.getStateFlowGraph().getInitialState());

        for (Eventable toReach : toReachIndex) {
            try {
                CandidateElement elementToReach = toReach.getSourceStateVertex()
                        .getCandidateElement(toReach.getElement().getNode())
                        .get(0);
                CandidateElement equivalentCandidate =
                        stateMachine.getCurrentState().getCandidateElement(toReach);
                if (equivalentCandidate != null
                        && fragmentManager.areRelated(
                                elementToReach.getClosestFragment(), equivalentCandidate.getClosestFragment())) {
                    LOG.info(
                            "Found equivalent edge from {}. Using it on {}",
                            toReach.getSourceStateVertex().getName(),
                            stateMachine.getCurrentState().getName());
                    Eventable clone = (Eventable) toReach.clone();
                    clone.setSource(stateMachine.getCurrentState());
                    clone.setId(getBackTrackingEventableId());
                    return clone;
                }
            } catch (Exception ex) {
                LOG.error("Error finding equivalent event to {}", toReach.getId());
            }
        }

        return null;
    }

    /**
     * Resets crawljax by navigating to home url
     *
     * @param nextTarget
     */
    public void reset(int nextTarget) {

        browser.handlePopups();
        boolean equivalentResetDone = false;
        if (UseEquivalentReset) {
            if (stateMachine != null
                    && stateMachine
                            .getCurrentState()
                            .equals(stateMachine.getStateFlowGraph().getInitialState())) {
                equivalentResetDone = true;
            } else {
                Eventable equivalentEvent = getResetEquivalent();
                if (equivalentEvent != null
                        && fireEventWithInputs(equivalentEvent, equivalentEvent.getRelatedFormInputs())) {
                    boolean updated = inspectNewState(equivalentEvent);
                    LOG.info("Event resulted in state change {}", updated);
                    LOG.info("Current state after event reset {} ", stateMachine.getCurrentState());
                    if (stateMachine
                            .getCurrentState()
                            .equals(stateMachine.getStateFlowGraph().getInitialState())) {
                        LOG.info("Reached initial state... Can ignore reload URL!!");
                        equivalentResetDone = true;
                    } else {
                        LOG.info("Action didn't result in index state. Disabling equivalent reset from next time");
                    }
                }
            }
        }

        CrawlSession session = context.getSession();
        if (crawlpath != null) {
            session.addCrawlPath(crawlpath);
        }
        List<StateVertex> onURLSetTemp = new ArrayList<>();
        StateVertex previousState = null;
        if (stateMachine != null) {
            onURLSetTemp = stateMachine.getOnURLSet();
            previousState = stateMachine.getCurrentState();
        }
        stateMachine = new StateMachine(
                graphProvider.get(), crawlRules.getInvariants(), plugins, stateComparator, onURLSetTemp);
        context.setStateMachine(stateMachine);
        crawlpath = new CrawlPath(nextTarget);
        context.setCrawlPath(crawlpath);

        if (!UseEquivalentReset || !equivalentResetDone) {
            browser.goToUrl(url);
            // Checks the landing page for URL and sets the current page accordingly
            checkOnURLState(previousState);
        }

        plugins.runOnUrlLoadPlugins(context);
        crawlDepth.set(0);
    }

    private void checkOnURLState(StateVertex previousState) {

        StateVertex newState = stateMachine.newStateFor(browser);
        StateVertex clone = stateMachine.getStateFlowGraph().putIfAbsent(newState);
        if (clone == null) {
            stateMachine.setCurrentState(newState);
            stateMachine.runOnInvariantViolationPlugins(context);

            plugins.runOnNewStatePlugins(context, newState);

            parseCurrentPageForCandidateElements();

            if (newState instanceof HybridStateVertexImpl) {
                for (StateVertex existing : stateMachine.getOnURLSet()) {
                    boolean assignDynamic = true;
                    fragmentManager.cacheStateComparision(newState, existing, assignDynamic);
                }
            }
            stateMachine.getOnURLSet().add(newState);
            newState.setOnURL(true);

        } else {
            if (!clone.getName().equalsIgnoreCase("index")) {
                LOG.info("index has changed to: {}", clone.getName());
                if (!stateMachine.getOnURLSet().contains(clone)) {
                    stateMachine.getOnURLSet().add(clone);
                }
            }
            stateMachine.setCurrentState(clone);
        }

        // Adding a reload edge whenever a URL reload happens. The Crawlpaths would not have this edge
        boolean added = stateMachine
                .getStateFlowGraph()
                .addEdge(
                        previousState,
                        stateMachine.getCurrentState(),
                        new Eventable(
                                new Identification(
                                        How.url, context.getConfig().getUrl().toString()),
                                EventType.reload));
        if (!added) {
            LOG.info(
                    "Did not add reload edge from {} to {}",
                    previousState.getName(),
                    stateMachine.getCurrentState().getName());
        } else {
            LOG.info(
                    "Added a reload edge from {} to {}",
                    previousState.getName(),
                    stateMachine.getCurrentState().getName());
        }
    }

    /**
     * @param crawlTask The {@link StateVertex} this {@link Crawler} should visit to crawl.
     */
    public void execute(StateVertex crawlTask) {
        LOG.debug("Going to state {}", crawlTask.getName());
        // boolean reached = false;
        if (stateMachine == null) {
            stateMachine = new StateMachine(
                    graphProvider.get(), crawlRules.getInvariants(), plugins, stateComparator, new ArrayList<>());

            context.setStateMachine(stateMachine);
            if (crawlpath == null) {
                crawlpath = new CrawlPath(stateMachine.getCurrentState().getId());
            }
            // New browser, without the page open yet.
            if (!url.toASCIIString().equals(browser.getCurrentUrl())) {
                browser.goToUrl(url);
            }
        }
        try {
            if (crawlTask.getId() == stateMachine.getCurrentState().getId()) {
                setBTStatus(true, -1);
                crawlThroughActions();
            }
        } catch (Exception e1) {
            LOG.error("StateMachine not set yet!! Resetting the browser to start again");
        }

        LOG.info("Resetting the crawler and Going to state {}", crawlTask.getName());
        try {
            reset(crawlTask.getId());

            boolean reachable = reachFromHome(crawlTask);
            if (!reachable) {
                LOG.info("state unreachable: Removing from candidate actions {}", crawlTask.getName());
                candidateActionCache.purgeActionsForState(crawlTask);
                return;
            }
            crawlThroughActions();
        } catch (StateUnreachableException ex) {
            LOG.info(ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
            candidateActionCache.purgeActionsForState(ex.getTarget());
        } catch (CrawlerLeftDomainException e) {
            LOG.info("The crawler left the domain. No biggy, we'll just go somewhere else.");
            LOG.debug("Domain escape was {}", e.getMessage());
        }
    }

    private void setBTStatus(boolean success, int reachedNd) {
        LOG.info("backtrack status: {}, reached NearDuplicate : {} ", success, reachedNd);
        crawlpath.setBacktrackSuccess(success);
        crawlpath.setReachedNearDup(reachedNd);
    }

    private boolean reachFromHome(StateVertex crawlTask) {
        // TODO: Changes to Eventables need to be accounted for

        ImmutableList<Eventable> path = null;
        try {
            path = shortestPathTo(crawlTask);
        } catch (Exception Ex) {
            LOG.info(crawlTask.getName() + " no path from "
                    + stateMachine.getCurrentState().getName());
            path = null;
        }

        List<StateVertex> alreadyTried = new ArrayList<>();
        if (path != null) {
            try {
                alreadyTried.add(stateMachine.getCurrentState());
                follow(CrawlPath.copyOf(path, crawlTask.getId()), crawlTask);
                if (!crawlTask.equals(stateMachine.getCurrentState())) {
                    // THis condition is reached when the reached state is an near-duplicate (updated therefore)
                    // Move the near-duplicate state to updatedStates in candidateActions cache
                    LOG.info(
                            "Tried reaching {} but Reached a near duplicate {}",
                            crawlTask.getName(),
                            stateMachine.getCurrentState().getName());
                    setBTStatus(false, stateMachine.getCurrentState().getId());
                    candidateActionCache.stateUpdated(crawlTask);
                } else {
                    LOG.debug("Reached the correct target {} ", crawlTask.getName());
                    setBTStatus(true, -1);
                }
                return true;
            } catch (Exception Ex) {
                LOG.info("Not a valid path anymore"
                        + stateMachine.getCurrentState().getName() + " : " + crawlTask.getName());
                LOG.info("Tried to follow ");

                printCrawlPath(path, true);
                LOG.info("Saving failed backtracking path to {}", crawlTask.getId());

                setBTStatus(false, -1);
                reset(crawlTask.getId());
            }
        }
        if (avoidDifferentBacktracking) {
            LOG.info("Avoiding different backtracking!! Not trying paths from other onURLSet");
            return false;
        }
        // TODO: Dealing with backtrack adds onurl state
        // Only use previously available set
        int size = stateMachine.getOnURLSet().size();
        for (int i = 0; i < size; i++) {
            StateVertex onURL = stateMachine.getOnURLSet().get(i);
            if (alreadyTried.contains(onURL)) {
                // Avoid multiple repetitions of the path from current state at the start of reachFromHome
                LOG.info("Avoid path from {} that is already tried", onURL.getName());
                continue;
            } else {
                alreadyTried.add(onURL);
            }
            StateVertex previous = stateMachine.getCurrentState();
            path = null;
            try {
                stateMachine.setCurrentState(onURL);
                path = shortestPathTo(crawlTask, onURL);
            } catch (Exception Ex) {
                LOG.info("{} no path from {}", crawlTask.getName(), onURL.getName());
                path = null;
            } finally {
                stateMachine.setCurrentState(previous);
            }
            if (path != null) {
                try {

                    ImmutableList<Eventable> followedPath =
                            ImmutableList.copyOf(follow(CrawlPath.copyOf(path, crawlTask.getId()), crawlTask));

                    LOG.info("Tried to follow v");
                    printCrawlPath(path, true);

                    printCrawlPath(crawlpath, true);
                    LOG.info("Followed path ^");

                    if (!crawlTask.equals(stateMachine.getCurrentState())) {
                        // THis condition is reached when the reached state is an near-duplicate (updated therefore)
                        // Move the near-duplicate state to updatedStates in candidateActions cache
                        LOG.info(
                                "Tried reaching {} but Reached a near duplicate {}",
                                crawlTask.getName(),
                                stateMachine.getCurrentState().getName());
                        candidateActionCache.stateUpdated(crawlTask);
                        setBTStatus(false, stateMachine.getCurrentState().getId());
                    } else {
                        LOG.debug("Reached the correct target {} ", crawlTask.getName());
                        setBTStatus(true, -1);
                    }
                    return true;
                } catch (Exception Ex) {
                    LOG.info("Not a valid path anymore {}: {}", onURL.getName(), crawlTask.getName());

                    LOG.info("Tried to follow ");
                    printCrawlPath(path, true);
                    LOG.info("Saving failed backtracking path to {}", crawlTask.getId());

                    setBTStatus(false, -1);
                    reset(crawlTask.getId());
                }
            }
        }
        return false;
    }

    private ImmutableList<Eventable> shortestPathTo(StateVertex crawlTask, StateVertex onURL) {
        StateFlowGraph graph = context.getSession().getStateFlowGraph();
        return graph.getShortestPath(onURL, crawlTask);
    }

    private ImmutableList<Eventable> shortestPathTo(StateVertex crawlTask) {
        StateFlowGraph graph = context.getSession().getStateFlowGraph();
        StateVertex currState = null;
        if (stateMachine != null) {
            currState = stateMachine.getCurrentState();
        }

        if (currState != null && currState != graph.getInitialState()) {
            LOG.info("Initial state not the same as current state : Getting shortest path from current state");
            return graph.getShortestPath(currState, crawlTask);
        }
        return graph.getShortestPath(graph.getInitialState(), crawlTask);
    }

    private ArrayList<Eventable> follow(CrawlPath path, StateVertex targetState) {
        StateVertex currState = null;
        if (stateMachine != null) {
            currState = stateMachine.getCurrentState();
        }
        if (currState == null) {
            currState = context.getSession().getInitialState();
        }
        if (currState instanceof HybridStateVertexImpl) {
            return followNew(path, targetState);
        } else {
            return followOld(path, targetState);
        }
    }

    private ArrayList<Eventable> followOld(CrawlPath path, StateVertex targetState) throws CrawljaxException {
        StateVertex currState = null;
        if (stateMachine != null) {
            currState = stateMachine.getCurrentState();
        }
        if (currState == null) {
            currState = context.getSession().getInitialState();
        }
        ArrayList<Eventable> returnList = new ArrayList<>();
        for (Eventable clickable : path) {
            checkCrawlConditions(targetState);
            LOG.debug("Backtracking by executing {} on element: {}", clickable.getEventType(), clickable);
            currState = changeState(targetState, clickable);

            List<FormInput> availableInputs = new ArrayList<>();

            if (!candidateActionCache.shouldDisableInput(clickable)) {
                if (candidateActionCache.getInput(clickable) != null) {
                    availableInputs = candidateActionCache.getInput(clickable);
                    LOG.info("Used a form input combination for backtracking {}", clickable.getId());
                } else {
                    LOG.info("No Input Combination Found for {}", clickable.getId());
                    availableInputs = getInputElements(clickable);
                }
            }

            formHandler.handleFormElements(availableInputs);

            tryToFireEvent(targetState, currState, clickable);
            checkCrawlConditions(targetState);
            returnList.add(clickable);
        }

        // This condition is probably unnecessary.
        if (!currState.equals(targetState)) {
            throw new StateUnreachableException(
                    targetState, "The path didn't result in the desired state but in state " + currState.getName());
        }
        return returnList;
    }

    private ArrayList<Eventable> followNew(CrawlPath path, StateVertex targetState) throws CrawljaxException {
        ArrayList<Eventable> followedPath = new ArrayList<>();
        boolean tookShorterPath = false;

        StateVertex currState = null;
        if (stateMachine != null) {
            LOG.info(
                    "Backtracking start from {}", stateMachine.getCurrentState().getName());
            currState = stateMachine.getCurrentState();
        }
        if (currState == null) {
            LOG.info(
                    "Backtracking Error!! starting from {}",
                    context.getSession().getInitialState().getName());

            currState = context.getSession().getInitialState();
        }
        for (Eventable clickable : path) {
            checkCrawlConditions(targetState);

            Eventable clone = (Eventable) clickable.clone();
            clone.setSource(currState);
            clone.setId(getBackTrackingEventableId());

            if (!clickable.getSourceStateVertex().equals(currState)) {

                if (avoidDifferentBacktracking
                        && fragmentManager.cacheStateComparision(clickable.getSourceStateVertex(), currState, true)
                                == StateComparision.DIFFERENT) {
                    // TODO: Check if this is called anywhere other than the first event of the path.
                    // Ideally this check should happen after event is fired for every other evernt in the path
                    LOG.info("Avoiding different backtracking -> {} {}", clickable.getSourceStateVertex(), currState);
                    throw new StateUnreachableException(
                            clickable.getSourceStateVertex(),
                            String.format(
                                    "The path didn't result in the desired %1$ but in %2$",
                                    clickable.getSourceStateVertex(), currState));
                }

                LOG.info("Backtracking exploration {}:  {} {}", clone.getId(), clone.getEventType(), clone);

                // TODO: formElements are added again. So empty existing elements.
                clone.setRelatedFormInputs(ImmutableList.copyOf(new ArrayList<>()));
                CandidateElement oldCandidate = clickable.getSourceStateVertex().getCandidateElement(clickable);

                CandidateElement newCandidate = currState.getCandidateElement(clone);

                if (newCandidate != null) {

                    LOG.info("Checking if old and new candiates are related");
                    LOG.info("old {}", oldCandidate);
                    LOG.info("new {}", newCandidate);

                    if (!fragmentManager.areRelated(
                            newCandidate.getClosestFragment(), oldCandidate.getClosestFragment())) {
                        LOG.info(
                                "{} {} and {} {} are unrelated",
                                newCandidate.getClosestFragment().getId(),
                                currState.getName(),
                                oldCandidate.getClosestFragment().getId(),
                                clickable.getSourceStateVertex().getName());
                        if (avoidUnrelatedBacktracking) {
                            LOG.info("Avoiding unrelated backtracking!!");
                            throw new StateUnreachableException(
                                    targetState, "Avoiding backtracking through unrelated fragments");
                        } else {
                            LOG.warn("Using unrelated Backtracking");
                        }
                    }

                    // TODO: set the element for the edge. Causes test failures otherwise.
                    clone.setElement(new Element(newCandidate.getElement()));
                    if (!newCandidate.isDirectAccess()) {
                        // TODO: mark event as fired in candidate cache.
                        LOG.info("BackTracking exploration candidate access {}", clone.getId());
                        candidateActionCache.removeAction(newCandidate, currState);
                        fragmentManager.recordAccess(newCandidate, currState);
                    } else {
                        LOG.info(
                                "Backtracking event {} explored already!! for equivalent candidate {}",
                                clone.getId(),
                                newCandidate.getIdentification().getValue());
                    }
                } else {
                    LOG.error(
                            "Could not find equivalent element in backtracking {} for candidate  {}",
                            clone.getId(),
                            clone.getIdentification().getValue());
                }

                fireEventWithInputs(clone);

            } else {
                List<FormInput> availableInputs = new ArrayList<>();

                if (!candidateActionCache.shouldDisableInput(clickable)) {
                    if (candidateActionCache.getInput(clickable) != null) {
                        //							formHandler.handleFormElements(candidateActionCache.getInput(clickable));
                        availableInputs = candidateActionCache.getInput(clickable);
                        LOG.info("Used a form input combination for backtracking {}", clickable.getId());
                    } else {
                        LOG.info("No Input Combination Found for {}", clickable.getId());
                        //							handleInputElements(clickable);
                        availableInputs = getInputElements(clone);
                    }
                }

                LOG.info("Backtracking event {} :  {} {}", clone.getId(), clone.getEventType(), clone);
                // TODO: If the form handling changed, and its a clone edge (removed) it will cause errors.
                if (!fireEventWithInputs(clone, availableInputs)) {
                    LOG.error("Backtracking event {} failed", clone.getId());
                    throw new StateUnreachableException(targetState, "couldn't fire eventable " + clone);
                } else {
                    LOG.info("Backtracking event {} successful", clone.getId());
                }
            }

            boolean stateUpdated = inspectNewState(clone);
            LOG.info("State is updated {}", stateUpdated);
            LOG.debug("new event {}", clone.getId());
            LOG.debug("Old event {}", clickable.getId());
            Eventable added = graphProvider
                    .get()
                    .getShortestPath(clone.getSourceStateVertex(), clone.getTargetStateVertex())
                    .get(0);
            LOG.info("Added to Path {}", added.getId());
            followedPath.add(added);

            LOG.info(
                    "Checking if event {} reached intended target {}",
                    added.getId(),
                    clickable.getTargetStateVertex().getName());
            if (!clickable.getTargetStateVertex().equals(stateMachine.getCurrentState())) {
                // DONE: change the path to accommodate this new state
                LOG.info(
                        "Backtracking state has changed from {} to {} ",
                        clickable.getTargetStateVertex().getName(),
                        stateMachine.getCurrentState());

                if (clickable.getSourceStateVertex().equals(currState)) {
                    // DONE: why am I removing the edge here??
                    // To avoid cycles ( when states are updated)
                    // DONE: Can I keep both current and expired edges? No!!
                    LOG.info(
                            " Removing expired Edge {} to {}",
                            clickable.getId(),
                            clickable.getTargetStateVertex().getName());
                    graphProvider.get().removeEdge(clickable);
                    // TODO: should I remove the vertex as well?
                    // TODO: APTED comparison may not be enough to remove the vertex. May be make sure before removing
                    // state.
                }

                // DONE: Are these two equivalent states?
                // What changed and how are they related.. What edge can we add between them? Added during inspect
                if (stateMachine.getCurrentState() instanceof HybridStateVertexImpl) {
                    StateVertex newState = stateMachine.getCurrentState();
                    StateVertex expectedState = clickable.getTargetStateVertex();
                    boolean assignDynamic = true;
                    StateComparision comp =
                            fragmentManager.cacheStateComparision(newState, expectedState, assignDynamic);

                    LOG.info(
                            "changed backtracking state {} and  {} are {}",
                            newState.getName(),
                            expectedState.getName(),
                            comp);
                    StateComparision prevComp =
                            fragmentManager.cacheStateComparision(clickable.getSourceStateVertex(), currState, true);
                    if (avoidDifferentBacktracking
                            && comp == StateComparision.DIFFERENT
                            && prevComp != StateComparision.DIFFERENT) {
                        // Purge the state and all incoming edges
                        candidateActionCache.purgeActionsForState(expectedState);
                        LOG.warn("Removing {} and all its connected edges from the graph", expectedState.getName());
                        graphProvider.get().removeState(expectedState);
                        throw new StateUnreachableException(
                                expectedState,
                                String.format(
                                        "The path didn't result in the desired %1$ but in %2$",
                                        expectedState, newState));
                    }
                }

                LOG.info(
                        "Finding shortest path from {} to {}",
                        stateMachine.getCurrentState().getName(),
                        targetState.getName());
                CrawlPath newPath = null;
                try {
                    newPath = CrawlPath.copyOf(
                            shortestPathTo(targetState, stateMachine.getCurrentState()), targetState.getId());
                } catch (Exception ex) {
                    LOG.info("No existing path between {} {}", stateMachine.getCurrentState(), targetState);
                }
                if (newPath != null) {
                    int newPathLength = newPath.size();
                    int oldPathLength = path.size() - followedPath.size();
                    if (newPathLength < oldPathLength) {
                        LOG.info(
                                "Found shorter path from chngwd bt state {} to {}",
                                stateMachine.getCurrentState().getName(),
                                targetState.getName());
                        printCrawlPath(newPath, true);
                        try {
                            ArrayList<Eventable> followedPathFrag = follow(newPath, targetState);
                            followedPath.addAll(followedPathFrag);
                        } catch (Exception ex) {
                            LOG.error("Shorter path resulted in error {}", ex.getMessage());
                            tookShorterPath = false;
                        }
                        currState = stateMachine.getCurrentState();
                        break;
                    }
                }

            } else {
                LOG.debug(
                        "Reached the intended backtracking state {} using {}",
                        stateMachine.getCurrentState().getName(),
                        clone.getId());
            }
            currState = stateMachine.getCurrentState();

            // Updated expired states, not needed during forward crawl (done in getNextAction)
            checkCrawlConditions(targetState);
        }

        LOG.info("Checking the result of Backtracking");
        if (!currState.equals(targetState)) {
            boolean assignDynamic = true;
            StateComparision comp = fragmentManager.cacheStateComparision(currState, targetState, assignDynamic);
            LOG.info("{} and {} are {}", currState.getName(), targetState.getName(), comp);
            switch (comp) {
                case DUPLICATE:
                case NEARDUPLICATE1:
                    LOG.info("This is a good path. Save it!!");
                case NEARDUPLICATE2:
                    LOG.info("Reached a near-duplicate state instead of target. State Updated?? ");
                    break;
                case DIFFERENT:
                case ERRORCOMPARING:
                default:
                    if (!tookShorterPath) {
                        LOG.info("The backtracking path can cause cycles. Avoiding next time!!");
                        //					avoidPath(crawlpath);
                    }
                    throw new StateUnreachableException(
                            targetState,
                            String.format(
                                    "The path didn't result in the desired %1$ but in %2$",
                                    targetState.getName(), currState.getName()));
            }
        }

        return followedPath;
    }

    private void checkCrawlConditions(StateVertex targetState) {
        if (!candidateExtractor.checkCrawlCondition()) {
            throw new StateUnreachableException(targetState, "Crawl conditions not complete. Not following path");
        }
    }

    private StateVertex changeState(StateVertex targetState, Eventable clickable) {
        boolean switched = stateMachine.changeState(clickable.getTargetStateVertex());
        if (!switched) {
            throw new StateUnreachableException(targetState, "Could not switch states");
        }
        StateVertex curState = clickable.getTargetStateVertex();
        crawlpath.add(clickable);
        return curState;
    }

    private void tryToFireEvent(StateVertex targetState, StateVertex curState, Eventable clickable) {
        browser.handlePopups();
        if (fireEvent(clickable, true)) {
            if (crawlerNotInScope()) {
                throw new StateUnreachableException(targetState, "Domain/scope left while following path");
            }
            int depth = crawlDepth.incrementAndGet();
            LOG.info("Crawl depth is now {}", depth);
            plugins.runOnRevisitStatePlugins(context, curState);

        } else {
            throw new StateUnreachableException(targetState, "couldn't fire eventable " + clickable);
        }
    }

    /**
     * Enters the form data. First, the related input elements (if any) to the eventable are filled in
     * and then it tries to fill in the remaining input elements.
     *
     * @param eventable the eventable element.
     * @return
     */
    private List<FormInput> handleInputElements(Eventable eventable) {
        List<FormInput> formInputs = getInputElements(eventable);
        return formHandler.handleFormElements(formInputs);
    }

    private List<FormInput> getInputElements(Eventable eventable) {
        ImmutableList<FormInput> formInputsExisting = eventable.getRelatedFormInputs();

        List<FormInput> formInputs = new ArrayList<>();
        formInputs.addAll(formInputsExisting);

        for (FormInput formInput : formHandler.getFormInputs()) {
            if (!formInputs.contains(formInput)) {
                formInputs.add(formInput);
            }
        }
        switch (crawlRules.getFormFillOrder()) {
            case NORMAL:
            case DOM:
                break;
            case VISUAL:
                formInputs = orderFormInputs(formInputs);
                break;
            default:
        }
        LOG.info(
                "Changing related inputs for {} with {}  to {} ",
                eventable.getId(),
                formInputsExisting.size(),
                formInputs.size());
        return formInputs;
    }

    /**
     * Computes visual order
     *
     * @param formInputs
     * @return
     */
    private List<FormInput> orderFormInputs(List<FormInput> formInputs) {
        if (stateMachine == null || !(stateMachine.getCurrentState() instanceof HybridStateVertexImpl)) {
            return formInputs;
        }

        LOG.info("Ordering form inputs visually");

        List<FormInput> returnList = new ArrayList<>();
        Map<Rectangle, FormInput> rectangleMap = new HashMap<>();

        for (FormInput input : formInputs) {
            try {
                Node inputNode = XPathHelper.getBelongingNode(
                        input, stateMachine.getCurrentState().getDocument());
                if (inputNode != null) {
                    Rectangle inputRectangle = VipsUtils.getRectangle(inputNode, browser.getWebDriver());
                    if (inputRectangle == null || inputRectangle.getX() < 0 || inputRectangle.getY() < 0) {
                        // Set a high value of x and y so that it is ordered last
                        inputRectangle = new Rectangle(10000, 10000, 1, 1);
                    }
                    rectangleMap.put(inputRectangle, input);
                }
            } catch (XPathExpressionException | IOException e) {
                LOG.warn(
                        "Error finding rectangle for formInput {}",
                        input.getIdentification().getValue());
                Rectangle inputRectangle = new Rectangle(10000, 10000, 1, 1);
                rectangleMap.put(inputRectangle, input);
            }
        }

        Comparator<Rectangle> visualOrder = (o1, o2) -> {
            if (o1.getY() < o2.getY()) {
                return -1;
            } else if (o1.getY() > o2.getY()) {
                return 1;
            } else {
                return Double.compare(o1.getX(), o2.getX());
            }
        };
        List<Rectangle> rectangles = new ArrayList<>(rectangleMap.keySet());
        rectangles.sort(visualOrder);

        for (Rectangle rect : rectangles) {
            returnList.add(rectangleMap.get(rect));
        }

        return returnList;
    }

    public boolean isValid(Eventable eventable) {
        //		String xpath = eventable.getIdentification().getValue();
        // TODO: check if old and new xpaths are the same.
        //		resolveByXpath(eventable, eventToFire)
        try {
            WebElement element = browser.getWebElement(eventable.getIdentification());

            if (element != null) {
                if (element.isDisplayed()) {
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            LOG.error("Eventable element is not valid: {}", e.getMessage());
        }

        return false;
    }

    private List<FormInput> getNextPair(Map<FormInput, Boolean> tries) {
        List<FormInput> returnList = new ArrayList<>();
        List<FormInput> available = new ArrayList<>();

        for (FormInput input : tries.keySet()) {
            if (!tries.get(input)) {
                available.add(input);
            }
        }

        if (available.size() <= 2) {
            for (FormInput input : available) {
                tries.replace(input, true);
            }
            return available;
        }

        Random rand = new Random();
        int rand1 = rand.nextInt(available.size());
        int rand2 = -1;
        if (rand1 + 1 == available.size()) {
            rand2 = rand1 - 1;
        } else {
            rand2 = rand1 + 1;
        }

        returnList.add(available.get(rand1));
        returnList.add(available.get(rand2));

        for (FormInput input : returnList) {
            tries.replace(input, true);
        }

        return returnList;
    }

    private List<FormInput> handleInputs_pairwise(Eventable event, List<FormInput> available) {

        boolean isValid = isValid(event);

        Map<FormInput, Boolean> tries = new HashMap<>();
        for (FormInput input : available) {
            tries.put(input, false);
        }

        int MAX_TRIES = 3;
        for (int i = 0; i < MAX_TRIES; i++) {
            List<FormInput> newTry = getNextPair(tries);
            if (newTry == null || newTry.isEmpty()) {
                return null;
            }

            List<FormInput> handled = formHandler.handleFormElements(newTry);

            boolean afterFormHandling = isValid(event);
            if (handled.get(handled.size() - 1).getType() != null || (isValid && !afterFormHandling)) {
                formHandler.resetFormInputs(available);
                LOG.info("Trying again!!");
            } else {
                return handled;
            }
        }

        return null;
    }

    public boolean fireEventWithInputs(Eventable event, List<FormInput> available) {
        boolean isValid = isValid(event);

        List<FormInput> handled = formHandler.handleFormElements(available);

        boolean afterFormHandling = isValid(event);

        if (!pairwiseFormHandling) {
            if (handled.get(handled.size() - 1).getType() == null) {
                handled.remove(handled.size() - 1);
            }
            candidateActionCache.mapInput(event, handled);
        } else {

            if (handled.get(handled.size() - 1).getType() != null || (isValid && !afterFormHandling)) {

                LOG.info("Normal input handling failed. Trying pairwise now");
                LOG.info("Resetting form Inputs first");
                formHandler.resetFormInputs(available);

                List<FormInput> worked = handleInputs_pairwise(event, available);
                if (worked != null) {
                    if (worked.get(worked.size() - 1).getType() == null) {
                        LOG.info("Combination worked. So saving it for backtracking");
                        worked.remove(worked.size() - 1);
                        candidateActionCache.mapInput(event, worked);
                    }
                } else {
                    candidateActionCache.disableInputsForPath(event);
                }
            } else {
                handled.remove(handled.size() - 1);
                candidateActionCache.mapInput(event, handled);
            }
        }

        //		waitForRefreshTagIfAny(event);
        return fireEvent(event, false);
    }

    public boolean fireEventWithInputs(Eventable event) {
        LOG.info(" Event xpath {}", event.getIdentification().getValue());

        List<FormInput> available = getInputElements(event);

        return fireEventWithInputs(event, available);
    }

    /**
     * Try to fire a given event on the Browser.
     *
     * @param eventable the eventable to fire
     * @return true iff the event is fired
     */
    private boolean fireEvent(Eventable eventable, boolean resolveXpath) {
        Eventable eventToFire = eventable;
        if (eventable.getIdentification().getHow().toString().equals("xpath")
                && eventable.getRelatedFrame().equals("")
                && resolveXpath) {
            eventToFire = resolveByXpath(eventable, eventToFire);
        }
        boolean isFired = false;
        try {
            isFired = browser.fireEventAndWait(eventToFire);
        } catch (ElementNotInteractableException | NoSuchElementException e) {
            if (crawlRules.isCrawlHiddenAnchors()
                    && eventToFire.getElement() != null
                    && "A".equals(eventToFire.getElement().getTag())) {
                isFired = visitAnchorHrefIfPossible(eventToFire);
            } else {
                LOG.debug("Ignoring invisible element {}", eventToFire.getElement());
            }
        } catch (InterruptedException e) {
            LOG.debug("Interrupted during fire event");
            interruptThread();
            return false;
        }

        LOG.debug("Event fired={} for eventable {}", isFired, eventable);

        if (isFired) {
            // Let the controller execute its specified wait operation on the browser thread safe.
            waitConditionChecker.wait(browser);
            browser.closeOtherWindows();
            return true;
        } else {
            /*
             * Execute the OnFireEventFailedPlugins with the current crawlPath with the crawlPath
             * removed 1 state to represent the path TO here.
             */
            plugins.runOnFireEventFailedPlugins(context, eventable, crawlpath.immutableCopyWithoutLast());
            return false; // no event fired
        }
    }

    private Eventable resolveByXpath(Eventable eventable, Eventable eventToFire) {
        // The path in the page to the 'clickable' (link, div, span, etc)
        String xpath = eventable.getIdentification().getValue();

        // The type of event to execute on the 'clickable' like onClick,
        // mouseOver, hover, etc
        EventType eventType = eventable.getEventType();

        // Try to find a 'better' / 'quicker' xpath
        String newXPath = new ElementResolver(eventable, browser).resolve();
        if (newXPath != null && !xpath.equals(newXPath)) {
            LOG.info("XPath changed from {} to {} relatedFrame: {}", xpath, newXPath, eventable.getRelatedFrame());
            eventToFire = new Eventable(new Identification(Identification.How.xpath, newXPath), eventType);
            // TODO: check if being used
            eventToFire.setId(getEventableId());
        }
        return eventToFire;
    }

    private boolean visitAnchorHrefIfPossible(Eventable eventable) {
        Element element = eventable.getElement();
        String href = element.getAttributeOrNull("href");
        if (href == null) {
            LOG.info("Anchor {} has no href and is invisible so it will be ignored", element);
        } else {
            LOG.info("Found an invisible link with href={}", href);
            URI url = UrlUtils.extractNewUrl(browser.getCurrentUrl(), href);
            browser.goToUrl(url);
            return true;
        }
        return false;
    }

    private void crawlThroughActionsOld() {
        boolean interrupted = Thread.interrupted();
        CandidateCrawlAction action = candidateActionCache.pollActionOrNull(stateMachine.getCurrentState());
        while (action != null && !interrupted) {
            boolean newStateFound = false;
            CandidateElement element = action.getCandidateElement();
            if (element.allConditionsSatisfied(browser)) {
                Eventable event = new Eventable(element, action.getEventType(), getEventableId());
                //				handleInputElements(event);
                waitForRefreshTagIfAny(event);

                boolean fired = fireEventWithInputs(event);
                if (fired) {
                    newStateFound = inspectNewState(event);
                }
            } else {
                LOG.info("Element {} not clicked because not all crawl conditions were satisfied", element);
            }

            if (newStateFound && stateMachine.getCurrentState().hasNearDuplicate()) {
                action = null;
            } else {
                // We have to check if we are still in the same state.
                action = candidateActionCache.pollActionOrNull(stateMachine.getCurrentState());
            }
            interrupted = Thread.interrupted();
            if (!interrupted && crawlerNotInScope()) {
                /*
                 * It's okay to have left the domain because the action didn't complete due to an
                 * interruption.
                 */
                throw new CrawlerLeftDomainException(browser.getCurrentUrl());
            }
        }
        if (interrupted) {
            LOG.info("Interrupted while firing actions. Putting back the actions on the todo list");
            if (action != null) {
                candidateActionCache.addActions(ImmutableList.of(action), stateMachine.getCurrentState());
            }
            interruptThread();
        }
    }

    private void crawlThroughActions() {
        if (stateMachine.getCurrentState() instanceof HybridStateVertexImpl) {
            crawlThroughActionsNew();
        } else {
            crawlThroughActionsOld();
        }
    }

    /**
     * Crawl through the actions of the current state. The browser keeps firing
     * {@link CandidateCrawlAction}s stored in the state until the DOM changes. When it does, it
     * checks if the new dom is a clone or a new state. In continues crawling in that new or clone
     * state. If the browser leaves the current domain, the crawler tries to get back to the previous
     * state.
     * <p>
     * The methods stops when {@link Thread#interrupted()}
     */
    private void crawlThroughActionsNew() {
        boolean afterBacktrack = true;
        boolean interrupted = Thread.interrupted();
        CandidateCrawlAction action =
                candidateActionCache.pollActionOrNull(stateMachine, context.getFragmentManager(), afterBacktrack);

        while (action != null && !interrupted) {
            boolean newStateFound = false;
            CandidateElement element = action.getCandidateElement();
            if (element.allConditionsSatisfied(browser)) {
                // set eventable id (based on access)
                long eventableId = getEventableId();
                if (element.wasExplored()) {
                    eventableId = (long) (element.getEquivalentAccess()) * DUPLICATE_EVENT_SEED + eventableId;
                    LOG.info(
                            "Duplicate access for {} \n seed {}",
                            element.getIdentification().getValue(),
                            eventableId);
                }
                Eventable event = new Eventable(element, action.getEventType(), eventableId);
                boolean fired = fireEventWithInputs(event);
                if (fired) {
                    try {
                        fragmentManager.recordAccess(action.getCandidateElement(), stateMachine.getCurrentState());
                    } catch (Exception ex) {
                        LOG.error("Could not record access to candidate : " + action.getCandidateElement());
                    }
                    StateVertex previous = stateMachine.getCurrentState();
                    newStateFound = inspectNewState(event);
                    StateVertex now = stateMachine.getCurrentState();
                } else {
                    LOG.info(
                            "Could not fire event. Putting back the actions on the todo list and disabling input next time");
                    LOG.info("Recording direct access to the action to avoid picking in the same state again");
                    element.setDirectAccess(true);
                    if (action != null) {
                        boolean added = candidateActionCache.disableInputsForAction(action);
                        if (added) {
                            List<CandidateCrawlAction> actions = new ArrayList<>();
                            actions.add(action);
                            candidateActionCache.addActions(actions, stateMachine.getCurrentState());
                        }
                    }
                }
            } else {
                LOG.info("Element {} not clicked because not all crawl conditions were satisfied", element);
            }

            afterBacktrack = false;
            action = candidateActionCache.pollActionOrNull(stateMachine, context.getFragmentManager(), afterBacktrack);
            //			}
            interrupted = Thread.interrupted();
            if (!interrupted && crawlerNotInScope()) {
                /*
                 * It's okay to have left the domain because the action didn't complete due to an
                 * interruption.
                 */
                throw new CrawlerLeftDomainException(browser.getCurrentUrl());
            }
        }
        if (interrupted) {
            LOG.info("Interrupted while firing actions. Putting back the actions on the todo list");
            if (action != null) {
                List<CandidateCrawlAction> actions = new ArrayList<>();
                actions.add(action);
                candidateActionCache.addActions(actions, stateMachine.getCurrentState());
            }
            interruptThread();
        }
    }

    private long getBackTrackingEventableId() {
        nextBackTrackingId += 1;
        return nextBackTrackingId;
    }

    private long getEventableId() {
        // TODO Auto-generated method stub
        nextEventableId = nextEventableId + 1;
        return nextEventableId;
    }

    private void interruptThread() {
        LOG.info("Crawler thread interrupted. Flushing crawlpaths");
        if (crawlpath != null) {
            printCrawlPath(crawlpath, true);
            LOG.info("Adding current crawlpath");
            context.getSession().addCrawlPath(crawlpath);
            FragmentationPlugin.writeCrawlPath(
                    context.getSession(),
                    crawlpath,
                    context.getSession().getCrawlPaths().size());
        }
        Thread.currentThread().interrupt();
    }

    boolean inspectNewState(Eventable event) {
        browser.handlePopups();
        if (crawlerNotInScope()) {
            LOG.debug("The browser left the domain/scope. Going back one state...");
            goBackOneState();
            return false;
        } else {
            StateVertex newState = stateMachine.newStateFor(browser);
            if (domChanged(event, newState)) {
                return inspectNewDom(event, newState);
            } else {
                LOG.debug("Dom unchanged");
                return false;
            }
        }
    }

    private boolean domChanged(final Eventable eventable, StateVertex newState) {
        //  DOM comparison behavior of StateVertex
        StateVertex stateBefore = stateMachine.getCurrentState();
        boolean isChanged = !newState.equals(stateBefore);

        if (isChanged) {
            LOG.debug("State is Changed!");
            return true;
        }

        LOG.debug("State not Changed!");
        return false;
    }

    private boolean inspectNewDom(Eventable event, StateVertex newState) {
        LOG.debug("The DOM has changed. Event added to the crawl path");
        if (event.getId() <= 0) {
            LOG.error("Adding Eventable to Crawlpath has id less than zero {}", event);
        }
        LOG.debug("Added eventable {}", event);
        crawlpath.add(event);
        boolean isNewState = stateMachine.switchToStateAndCheckIfClone(event, newState, context);
        if (isNewState) {
            int depth = crawlDepth.incrementAndGet();
            LOG.info("New DOM is a new state! crawl depth is now {}", depth);
            if (maxDepth == depth) {
                LOG.debug("Maximum depth achieved. Not crawling this state any further");
            } else {
                parseCurrentPageForCandidateElements();
            }
            return true;
        } else {
            if (event.getId() == -1) {
                // Removing the clone Edge and adding the SFG edge to the crawlpath
                LOG.info("Removing Clone Edge");
                Eventable removed = crawlpath.remove(crawlpath.size() - 1);
                boolean fixed = false;
                for (Eventable edge : stateMachine.getStateFlowGraph().getAllEdges()) {
                    if (edge.equals(event)) {
                        crawlpath.add(edge);
                        LOG.info("CrawlPath fixed !! The eventable fired was a clone of existing edge{}", edge.getId());
                        fixed = true;
                        break;
                    }
                }
                // TODO: Check if this is working properly. Some events are missing in crawlpaths if they are not fixed
                // in the above code
                if (!fixed) {
                    LOG.info(
                            "Crawlpath could not be fixed with graph. Using the removed eventable {}", removed.getId());
                    crawlpath.add(removed);
                }
            }
            LOG.debug("New DOM is a clone state. Continuing in that state.");
            candidateActionCache.rediscoveredState(stateMachine.getCurrentState());
            if (graphProvider.get().restoreState(stateMachine.getCurrentState())) {
                LOG.info("Restored expired {} and its incoming edges", stateMachine.getCurrentState());
            }
            return false;
        }
    }

    private void parseCurrentPageForCandidateElements() {
        StateVertex currentState = stateMachine.getCurrentState();
        LOG.info("Parsing DOM of state {} for candidate elements", currentState.getName());
        ImmutableList<CandidateElement> extract = candidateExtractor.extract(currentState);

        plugins.runPreStateCrawlingPlugins(context, extract, currentState);
        candidateActionCache.addActions(extract, currentState);
    }

    private void waitForRefreshTagIfAny(final Eventable eventable) {
        if ("meta".equalsIgnoreCase(eventable.getElement().getTag())) {
            Pattern p = Pattern.compile("(\\d+);\\s+URL=(.*)");
            for (Entry<String, String> e :
                    eventable.getElement().getAttributes().entrySet()) {
                Matcher m = p.matcher(e.getValue());
                long waitTime = parseWaitTimeOrReturnDefault(m);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    LOG.info("Crawler timed out while waiting for page to reload");
                    interruptThread();
                }
            }
        }
    }

    private boolean crawlerNotInScope() {
        return !crawlScope.isInScope(browser.getCurrentUrl());
    }

    private long parseWaitTimeOrReturnDefault(Matcher m) {
        long waitTime = TimeUnit.SECONDS.toMillis(10);
        if (m.find()) {
            LOG.debug("URL: {}", m.group(2));
            try {
                waitTime = Integer.parseInt(m.group(1)) * 1000L;
            } catch (NumberFormatException ex) {
                LOG.info(
                        "Could not parse the amount of time to wait for a META tag refresh. Waiting 10 seconds instead...");
            }
        }
        return waitTime;
    }

    private void goBackOneState() {
        CrawlPath currentPath = crawlpath.immutableCopy();
        printCrawlPath(currentPath, true);

        LOG.info("Going back one state to {}", stateMachine.getCurrentState().getId());
        reset(stateMachine.getCurrentState().getId());
        setBTStatus(true, -1);
    }

    /**
     * This method calls the index state. It should be called once per crawl in order to setup the
     * crawl.
     *
     * @return The initial state.
     */
    public StateVertex crawlIndex() {
        LOG.debug("Setting up vertex of the index page");

        if (basicAuthUrl != null) {
            browser.goToUrl(basicAuthUrl);
        }

        browser.goToUrl(url);

        // Run url first load plugin to clear the application state
        plugins.runOnUrlFirstLoadPlugins(context);

        plugins.runOnUrlLoadPlugins(context);
        StateVertex index = vertexFactory.createIndex(
                url.toString(), browser.getStrippedDom(), stateComparator.getStrippedDom(browser), browser);

        index.setOnURL(true);

        Preconditions.checkArgument(
                index.getId() == StateVertex.INDEX_ID, "It seems some the index state is crawled more than once.");

        plugins.runOnNewStatePlugins(context, index);

        LOG.debug("Parsing the index for candidate elements");
        ImmutableList<CandidateElement> extract = candidateExtractor.extract(index);

        plugins.runPreStateCrawlingPlugins(context, extract, index);

        candidateActionCache.addActions(extract, index);

        return index;
    }

    public CrawlerContext getContext() {
        return context;
    }

    public CrawlRules getCrawlRules() {
        return crawlRules;
    }

    public List<StateVertex> getOnUrlSet() {
        if (this.stateMachine != null) {
            return this.stateMachine.getOnURLSet();
        }
        return null;
    }
}

package com.crawljax.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.crawljax.stateabstractions.hybrid.VipsTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.condition.eventablecondition.EventableConditionChecker;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawlRules.FormFillOrder;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.configuration.InputSpecification;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.DefaultStateVertexFactory;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.crawljax.di.CoreModule.CandidateElementExtractorFactory;
import com.crawljax.di.CoreModule.FormHandlerFactory;
import com.crawljax.di.CoreModule.TrainingFormHandlerFactory;
import com.crawljax.di.CrawlSessionProvider;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.FormInput.InputType;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.fragmentation.FragmentationPlugin;
import com.crawljax.oraclecomparator.StateComparator;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexFactory;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;

@RunWith(MockitoJUnitRunner.class)

public class CrawlerProviderTest {

	@Mock
	private Provider<InMemoryStateFlowGraph> graphProvider;

	@Mock
	private Provider<StateFlowGraph> sfgProvider;

	@Mock
	private CrawlSession session;

	private UnfiredFragmentCandidates candidateActionCache;

	private InMemoryStateFlowGraph sfg;

	StateMachine sm;
	StateVertex index;

	private FormHandler formHandler;

	private CandidateElementExtractor newElementExtractor(CrawljaxConfiguration config, EmbeddedBrowser browser) {
		FormHandler formHandler = new FormHandler(browser, config.getCrawlRules());

		EventableConditionChecker eventableConditionChecker = new EventableConditionChecker(config.getCrawlRules());
		ConditionTypeChecker<CrawlCondition> crawlConditionChecker = new ConditionTypeChecker<>(
				config.getCrawlRules().getPreCrawlConfig().getCrawlConditions());
		ExtractorManager checker = new CandidateElementManager(eventableConditionChecker, crawlConditionChecker);

		return new CandidateElementExtractor(checker, browser, formHandler, config);
	}

	private InMemoryStateFlowGraph newStateFlowGraph() {
		InMemoryStateFlowGraph sfg = new InMemoryStateFlowGraph(new ExitNotifier(0), new DefaultStateVertexFactory());
		return sfg;
	}

	private void setStateMachineForConfig(CrawljaxConfiguration config, InMemoryStateFlowGraph sfg) {
		sm = new StateMachine(sfg, config.getCrawlRules().getInvariants(), new Plugins(config, new MetricRegistry()),
				null, new ArrayList<>());
	}

	public Crawler getCrawler(CrawljaxConfigurationBuilder configBuilder, EmbeddedBrowser browser) {
		CrawljaxRunner crawljax = new CrawljaxRunner(configBuilder.build());

		CrawljaxConfiguration config = configBuilder.build();

		CrawlSessionProvider sessionProvider = mock(CrawlSessionProvider.class);
		when(sessionProvider.get()).thenReturn(session);

		CrawlerContext context = new CrawlerContext(browser, config, sessionProvider, null, new MetricRegistry());
		StateComparator stateComparator = new StateComparator(config.getCrawlRules());

		formHandler = new FormHandler(browser, config.getCrawlRules());
		FormHandlerFactory formHandlerFactory = mock(FormHandlerFactory.class);
		when(formHandlerFactory.newFormHandler(browser)).thenReturn(formHandler);

		WaitConditionChecker waitConditionChecker = mock(WaitConditionChecker.class);

		CandidateElementExtractor elementExtractor = newElementExtractor(config, browser);
		CandidateElementExtractorFactory elementExtractorFactory = mock(CandidateElementExtractorFactory.class);
		when(elementExtractorFactory.newExtractor(browser)).thenReturn(elementExtractor);

		sfg = new InMemoryStateFlowGraph(new ExitNotifier(0), new HybridStateVertexFactory(0, configBuilder, false));

		when(graphProvider.get()).thenReturn(sfg);

		when(sfgProvider.get()).thenReturn(sfg);

		candidateActionCache = new UnfiredFragmentCandidates(config.getBrowserConfig(), sfgProvider,
				new MetricRegistry(), null);

		Plugins plugins = mock(Plugins.class);
		TrainingFormHandlerFactory trainingFormHandlerFactory = mock(TrainingFormHandlerFactory.class);

		Crawler crawler = new Crawler(context, config, stateComparator, candidateActionCache, formHandlerFactory,
				trainingFormHandlerFactory, waitConditionChecker, elementExtractorFactory, graphProvider, plugins,
				new HybridStateVertexFactory(0, configBuilder, false));

		return crawler;
	}

	@Ignore
	@Test
	public void test() {
		// String url =
		// "http://localhost:8888/addressbook/addressbook-mod/addressbook/index.php";
		String url = "http://localhost:4000";
		CrawljaxConfigurationBuilder configBuilder = CrawljaxConfiguration.builderFor(url);
		configBuilder.crawlRules().setFormFillMode(FormFillMode.RANDOM);
		configBuilder.crawlRules().setInputSpec(getInputSpec());
		configBuilder.crawlRules().setFormFillOrder(FormFillOrder.VISUAL);

		configBuilder.crawlRules().click("div").withAttribute("class", "board");
		// list
		configBuilder.crawlRules().click("div").withAttribute("class", "list add-new");
		// created boards and lists
		configBuilder.crawlRules().click("div").withAttribute("class", "list");
		// created card
		configBuilder.crawlRules().click("div").withAttribute("class", "card-content");

		configBuilder.setStateVertexFactory(new HybridStateVertexFactory(0.0, configBuilder, false));

		BrowserConfiguration browserConfiguration = new BrowserConfiguration(BrowserType.CHROME, 1,
				// new BrowserOptions(BrowserOptions.MACBOOK_PRO_RETINA_PIXEL_DENSITY));
				new BrowserOptions());
		configBuilder.setBrowserConfig(browserConfiguration);
		WebDriverBrowserBuilder builder = new WebDriverBrowserBuilder(configBuilder.build(), null);

		FragmentManager manager = new FragmentManager(null);

		// EmbeddedBrowser browser = null;
		EmbeddedBrowser browser = builder.get();

		browser.goToUrl(URI.create(url));

		// com.crawljax.vips_selenium.Test.login_addressbook(browser.getWebDriver());
		VipsTest.phoenix_login(browser.getWebDriver());

		Crawler crawler = getCrawler(configBuilder, browser);

		index = crawler.crawlIndex();

		BufferedImage screenshot = browser.getScreenShotAsBufferedImage(100);
		File screenshotFile = new File("testScreenshot.png");
		((HybridStateVertexImpl) index).fragmentDom(browser, screenshot, screenshotFile);
		sfg.putIndex(index);
		crawler.reset(0);
		sm = crawler.getContext().getStateMachine();

		int eventNum = 1;
		String xpath = "/html[1]/body[1]/main[1]/div[1]/div[1]/div[1]/div[1]/section[1]/div[1]/div[1]";
		performEvent(manager, browser, crawler, eventNum, xpath);

		// com.crawljax.vips_selenium.Test.phoenix_opencard(browser.getWebDriver());
		// com.crawljax.vips_selenium.Test.phoenix_addComment(browser.getWebDriver());

		eventNum = 2;
		xpath = "/html[1]/body[1]/main[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]";
		performEvent(manager, browser, crawler, eventNum, xpath);

		xpath = "/html[1]/body[1]/main[1]/div[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/form[1]/div[2]/button[1]";
		eventNum = 3;
		performEvent(manager, browser, crawler, eventNum, xpath);

		crawler.close();
		browser.close();

	}

	private void performEvent(FragmentManager manager, EmbeddedBrowser browser, Crawler crawler, int eventNum,
			String xpath) {

		List<CandidateElement> elements = sm.getCurrentState().getCandidateElements();
		Eventable event = null;
		CandidateElement element = null;

		element = getMatchingCandidate(elements, element, xpath);

		event = new Eventable(element, EventType.click, eventNum);

		// click board
		boolean fired = crawler.fireEventWithInputs(event);
		if (fired)
			recordEvent(manager, crawler, event, element);

		FragmentationPlugin.fragmentState(sm.getCurrentState(), manager, browser, new File("test"), false);
		// ((HybridStateVertexImpl)
		// sm.getCurrentState()).fragmentDom(browser.getWebDriver(), screenshot,
		// screenshotFile);
	}

	private CandidateElement getMatchingCandidate(List<CandidateElement> elements, CandidateElement element,
			String xpath) {
		for (CandidateElement elem : elements) {
			System.out.println(elem.getIdentification().getValue());

			if (elem.getIdentification().getValue().equalsIgnoreCase(xpath)) {
				element = elem;
			}
		}
		return element;
	}

	private void recordEvent(FragmentManager manager, Crawler crawler, Eventable event, CandidateElement element) {
		try {
			manager.recordAccess(element, sm.getCurrentState());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		StateVertex previous = sm.getCurrentState();
		boolean newStateFound = crawler.inspectNewState(event);
		StateVertex now = sm.getCurrentState();
		System.out.println(now);
	}

	private InputSpecification getInputSpec() {

		InputSpecification inputAddressBook = new InputSpecification();

		FormInput search = new FormInput(InputType.TEXT, new Identification(How.name, "searchstring"));
		search.inputValues("andrea");
		inputAddressBook.inputField(search);

		// FormInput selectBox = new FormInput(InputType.SELECT, new
		// Identification(How.name, "group" ));
		// selectBox.inputValues("all");
		// inputAddressBook.inputField(selectBox);

		return inputAddressBook;
	}

}

package com.crawljax.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.di.CoreModule.CandidateElementExtractorFactory;
import com.crawljax.di.CoreModule.FormHandlerFactory;
import com.crawljax.forms.FormHandler;
import com.crawljax.oraclecomparator.StateComparator;

/**
 * Test class for the Crawler testing.
 */
@RunWith(MockitoJUnitRunner.class)
public class CrawlerTest {

	private URL url;

	private NewCrawler crawler;

	@Mock
	private EmbeddedBrowser browser;

	@Spy
	private Plugins plugins = Plugins.noPlugins();

	private CrawlRules crawlRules;

	@Mock
	private Provider<CrawlSession> sessionProvider;

	@Mock
	private CrawlSession session;

	private StateComparator stateComparator;
	private UnfiredCandidateActions candidateActionCache;
	private FormHandler formHandler;
	private WaitConditionChecker waitConditionChecker;
	private CandidateElementExtractor extractor;

	@Before
	public void setup() throws MalformedURLException {
		CandidateElementExtractorFactory elementExtractor =
		        mock(CandidateElementExtractorFactory.class);
		when(elementExtractor.newExtractor(browser)).thenReturn(extractor);

		FormHandlerFactory formHandlerFactory = mock(FormHandlerFactory.class);
		when(formHandlerFactory.newFormHandler(browser)).thenReturn(formHandler);
		url = new URL("http://example.com");
		when(sessionProvider.get()).thenReturn(session);

		crawlRules = CrawljaxConfiguration.builderFor(url).build().getCrawlRules();
		crawler =
		        new NewCrawler(browser, url, plugins, crawlRules, sessionProvider,
		                stateComparator,
		                candidateActionCache, formHandlerFactory, waitConditionChecker,
		                elementExtractor);
	}

	@Test
	public void whenResetTheStateIsBackToIndex() {
		crawler.reset();
		verify(browser).goToUrl(url);
		verify(plugins).runOnUrlLoadPlugins(browser);
	}

}

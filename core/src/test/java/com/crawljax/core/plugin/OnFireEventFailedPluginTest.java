package com.crawljax.core.plugin;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import org.apache.html.dom.HTMLAnchorElementImpl;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Category(BrowserTest.class)
public class OnFireEventFailedPluginTest {

	private CrawljaxRunner controller;

	private final AtomicInteger hits = new AtomicInteger();

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("site");

	@Before
	public void setup() {

		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("crawler/index.html");
		builder.addPlugin((PreStateCrawlingPlugin) (session, candidateElements, state) -> {
			for (CandidateElement candidate : candidateElements) {
				HTMLAnchorElementImpl impl = (HTMLAnchorElementImpl) candidate.getElement();
				impl.setName("fail");
				impl.setId("eventually");
				impl.setHref("will");
				impl.setTextContent("This");
				candidate.getIdentification().setValue("/HTML[1]/BODY[1]/FAILED[1]/A[1]");
			}
		});
		builder.addPlugin(
				(OnFireEventFailedPlugin) (context, eventable, pathToFailure) -> hits
						.incrementAndGet());

		builder.setBrowserConfig(new BrowserConfiguration(BrowserProvider.getBrowserType()));

		controller = new CrawljaxRunner(builder.build());
	}

	@Test
	public void testFireEventFailedHasBeenExecuted() throws CrawljaxException {
		controller.call();
		assertThat("The FireEventFailed Plugin has been executed the correct amount of times",
				hits.get(), is(2));
	}

}

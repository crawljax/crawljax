package com.crawljax.core.plugin;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.html.dom.HTMLAnchorElementImpl;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import com.google.common.collect.ImmutableList;

@Category(BrowserTest.class)
public class OnFireEventFailedPluginTest {

	private CrawljaxRunner controller;

	private final AtomicInteger hits = new AtomicInteger();

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("site");

	@Before
	public void setup() {

		CrawljaxConfigurationBuilder builder = SERVER.newConfigBuilder("crawler/index.html");
		builder.addPlugin(new PreStateCrawlingPlugin() {

			@Override
			public void preStateCrawling(CrawlerContext session,
			        ImmutableList<CandidateElement> candidateElements, StateVertex state) {
				for (CandidateElement candidate : candidateElements) {
					HTMLAnchorElementImpl impl = (HTMLAnchorElementImpl) candidate.getElement();
					impl.setName("fail");
					impl.setId("eventually");
					impl.setHref("will");
					impl.setTextContent("This");
					candidate.getIdentification().setValue("/HTML[1]/BODY[1]/FAILED[1]/A[1]");
				}
			}
		});
		builder.addPlugin(new OnFireEventFailedPlugin() {
			@Override
			public void onFireEventFailed(CrawlerContext context, Eventable eventable,
			        List<Eventable> pathToFailure) {
				hits.incrementAndGet();
			}
		});

		controller = new CrawljaxRunner(builder.build());
	}

	@Test
	public void testFireEventFaildHasBeenExecuted() throws CrawljaxException {
		controller.call();
		assertThat("The FireEventFaild Plugin has been executed the correct amount of times",
		        hits.get(), is(2));
	}

}

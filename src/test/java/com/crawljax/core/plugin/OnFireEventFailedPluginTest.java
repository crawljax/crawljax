package com.crawljax.core.plugin;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Eventable;

/**
 * Test case for bug #8.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class OnFireEventFailedPluginTest {
	private static CrawljaxController controller;

	private static int hit;

	@BeforeClass
	public static void setup() throws ConfigurationException {

		CrawlSpecification spec =
		        new CrawlSpecification("file://"
		                + new File("src/test/site/crawler/index.html").getAbsolutePath());
		spec.clickDefaultElements();

		CrawljaxConfiguration config = new CrawljaxConfiguration();
		config.setCrawlSpecification(spec);
		config.addPlugin(new PreStateCrawlingPlugin() {

			@Override
			public void preStateCrawling(CrawlSession session,
			        List<CandidateElement> candidateElement) {
				for (CandidateElement candidate : candidateElement) {
					candidate.getIdentification().setValue("/HTML[1]/BODY[1]/FAILED[1]/A[1]");
				}
			}
		});
		config.addPlugin(new OnFireEventFailedPlugin() {
			@Override
			public void onFireEventFaild(Eventable eventable, List<Eventable> pathToFailure) {
				hit++;
			}
		});

		controller = new CrawljaxController(config);
	}

	@Test
	public void testFireEventFaildHasBeenExecuted() throws ConfigurationException,
	        CrawljaxException {
		controller.run();
		Assert.assertEquals(
		        "The FireEventFaild Plugin has been executed the correct amount of times", hit,
		        controller.getElementChecker().numberOfExaminedElements());
	}
}

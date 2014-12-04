package com.crawljax.core.largetests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;

import java.io.IOException;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.test.BrowserTest;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.slf4j.LoggerFactory;

@Category(BrowserTest.class)
public class LargeChromeTest extends LargeTestBase {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LargeChromeTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		assumeThat(System.getProperty("webdriver.chrome.driver") != null
		  || isOnClassPath(), is(true));
	}

	private static boolean isOnClassPath() throws IOException, InterruptedException {
		try {
			if (!System.getProperty("os.name").startsWith("Windows")) {
				Process exec = Runtime.getRuntime().exec("which chromedriver");
				boolean found = exec.waitFor() == 0;
				LOG.info("Found chrom on the classpath = {}", found);
				return found;
			}
			else {
				return false;
			}
		}
		catch (RuntimeException e) {
			LOG.info("Could not determine if chrome is on the classpath: {}", e.getMessage());
			return false;
		}
	}

	@Override
	BrowserConfiguration getBrowserConfiguration() {
		return new BrowserConfiguration(BrowserType.CHROME);
	}

	@Override
	long getTimeOutAfterReloadUrl() {
		return 100;
	}

	@Override
	long getTimeOutAfterEvent() {
		return 100;
	}
}

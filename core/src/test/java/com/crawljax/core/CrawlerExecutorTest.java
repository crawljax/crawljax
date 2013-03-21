package com.crawljax.core;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;

/**
 * Test the CrawlerExecutor ThreadPoolExecutor. Basically it test only the correct naming.
 */
public class CrawlerExecutorTest {

	/**
	 * Test for multi-thread setup.
	 * 
	 * @throws InterruptedException
	 *             the the waitForTermination fails.
	 * @throws ConfigurationException
	 *             when config fails
	 */
	@Test
	public void testCorrectNamesMultiThread() throws InterruptedException, ConfigurationException {
		CrawlerExecutor executor =
		        new CrawlerExecutor(new BrowserConfiguration(BrowserType.firefox, 2));
		TestThread t1 = new TestThread("Thread 1 Crawler 1", "");
		TestThread t2 = new TestThread("Thread 2 Crawler 2 (Automatic)", "Automatic");
		executor.execute(t1);
		executor.execute(t2);

		executor.waitForTermination();

		assertThat(t1.appointedName, is(t1.expetedName));
		assertThat(t2.appointedName, is(t2.expetedName));
	}

	/**
	 * Internal class used to 'emulate' a Crawler and do the actual unit-check.
	 */
	private static class TestThread implements Runnable {

		private final String expetedName;
		private String appointedName;
		private String toStringName;

		public TestThread(String expetedName, String toStringName) {
			this.expetedName = expetedName;
			this.toStringName = toStringName;

		}

		@Override
		public void run() {
			try {
				Thread.sleep(500);
				appointedName = Thread.currentThread().getName();
			} catch (InterruptedException | RuntimeException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return toStringName;
		}

	}
}

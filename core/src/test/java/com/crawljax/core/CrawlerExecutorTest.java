package com.crawljax.core;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;

/**
 * Test the CrawlerExecutor ThreadPoolExecutor. Basically it test only the correct naming.
 */
public class CrawlerExecutorTest {
	private CrawlerExecutor excutor = new CrawlerExecutor(1);

	/**
	 * Test for single thread setup.
	 * 
	 * @throws InterruptedException
	 *             the the waitForTermination fails.
	 */
	@Test
	public void testCorrectNamesSingleThread() throws InterruptedException {
		TestThread t1 = new TestThread("Thread 1 Crawler 1");
		TestThread t2 = new TestThread("Thread 1 Crawler 2 (Automatic)", "Automatic");

		// First to start
		excutor.execute(t1);

		excutor.execute(t2);

		excutor.waitForTermination();

		assertThat("Thread 1 ok", t1.success, is(true));
		assertThat("Thread 2 ok", t2.success, is(true));
	}

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
		CrawlSpecification spec = new CrawlSpecification("http://google.com");
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		cfg.setThreadConfiguration(new ThreadConfiguration(2));
		excutor = new CrawlerExecutor(2);
		TestThread t1 = new TestThread("Thread 1 Crawler 1");
		TestThread t2 = new TestThread("Thread 2 Crawler 2 (Automatic)", "Automatic");
		excutor.execute(t1);
		excutor.execute(t2);

		excutor.waitForTermination();

		assertThat("Thread 1 ok", t1.success, is(true));
		assertThat("Thread 2 ok", t2.success, is(true));
	}

	/**
	 * Internal class used to 'emulate' a Crawler and do the actual unit-check.
	 * 
	 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
	 */
	private static class TestThread implements Runnable {
		private final String compare;
		private final String name;
		private boolean success = false;

		public TestThread(String c, String n) {
			compare = c;
			name = n;
		}

		public TestThread(String string) {
			this(string, "");
		}

		@Override
		public void run() {
			try {
				Thread.sleep(500);
				String tName = Thread.currentThread().getName();
				if (tName.equals(compare)) {
					success = true;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}
}

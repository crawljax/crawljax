package com.crawljax.core;

import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.util.PropertyHelper;

/**
 * Test the CrawlerExecutor ThreadPoolExecutor. Basically it test only the correct naming.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class CrawlerExecutorTest {
	private CrawlerExecutor excutor = new CrawlerExecutor();

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
		TestThread t3 = new TestThread("Thread 1 Crawler 3");
		TestThread t4 = new TestThread("Thread 1 Crawler 4 (Automatic)", "Automatic");
		TestThread t5 = new TestThread("Thread 1 Crawler 5");
		TestThread t6 = new TestThread("Thread 1 Crawler 6 (Automatic)", "Automatic");
		TestThread t7 = new TestThread("Thread 1 Crawler 7");
		TestThread t8 = new TestThread("Thread 1 Crawler 8 (Automatic)", "Automatic");

		// First to start
		excutor.execute(t1);

		// Other in Stack way...
		excutor.execute(t8);
		excutor.execute(t7);
		excutor.execute(t6);
		excutor.execute(t5);
		excutor.execute(t4);
		excutor.execute(t3);
		excutor.execute(t2);

		excutor.waitForTermination();

		Assert.assertTrue("Thread 1 ok", t1.success);
		Assert.assertTrue("Thread 2 ok", t2.success);
		Assert.assertTrue("Thread 3 ok", t3.success);
		Assert.assertTrue("Thread 4 ok", t4.success);
		Assert.assertTrue("Thread 5 ok", t5.success);
		Assert.assertTrue("Thread 6 ok", t6.success);
		Assert.assertTrue("Thread 7 ok", t7.success);
		Assert.assertTrue("Thread 8 ok", t8.success);
	}

	/**
	 * Test for multi-thread setup.
	 * 
	 * @throws InterruptedException
	 *             the the waitForTermination fails.
	 */
	@Test
	public void testCorrectNamesMultiThread() throws InterruptedException, ConfigurationException {
		CrawlSpecification spec = new CrawlSpecification("about:plugins");
		spec.setNumberOfThreads(2);
		CrawljaxConfiguration cfg = new CrawljaxConfiguration();
		cfg.setCrawlSpecification(spec);
		PropertyHelper.init(cfg);

		excutor = new CrawlerExecutor();
		TestThread t1 = new TestThread("Thread 1 Crawler 1");
		TestThread t2 = new TestThread("Thread 2 Crawler 2 (Automatic)", "Automatic");
		excutor.execute(t1);
		excutor.execute(t2);

		excutor.waitForTermination();

		Assert.assertTrue("Thread 1 ok", t1.success);
		Assert.assertTrue("Thread 2 ok", t2.success);
	}

	/**
	 * Internal class used to 'emulate' a Crawler and do the actual unit-check.
	 * 
	 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
	 */
	private class TestThread implements Runnable {
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
				Thread.sleep(100);
				String tName = Thread.currentThread().getName();
				if (tName.equals(compare)) {
					success = true;
				} else {
					System.out.println("Boe!: " + tName + "!=" + compare);
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

package com.crawljax.core;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;

/**
 * Original: Test the CrawlerExecutor ThreadPoolExecutor. Basically it test only the correct naming.</br></br>
 * 
 * Major Overhaul on March 24, 2013 by Jae-Hwan Jung <jaehwan.jeff.jung@gmail.com>: </br>
 * Testing for correct naming does not work properly as scheduling of threads is done by the host OS.
 * As a result, the order of statements may not be the same as the order of actual execution due to the loads
 * of cores at time. 
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
		
		/**
		 * Since the default number of threads in the pool is 10, the first 10 runnables will be
		 * assigned to the threads. Then the remaining threads will be inserted into the queue.
		 * Then when the first 10 threads are done working, the queue will be popped in FILO order.
		 */
		TestThread t1 = new TestThread("Thread 1 Crawler 1", "");
		TestThread t2 = new TestThread("Thread 2 Crawler 2", "");
		TestThread t3 = new TestThread("Thread 3 Crawler 3", "");
		TestThread t4 = new TestThread("Thread 4 Crawler 4", "");
		TestThread t5 = new TestThread("Thread 5 Crawler 5", "");
		TestThread t6 = new TestThread("Thread 6 Crawler 6", "");
		TestThread t7 = new TestThread("Thread 7 Crawler 7", "");
		TestThread t8 = new TestThread("Thread 8 Crawler 8", "");
		TestThread t9 = new TestThread("Thread 9 Crawler 9", "");
		TestThread t10 = new TestThread("Thread 10 Crawler 10", "");
		/**
		 * As mentioned above, these are the runnables in the queue and will be popped in FILO order.
		 * Therefore the strings passed are different from above.
		 */		
		TestThread t11 = new TestThread("Thread 6 Crawler 16", "");
		TestThread t12 = new TestThread("Thread 5 Crawler 15", "");
		TestThread t13 = new TestThread("Thread 4 Crawler 14", "");
		TestThread t14 = new TestThread("Thread 3 Crawler 13", "");
		TestThread t15 = new TestThread("Thread 2 Crawler 12", "");
		TestThread t16 = new TestThread("Thread 1 Crawler 11", "");
		
		
		
		/**
		 * Slight delay between ThreadPoolExecutor.execute() is needed for this test to function correctly.
		 * Depending on how the OS assigns JAVA threads to cores (or OS threads) and the load of the cores
		 * at the moment, the speed of execution of the methods could differ from the actual order of 
		 * calling them. As a result, sometimes this test fails when executor.execute(t2) is actually executed
		 * before executor.execute(t1).
		 * There are only two ways to fix this. First is to put a small delay (as Thread.sleep(SLEEPTIME)) between
		 * method calling. Or assign only one core to JAVA SE process on Windows Task Manager (or equivalent
		 * features of other OS).
		 */
		final int SLEEPTIME = 10;
		executor.execute(t1);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t2);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t3);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t4);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t5);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t6);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t7);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t8);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t9);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t10);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t11);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t12);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t13);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t14);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t15);
		Thread.sleep(SLEEPTIME);		
		executor.execute(t16);

		executor.waitForTermination();

		assertThat(t1.appointedName, is(t1.expetedName));
		assertThat(t2.appointedName, is(t2.expetedName));
		assertThat(t3.appointedName, is(t3.expetedName));
		assertThat(t4.appointedName, is(t4.expetedName));
		assertThat(t5.appointedName, is(t5.expetedName));
		assertThat(t6.appointedName, is(t6.expetedName));
		assertThat(t7.appointedName, is(t7.expetedName));
		assertThat(t8.appointedName, is(t8.expetedName));
		assertThat(t9.appointedName, is(t9.expetedName));
		assertThat(t10.appointedName, is(t10.expetedName));
		assertThat(t11.appointedName, is(t11.expetedName));
		assertThat(t12.appointedName, is(t12.expetedName));
		assertThat(t13.appointedName, is(t13.expetedName));
		assertThat(t14.appointedName, is(t14.expetedName));
		assertThat(t15.appointedName, is(t15.expetedName));
		assertThat(t16.appointedName, is(t16.expetedName));
				
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
				appointedName = Thread.currentThread().getName();
				Thread.sleep(1000);				
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

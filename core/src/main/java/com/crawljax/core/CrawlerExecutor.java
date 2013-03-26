package com.crawljax.core;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.BrowserConfiguration;

/**
 * Main Executor inheriting {@link ThreadPoolExecutor} to implement the beforeExecute, afterExecute
 * and terminated. Every time a new Crawler is added to this Executor by its
 * {@link #execute(Runnable)} function a new Thread is created when there are no more free threads.
 * If there is a free (old) unused thread left the Crawler will be loaded into that Thread. So
 * Threads will be reused, at most numberOfThreads will be created. The number of Crawlers active at
 * the same time will be the maximum of the number of Threads. If there are no more Threads left,
 * Crawlers will be stored in a workQueue until a Thread will become available.
 */
public class CrawlerExecutor extends ThreadPoolExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerExecutor.class);
	/**
	 * Counter for the number of Crawlers that in total have run. Every time a new Crawler beginning
	 * executing this counter is incremented.
	 */
	private final AtomicInteger crawlerCount = new AtomicInteger();

	/**
	 * The total amount of currently executing (running) tasks (Crawlers).
	 */
	private final AtomicInteger runningTasks = new AtomicInteger();

	/**
	 * In this map the combination Thread-id is stored. Ever time a new thread is created by the
	 * ThreadFactory a id is incremented so first Thread has id 1 till the number of defined
	 * threads.
	 */
	private final ConcurrentHashMap<Thread, Integer> threadIdMap =
	        new ConcurrentHashMap<Thread, Integer>();

	private boolean aborted = false;

	/**
	 * Default CrawlerExecutor using the configured number of threads, no timeout for idle threads.
	 * Custom FILO workQueue to support Depth-first crawling and the local ThreadFactory. It will have a minimum
	 * pool size of the #browsers * 5, and a maximum of #browsers * 5, thereby making it a fixed-size pool.
	 * The default bounded CrawlQueue of size 100. Also, by setting ThreadPoolExecutor.CallerRunsPolicy() the 
	 * thread that invokes execute itself runs the task if the queue is full and the max number of threads are 
	 * running already, thereby slowing down the rate the new tasks are submitted. Also, since this is a fixed-
	 * size pool, idle threads will not be removed but they are suspended and won't affect the system much.
	 */
	public CrawlerExecutor(BrowserConfiguration config) {
		super(config.getNumberOfBrowsers() * 5, config.getNumberOfBrowsers() * 5, 0L,
		        TimeUnit.MILLISECONDS, new CrawlQueue(1000), new ThreadPoolExecutor.CallerRunsPolicy());
		setThreadFactory(new CrawlerThreadFactory());
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (t != null) {
			LOGGER.error("Finished unsuccesfully", t);
		}
		LOGGER.debug("Finished executing");
		if (runningTasks.decrementAndGet() == 0 && getQueue().isEmpty()) {
			/**
			 * As the number of running tasks equals 0 there are no tasks running anymore. A new
			 * task can only be added by a Crawler which happens before the run function exits so if
			 * the counter hits 0 there is no more work to do so calling the shutdown method. which
			 * will lead to a call to terminated which notifies the waitForTermination function.
			 */
			LOGGER.info("All Crawlers finished executing, now shutting down");
			this.shutdown();
		}
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		runningTasks.incrementAndGet();

		/**
		 * For logging purposes, set the correct the name of the Thread-Crawler combination.
		 */
		String crawlerName = r.toString();

		if (!crawlerName.equals("")) {
			// This is a custom Crawler us the toString method to determine the name.
			crawlerName = " (" + crawlerName + ")";
		}

		// Example name: "Thread 5 Crawler 2 (Guided)"
		String threadName =
		        "Thread " + threadIdMap.get(t) + " Crawler " + crawlerCount.incrementAndGet()
		                + crawlerName;

		t.setName(threadName);
		LOGGER.info("Starting new Crawler: " + threadName);
	}

	@Override
	protected void terminated() {
		super.terminated();
		synchronized (this) {
			// Send a notify to the wait function in waitForTermination
			notifyAll();
		}
	}

	/**
	 * This function blocks until all tasks has been executed.
	 * 
	 * @throws InterruptedException
	 *             when this thread is interrupted.
	 */
	public void waitForTermination() throws InterruptedException {
		synchronized (this) {
			// Block until terminated is called
			wait();
		}
		LOGGER.info("CrawlerExecutor terminated");
	}

	/**
	 * The most simple implementation of a ThreadFactory, but with a counter and safe for the number
	 * of threads created. The results are stored in the {@link CrawlerExecutor#threadIdMap}.
	 * 
	 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
	 */
	private class CrawlerThreadFactory implements ThreadFactory {
		/**
		 * This counter is incremented every time a new thread is created.
		 */
		private final AtomicInteger threadCount = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			threadIdMap.put(t, threadCount.incrementAndGet());
			LOGGER.debug("Created new Thread");
			return t;
		}
	}

	/**
	 * shutdown the CrawlerExecutor, depending on the argument issue a abort or normal shutdown.
	 * 
	 * @param isAbort
	 *            if set to true indicating this is abort instead of normal shutdown.
	 * @return a list of runnables as where in the system on time of the shutdown.
	 */
	public List<Runnable> shutdownNow(boolean isAbort) {
		this.aborted = isAbort;
		return super.shutdownNow();
	}

	/**
	 * @return true if the shutdown was issues as abort false otherwise.
	 */
	public boolean isAborted() {
		return aborted;
	}
}

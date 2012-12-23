package com.crawljax.core;

/**
 * This interface defines operations that can be used to add work to the queue and remove work from
 * the crawlqueue.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public interface CrawlQueueManager {
	/**
	 * Add work (Crawler) to the Queue of work that need to be done. The class is thread-safe.
	 * 
	 * @param work
	 *            the work (Crawler) to add to the Queue
	 */
	void addWorkToQueue(Crawler work);

	/**
	 * Removes this Crawler from the workQueue if it is present, thus causing it not to be run if it
	 * has not already started.
	 * 
	 * @param crawler
	 *            the Crawler to remove
	 * @return true if the crawler was removed successfully
	 */
	boolean removeWorkFromQueue(Crawler crawler);

	/**
	 * Block (Wait) until all the work for this queue has been finished.
	 * 
	 * @throws InterruptedException
	 *             when the blocking thread gets interupted.
	 */
	void waitForTermination() throws InterruptedException;

	/**
	 * Terminate this CrawlQueue, depending on the parameter issue a abort or a 'normal' terminate.
	 * A 'normal' terminate happens when the MaximumRunTime() or the MaxNumberOfStates is reached
	 * while a abort can be issues when an exception is catched and handled.
	 * 
	 * @param isAbort
	 *            true for abort, false for normal shutdown.
	 */
	void terminate(boolean isAbort);
}

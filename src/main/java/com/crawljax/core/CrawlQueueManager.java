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
}

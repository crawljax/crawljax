package com.crawljax.core;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;

/**
 * The factory to use create new Thread for Crawlers.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
@ThreadSafe
public class CrawlThreadFactory implements ThreadFactory {
	private static final Logger LOGGER = Logger.getLogger(CrawlThreadFactory.class);
	private final AtomicInteger threadCount = new AtomicInteger();

	@Override
	public Thread newThread(Runnable r) {
		int id = threadCount.incrementAndGet();

		/**
		 * This is a bit nasty code but it works...
		 */
		Object name = new String("");
		try {
			Field[] fields = r.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.getName().startsWith("firstTask")) {
					field.setAccessible(true);
					name = field.get(r);
					break;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Can not determine name of the Crawler for creating a new Thread", e);
		}
		String crawlerName = "Crawler " + id;
		if (!name.toString().trim().equals("")) {
			crawlerName += " (" + name.toString() + ")";
		}
		return new Thread(r, crawlerName);
	}

}

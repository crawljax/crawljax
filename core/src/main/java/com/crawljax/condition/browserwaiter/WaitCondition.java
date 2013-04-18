package com.crawljax.condition.browserwaiter;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;

/**
 * Defines a wait condition for an url. A waitcondition has a list of expected conditions which
 * should all be satisfied for the specified url.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 */
@ThreadSafe
public class WaitCondition {
	private static final Logger LOGGER = LoggerFactory.getLogger(WaitCondition.class.getName());

	private static final int WAITTIME = 500;

	private final String url;
	private final List<ExpectedCondition> expectedConditions = new ArrayList<ExpectedCondition>();
	private final int timeOut;
	private final int pollingTime = 100;

	/**
	 * @param url
	 *            Which url to use.
	 * @param expectedConditions
	 *            Conditions expected.
	 */
	public WaitCondition(String url, ExpectedCondition... expectedConditions) {
		this(url, WAITTIME, expectedConditions);
	}

	/**
	 * @param url
	 *            Which url to use.
	 * @param timeoutMilliseconds
	 *            Time to wait in miliseconds.
	 * @param expectedConditions
	 *            Conditions expected.
	 */
	public WaitCondition(String url, int timeoutMilliseconds,
	        ExpectedCondition... expectedConditions) {
		this.url = url;
		this.timeOut = timeoutMilliseconds;
		for (ExpectedCondition condition : expectedConditions) {
			this.expectedConditions.add(condition);

		}
	}

	/**
	 * @param url
	 *            Which url to use.
	 * @param timeoutMilliseconds
	 *            Time to wait in miliseconds.
	 * @param expectedConditions
	 *            Conditions expected.
	 */
	public WaitCondition(String url, int timeoutMilliseconds,
	        List<ExpectedCondition> expectedConditions) {
		this.url = url;
		this.timeOut = timeoutMilliseconds;
		this.expectedConditions.addAll(expectedConditions);
	}

	/**
	 * Tests all the conditions and waits for them to be satisfied or times out.
	 * 
	 * @param browser
	 *            The browser to use.
	 * @return -1 if browser does not match url. 0 by timeout. 1 if all conditions are satisfied
	 */
	public int testAndWait(EmbeddedBrowser browser) {
		if (expectedConditions.isEmpty()) {
			// No ExpectedConditions return successful wait.
			return 1;
		}
		if (!browser.getCurrentUrl().toLowerCase().contains(this.url.toLowerCase())) {
			return -1;
		}
		ExpectedCondition lastCheckCondition = null;
		List<ExpectedCondition> toCheckwaitConditions = new ArrayList<ExpectedCondition>();
		toCheckwaitConditions.addAll(expectedConditions);
		long currentTime = System.currentTimeMillis();
		long maxTime = currentTime + this.timeOut;
		long repeatTime = this.pollingTime;
		LOGGER.info("Waiting for " + toCheckwaitConditions.size() + " conditions");
		int index = 0;
		while (index < toCheckwaitConditions.size() && currentTime <= maxTime) {
			ExpectedCondition checkCondition = toCheckwaitConditions.get(index);
			lastCheckCondition = checkCondition;
			LOGGER.debug("Waiting for: " + checkCondition);
			if (checkCondition.isSatisfied(browser)) {
				index++;
			} else {
				try {
					Thread.sleep(repeatTime);
				} catch (InterruptedException e) {
					LOGGER.warn("Intterupted while waiting in condition");
					Thread.currentThread().interrupt();
				}
			}
			currentTime = System.currentTimeMillis();
		}
		if (currentTime >= maxTime) {
			LOGGER.info("TIMEOUT WaitCondition url " + getUrl() + "; Timout while waiting for "
			        + lastCheckCondition);
			return 0;
		} else {
			return 1;
		}

	}

	/**
	 * @return the url
	 */
	protected String getUrl() {
		return url;
	}

}

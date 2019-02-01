package com.crawljax.condition.browserwaiter;

import com.crawljax.browser.EmbeddedBrowser;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a wait condition for an url. A WaitCondition has a list of expected conditions which
 * should all be satisfied for the specified url.
 *
 * @author dannyroest@gmail.com (Danny Roest)
 */
@ThreadSafe
public class WaitCondition {
	private static final Logger LOGGER = LoggerFactory.getLogger(WaitCondition.class.getName());

	private static final int WAIT_TIME = 500;
	private static final long POLLING_TIME = 100;

	private final String url;
	private final List<ExpectedCondition> expectedConditions = new ArrayList<>();
	private final int timeOut;

	/**
	 * @param url                Which url to use.
	 * @param expectedConditions Conditions expected.
	 */
	public WaitCondition(String url, ExpectedCondition... expectedConditions) {
		this(url, WAIT_TIME, expectedConditions);
	}

	/**
	 * @param url                 Which url to use.
	 * @param timeoutMilliseconds Time to wait in milliseconds.
	 * @param expectedConditions  Conditions expected.
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
	 * @param url                 Which url to use.
	 * @param timeoutMilliseconds Time to wait in milliseconds.
	 * @param expectedConditions  Conditions expected.
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
	 * @param browser The browser to use.
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
		List<ExpectedCondition> toCheckWaitConditions = new ArrayList<>(expectedConditions);
		long currentTime = System.currentTimeMillis();
		long maxTime = currentTime + this.timeOut;
		LOGGER.info("Waiting for " + toCheckWaitConditions.size() + " conditions");
		int index = 0;
		while (index < toCheckWaitConditions.size() && currentTime <= maxTime) {
			ExpectedCondition checkCondition = toCheckWaitConditions.get(index);
			lastCheckCondition = checkCondition;
			LOGGER.debug("Waiting for: " + checkCondition);
			if (checkCondition.isSatisfied(browser)) {
				index++;
			} else {
				try {
					Thread.sleep(POLLING_TIME);
				} catch (InterruptedException e) {
					LOGGER.warn("Interrupted while waiting in condition");
					Thread.currentThread().interrupt();
				}
			}
			currentTime = System.currentTimeMillis();
		}
		if (currentTime >= maxTime) {
			LOGGER.info("TIMEOUT WaitCondition url " + getUrl() + "; Timeout while waiting for "
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

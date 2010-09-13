package com.crawljax.condition.browserwaiter;

import com.crawljax.browser.DummyBrowser;
import com.crawljax.browser.EmbeddedBrowser;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;

/**
 * This test case tests the WaitCondition class. Issue #30 was covered by this test case.
 * 
 * @author slenselink@google.com (Stefan Lenselink)
 * @version $Id$
 */
public class WaitConditionTest {

	@Test
	public void testWaitConditionNoIndexOutOfBounceAfterTwoTries() {
		WaitCondition wc = new WaitCondition("tmp", 2000, new TimeoutExpectedCondition());
		Assert.assertEquals("Wait timed out", 0, wc.testAndWait(new DummyBrowser("tmp")));
	}

	@Test
	public void testWaitConditionSuccessZeroSpecified() {
		WaitCondition wc = new WaitCondition("tmp", 2000, new ArrayList<ExpectedCondition>());
		Assert.assertEquals("Wait success", 1, wc.testAndWait(new DummyBrowser("tmp")));
	}

	@Test
	public void testWaitConditionSuccessZeroSpecifiedZeroTimeout() {
		WaitCondition wc = new WaitCondition("tmp", 0, new ArrayList<ExpectedCondition>());
		Assert.assertEquals("Wait success", 1, wc.testAndWait(new DummyBrowser("tmp")));
	}

	@Test
	public void testWaitConditionNoIndexOutOfBounceAfterFirstTry() {
		WaitCondition wc = new WaitCondition("tmp", 2000, new ExpectedCondition() {
			@Override
			public boolean isSatisfied(EmbeddedBrowser browser) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					Assert.fail(e.getMessage());
					e.printStackTrace();
				}
				return true;
			}
		});
		Assert.assertEquals("Wait timed out", 0, wc.testAndWait(new DummyBrowser("tmp")));
	}

	@Test
	public void testWaitConditionNotRunBecauseUrl() {
		WaitCondition wc = new WaitCondition("tmp/foo", 2000, new TimeoutExpectedCondition());
		Assert.assertEquals("Wait not run because browser url missmatch", -1,
		        wc.testAndWait(new DummyBrowser("tmp")));
	}

	@Test
	public void testWaitConditionSuccessfulRun() {
		WaitCondition wc = new WaitCondition("tmp", 2000, new ExpectedCondition() {
			@Override
			public boolean isSatisfied(EmbeddedBrowser browser) {
				return true;
			}
		});
		Assert.assertEquals("Wait succeded", 1, wc.testAndWait(new DummyBrowser("tmp")));
	}

	@Test
	public void testWaitConditionTimeoutRun() {
		WaitCondition wc = new WaitCondition("tmp", 0, new ExpectedCondition() {
			@Override
			public boolean isSatisfied(EmbeddedBrowser browser) {
				return true;
			}
		});
		Assert.assertEquals("Wait succeded", 0, wc.testAndWait(new DummyBrowser("tmp")));
	}

	/**
	 * Internal mock-class to represent a long-time WaitCondition exceeding the timeout limit.
	 * 
	 * @author slenselink@google.com (Stefan Lenselink)
	 */
	private static class TimeoutExpectedCondition implements ExpectedCondition {
		private int count = 0;
		@Override
		public boolean isSatisfied(EmbeddedBrowser browser) {
			count++;
			try {
				// Sleep for 1.5 sec so the first time the limit is not reached, the second time it
				// will and true will be returned.
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				Assert.fail(e.getMessage());
				e.printStackTrace();
			}
			return count > 1;
		}

	}
}

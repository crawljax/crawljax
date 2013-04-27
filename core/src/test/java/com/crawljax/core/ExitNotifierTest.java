package com.crawljax.core;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.crawljax.core.ExitNotifier.ExitStatus;

public class ExitNotifierTest {

	private ExitNotifier notifier;

	@Test(timeout = 2000)
	public void whenMaximumStatesReachedItExists() throws InterruptedException {
		notifier = new ExitNotifier(2);
		notifier.incrementNumberOfStates();
		notifier.incrementNumberOfStates();
		ExitStatus reason = notifier.awaitTermination();
		assertThat(reason, is(ExitStatus.MAX_STATES));

	}

	@Test(timeout = 2000)
	public void whenNoStateLimitItDoesntTerminate() throws InterruptedException {
		notifier = new ExitNotifier(0);
		notifier.incrementNumberOfStates();
		notifier.incrementNumberOfStates();
		assertThat(notifier.isExitCalled(), is(false));
	}

}

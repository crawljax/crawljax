package com.crawljax.plugins.testcasegenerator.report;

import com.crawljax.core.state.Eventable;

public class EventableResult {
	private Eventable eventable;
	private boolean success;

	public EventableResult(Eventable eventable) {
		this.eventable = eventable;
		this.success = true;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}

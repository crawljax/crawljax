package com.crawljax.plugins.testcasegenerator.report;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;

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

	public Eventable getEventable() {
		return eventable;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setEventable(Eventable eventable) {
		this.eventable = eventable;
	}
}

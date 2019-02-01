package com.crawljax.plugins.testcasegenerator.report;

import java.util.List;

import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.state.StateVertex;

public class StateVertexResult {
	private final int id;
	private final String url;
	private String name;

	private boolean success;
	private boolean identical;
	private List<Invariant> failedInvariants;

	public StateVertexResult(StateVertex state) {
		this.id = state.getId();
		this.url = state.getUrl();
		this.name = state.getName();
		this.success = true;
		this.identical = true;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setIdentical(boolean identical) {
		this.identical = identical;
	}

	public void setFailedInvariants(List<Invariant> failedInvariants) {
		this.failedInvariants = failedInvariants;
	}
}

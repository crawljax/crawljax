package com.crawljax.plugins.testcasegenerator.report;

import java.util.LinkedList;
import java.util.List;

import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateVertex;

public class MethodResult {
	private LinkedList<StateVertexResult> crawlStates;
	private LinkedList<EventableResult> crawlPath;
	private boolean success;
	private String methodName;

	public MethodResult(String methodName) {
		this.methodName = methodName;
		this.crawlPath = new LinkedList<EventableResult>();
		this.crawlStates = new LinkedList<StateVertexResult>();
		this.success = false;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void addEventable(Eventable eventable) {
		this.crawlPath.add(new EventableResult(eventable));
	}

	public void addState(StateVertex state) {
		this.crawlStates.add(new StateVertexResult(state));
	}

	public void markLastEventableFailed() {
		this.crawlPath.getLast().setSuccess(false);
	}

	public void markLastStateFailed(List<Invariant> failedInvariants) {
		this.crawlStates.getLast().setSuccess(false);
		this.crawlStates.getLast().setFailedInvariants(failedInvariants);
	}

	public void markLastStateDifferent() {
		this.crawlStates.getLast().setIdentical(false);
	}
}

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
	private WarnLevel warnLevel;
	
	/**
	 * @author rahulkrishna
	 * 0 - states same
	 * 1 - state has data changes but dynamic
	 * 2 - state has changes and not dynamic
	 * 3 - states are nd2 (structure different)
	 */
	public enum WarnLevel {
		LEVEL0, LEVEL1, LEVEL2, LEVEL3
	}

	public MethodResult(String methodName) {
		this.methodName = methodName;
		this.crawlPath = new LinkedList<EventableResult>();
		this.crawlStates = new LinkedList<StateVertexResult>();
		this.success = false;
		this.warnLevel = WarnLevel.LEVEL0;
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
	
	public void setLastStateComparison(String compResult) {
		this.crawlStates.getLast().setCompResult(compResult);
	}

	public void setWarnLevel(WarnLevel level) {
		this.crawlStates.getLast().setWarnLevel(level);
		this.warnLevel = getHigherLevel(level, warnLevel);
	}
	
	public WarnLevel getHigherLevel(WarnLevel level1, WarnLevel level2) {
		if(level1 == WarnLevel.LEVEL3 || level2 == WarnLevel.LEVEL3) {
			return WarnLevel.LEVEL3;
		}
		else if(level1 == WarnLevel.LEVEL2 || level2 == WarnLevel.LEVEL2) {
			return WarnLevel.LEVEL2;
		}
		else if(level1 == WarnLevel.LEVEL1 || level2 == WarnLevel.LEVEL1){
			return WarnLevel.LEVEL1;
		}
		else {
			return WarnLevel.LEVEL0;
		}
	}

	public List<StateVertexResult> getStateResults() {
		return crawlStates;
	}
	
	public List<EventableResult> getEventableResults() {
		return crawlPath;
	}

	public void setLocatorWarning(boolean broken) {
		this.crawlStates.getLast().setLocatorWarning(broken);
	}
	
	public StateVertexResult getLastState() {
		return this.crawlStates.getLast();
	}

	public void setEventableResults(LinkedList<EventableResult> eventableResults) {
		crawlPath = eventableResults;
	}
}

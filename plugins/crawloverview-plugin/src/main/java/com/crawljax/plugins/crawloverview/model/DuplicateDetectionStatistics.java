package com.crawljax.plugins.crawloverview.model;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateVertexNDD;
import com.crawljax.core.state.duplicatedetection.Fingerprint;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DuplicateDetectionStatistics {
	
	private double threshold = 0;
	private double minThreshold = 0;
	private double maxThreshold = 0;
	private double stepsize = 0;

	public DuplicateDetectionStatistics(CrawlSession session) {
		if(session.getInitialState() instanceof StateVertexNDD) {
			StateVertexNDD stateVertex = (StateVertexNDD) session.getInitialState();
			Fingerprint fingerprint = stateVertex .getFingerprint();
			this.threshold = fingerprint.getDefaultThreshold();
			this.maxThreshold = fingerprint.getThresholdUpperlimit();
			this.minThreshold = fingerprint.getThresholdLowerlimit();
			this.stepsize = this.maxThreshold > 1 ? 1 : 0.05;
		}
	}

	@JsonCreator
	public DuplicateDetectionStatistics(
			@JsonProperty("threshold") double threshold,
	        @JsonProperty("maxThreshold") double maxThreshold,
	        @JsonProperty("minThreshold") double minThreshold,
	        @JsonProperty("stepsize") double stepsize) {
		this.threshold = threshold;
		this.maxThreshold = maxThreshold;
		this.minThreshold = minThreshold;
		this.stepsize = stepsize;
	}

	public double getThreshold() {
		return threshold;
	}

	public double getMinThreshold() {
		return minThreshold;
	}

	public double getMaxThreshold() {
		return maxThreshold;
	}

	public double getStepsize() {
		return stepsize;
	}
	
	
}
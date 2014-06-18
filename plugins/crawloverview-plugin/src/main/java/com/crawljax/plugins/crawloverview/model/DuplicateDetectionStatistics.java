package com.crawljax.plugins.crawloverview.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DuplicateDetectionStatistics {
	
	private double threshold;
	private double minThreshold;
	private double maxThreshold;
	private double stepsize;

	public DuplicateDetectionStatistics() {
		// TODO link to duplicateDetection-class.
		this.threshold = 3;
		this.maxThreshold = 32;
		this.minThreshold = 0;
		this.stepsize = 0.5;
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
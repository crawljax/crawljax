package com.crawljax.fragmentation;

public class FragmentRules {
	double thresholdWidth = 50;
	double thresholdHeight = 50;
	int subtreeWidth_and = 1;
	int subtreeWidth_or = 4;
	public FragmentRules() {
		
	}
	
	public FragmentRules(double thresholdWidth, double thresholdHeight, int subtreeWidth_and, int subtreeWidth_or) {
		this.thresholdHeight = thresholdHeight;
		this.thresholdWidth = thresholdWidth;
		this.subtreeWidth_and = subtreeWidth_and;
		this.subtreeWidth_or = subtreeWidth_or;
	}

	public double getThresholdWidth() {
		return thresholdWidth;
	}

	public void setThresholdWidth(double thresholdWidth) {
		this.thresholdWidth = thresholdWidth;
	}

	public double getThresholdHeight() {
		return thresholdHeight;
	}

	public void setThresholdHeight(double thresholdHeight) {
		this.thresholdHeight = thresholdHeight;
	}

	public int getSubtreeWidth_and() {
		return subtreeWidth_and;
	}

	public void setSubtreeWidth_and(int subtreeWidth_and) {
		this.subtreeWidth_and = subtreeWidth_and;
	}

	public int getSubtreeWidth_or() {
		return subtreeWidth_or;
	}

	public void setSubtreeWidth_or(int subtreeWidth_or) {
		this.subtreeWidth_or = subtreeWidth_or;
	}
}

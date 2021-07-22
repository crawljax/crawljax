package com.crawljax.core;

public class CrawlPathInfo {
	int id;
	int backtrackTarget;
	boolean backtrackSuccess;
	int reachedNearDup;
	String pathString;
	
	public CrawlPathInfo(int id, int backtrackTarget, boolean backtrackSuccess, int reachedNearDup) {
		this.id = id;
		this.backtrackTarget  = backtrackTarget;
		this.backtrackSuccess = backtrackSuccess;
		this.reachedNearDup = reachedNearDup;
		this.pathString = "";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBacktrackTarget() {
		return backtrackTarget;
	}

	public void setBacktrackTarget(int backtrackTarget) {
		this.backtrackTarget = backtrackTarget;
	}

	public boolean isBacktrackSuccess() {
		return backtrackSuccess;
	}

	public void setBacktrackSuccess(boolean backtrackSuccess) {
		this.backtrackSuccess = backtrackSuccess;
	}

	public int isReachedNearDup() {
		return reachedNearDup;
	}

	public void setReachedNearDup(int reachedNearDup) {
		this.reachedNearDup = reachedNearDup;
	}

	public void setPathString(String pathString) {
		this.pathString = pathString;
	}
	
	public String getPathString() {
		return pathString;
	}
}

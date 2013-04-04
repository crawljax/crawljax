package com.crawljax.plugins.crawloverview.model;

import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.openqa.selenium.Point;

import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableList;

@Immutable
public class State {

	private final String name;
	private final String url;
	private final ImmutableList<CandidateElementPosition> candidateElements;
	private final int fanIn;
	private final int fanOut;
	private final int screenshotOffsetTop;
	private final int screenshotOffsetLeft;

	public State(StateVertex state, int fanIn, int fanOut,
	        ImmutableList<CandidateElementPosition> candidates, Point offset) {
		this.fanIn = fanIn;
		this.fanOut = fanOut;
		candidateElements = candidates;
		this.name = state.getName();
		this.url = state.getUrl();
		this.screenshotOffsetLeft = offset.x;
		this.screenshotOffsetTop = offset.y;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public ImmutableList<CandidateElementPosition> getCandidateElements() {
		return candidateElements;
	}

	public int getFanIn() {
		return fanIn;
	}

	public int getFanOut() {
		return fanOut;
	}

	public int getScreenshotOffsetTop() {
		return screenshotOffsetTop;
	}

	public int getScreenshotOffsetLeft() {
		return screenshotOffsetLeft;
	}

	@Override
	public String toString() {
		return "State [name=" + name + ", url=" + url + ", candidateElements="
		        + candidateElements + "]";
	}

}

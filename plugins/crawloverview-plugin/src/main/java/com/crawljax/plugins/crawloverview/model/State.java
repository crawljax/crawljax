package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import org.openqa.selenium.Point;

import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableList;
import com.google.common.base.Objects;

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
	public int hashCode() {
		return Objects.hashCode(name, url, candidateElements, fanIn, fanOut, screenshotOffsetTop,
		        screenshotOffsetLeft);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof State) {
			State that = (State) object;
			return Objects.equal(this.name, that.name)
			        && Objects.equal(this.url, that.url)
			        && Objects.equal(this.candidateElements, that.candidateElements)
			        && Objects.equal(this.fanIn, that.fanIn)
			        && Objects.equal(this.fanOut, that.fanOut)
			        && Objects.equal(this.screenshotOffsetTop, that.screenshotOffsetTop)
			        && Objects.equal(this.screenshotOffsetLeft, that.screenshotOffsetLeft);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("name", name)
		        .add("url", url)
		        .add("candidateElements", candidateElements)
		        .add("fanIn", fanIn)
		        .add("fanOut", fanOut)
		        .add("screenshotOffsetTop", screenshotOffsetTop)
		        .add("screenshotOffsetLeft", screenshotOffsetLeft)
		        .toString();
	}

}

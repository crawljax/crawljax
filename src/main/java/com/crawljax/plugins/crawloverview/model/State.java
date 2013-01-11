package com.crawljax.plugins.crawloverview.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.Point;

import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.RenderedCandidateElement;
import com.google.common.collect.Lists;

public class State {

	private final String name;
	private final String url;
	private final List<RenderedCandidateElement> candidateElements;
	private Point screenShotOffset;
	private final AtomicInteger fanIn = new AtomicInteger();
	private final AtomicInteger fanOut = new AtomicInteger();

	public State(String name, String url) {
		this.name = name;
		this.url = url;
		candidateElements = Lists.newLinkedList();
	}

	public State(StateVertex state) {
		this(state.getName(), state.getUrl());
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public List<RenderedCandidateElement> getCandidateElements() {
		return candidateElements;
	}

	public void setScreenShotOffset(Point point) {
		this.screenShotOffset = point;
	}

	public Point getScreenShotOffset() {
		return screenShotOffset;
	}

	public int incrementFanOut() {
		return fanOut.incrementAndGet();
	}

	public int incrementFanIn() {
		return fanIn.incrementAndGet();
	}

	public int getFanIn() {
		return fanIn.get();
	}

	public int getFanOut() {
		return fanOut.get();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
		        prime * result + ((candidateElements == null) ? 0 : candidateElements.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (candidateElements == null) {
			if (other.candidateElements != null)
				return false;
		} else if (!candidateElements.equals(other.candidateElements))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "State [name=" + name + ", url=" + url + ", candidateElements="
		        + candidateElements + "]";
	}

}

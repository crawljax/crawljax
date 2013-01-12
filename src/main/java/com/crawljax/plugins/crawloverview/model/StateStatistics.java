package com.crawljax.plugins.crawloverview.model;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSortedSet;

@Immutable
public class StateStatistics {

	private final State leastFanOut;
	private final State leastFanIn;
	private final State mostFanOut;
	private final State mostFanIn;
	private final int totalNumberOfStates;
	private final ImmutableSet<String> urls;

	public StateStatistics(Collection<State> states) {
		totalNumberOfStates = states.size();

		State randomState = states.iterator().next();
		State leastFanOut = randomState;
		State mostFanOut = randomState;
		State leastFanIn = randomState;
		State mostFanIn = randomState;
		Builder<String> builder = ImmutableSortedSet.naturalOrder();
		for (State state : states) {
			if (state.getFanIn() > mostFanIn.getFanIn()) {
				mostFanIn = state;
			} else if (state.getFanIn() < leastFanIn.getFanIn()) {
				leastFanIn = state;
			}
			if (state.getFanOut() > mostFanOut.getFanOut()) {
				mostFanOut = state;
			} else if (state.getFanOut() < leastFanOut.getFanOut()) {
				leastFanOut = state;
			}
			builder.add(state.getUrl());
		}
		this.urls = builder.build();
		this.leastFanOut = leastFanOut;
		this.leastFanIn = leastFanIn;
		this.mostFanOut = mostFanOut;
		this.mostFanIn = mostFanIn;
	}

	public State getLeastFanOut() {
		return leastFanOut;
	}

	public State getLeastFanIn() {
		return leastFanIn;
	}

	public State getMostFanOut() {
		return mostFanOut;
	}

	public State getMostFanIn() {
		return mostFanIn;
	}

	public int getTotalNumberOfStates() {
		return totalNumberOfStates;
	}

	public ImmutableSet<String> getUrls() {
		return urls;
	}
}

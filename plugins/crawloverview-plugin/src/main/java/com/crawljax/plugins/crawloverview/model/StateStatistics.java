package com.crawljax.plugins.crawloverview.model;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableSetMultimap;

@Immutable
public class StateStatistics {

	private final State leastFanOut;
	private final State leastFanIn;
	private final State mostFanOut;
	private final State mostFanIn;
	private final int totalNumberOfStates;
	private final ImmutableSetMultimap<String, String> urls;

	public StateStatistics(Collection<State> states) {
		totalNumberOfStates = states.size();

		State randomState = states.iterator().next();
		State tmpLeastFanOut = randomState;
		State tmpMostFanOut = randomState;
		State tmpLeastFanIn = randomState;
		State tmpMostFanIn = randomState;
		ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();
		for (State state : states) {
			if (state.getFanIn() > tmpMostFanIn.getFanIn()) {
				tmpMostFanIn = state;
			} else if (state.getFanIn() < tmpLeastFanIn.getFanIn()) {
				tmpLeastFanIn = state;
			}
			if (state.getFanOut() > tmpMostFanOut.getFanOut()) {
				tmpMostFanOut = state;
			} else if (state.getFanOut() < tmpLeastFanOut.getFanOut()) {
				tmpLeastFanOut = state;
			}
			builder.put(state.getUrl(), state.getName());
		}
		this.urls = builder.build();
		this.leastFanOut = tmpLeastFanOut;
		this.leastFanIn = tmpLeastFanIn;
		this.mostFanOut = tmpMostFanOut;
		this.mostFanIn = tmpMostFanIn;
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

	public ImmutableSetMultimap<String, String> getUrls() {
		return urls;
	}
}

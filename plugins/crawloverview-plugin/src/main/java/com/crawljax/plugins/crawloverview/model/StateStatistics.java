package com.crawljax.plugins.crawloverview.model;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSetMultimap;

@Immutable
public class StateStatistics {

	private final Pair<String, Integer> leastFanOut;
	private final Pair<String, Integer> leastFanIn;
	private final Pair<String, Integer> mostFanOut;
	private final Pair<String, Integer> mostFanIn;
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
		this.leastFanOut = Pair.of(tmpLeastFanOut.getName(), tmpLeastFanOut.getFanOut());
		this.leastFanIn = Pair.of(tmpLeastFanIn.getName(), tmpLeastFanIn.getFanOut());
		this.mostFanOut = Pair.of(tmpMostFanOut.getName(), tmpMostFanOut.getFanOut());
		this.mostFanIn = Pair.of(tmpMostFanIn.getName(), tmpMostFanIn.getFanOut());
	}

	public Pair<String, Integer> getLeastFanOut() {
		return leastFanOut;
	}

	public Pair<String, Integer> getLeastFanIn() {
		return leastFanIn;
	}

	public Pair<String, Integer> getMostFanOut() {
		return mostFanOut;
	}

	public Pair<String, Integer> getMostFanIn() {
		return mostFanIn;
	}

	public int getTotalNumberOfStates() {
		return totalNumberOfStates;
	}

	public ImmutableSetMultimap<String, String> getUrls() {
		return urls;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(leastFanOut, leastFanIn, mostFanOut, mostFanIn,
		        totalNumberOfStates, urls);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof StateStatistics) {
			StateStatistics that = (StateStatistics) object;
			return Objects.equal(this.leastFanOut, that.leastFanOut)
			        && Objects.equal(this.leastFanIn, that.leastFanIn)
			        && Objects.equal(this.mostFanOut, that.mostFanOut)
			        && Objects.equal(this.mostFanIn, that.mostFanIn)
			        && Objects.equal(this.totalNumberOfStates, that.totalNumberOfStates)
			        && Objects.equal(this.urls, that.urls);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("leastFanOut", leastFanOut)
		        .add("leastFanIn", leastFanIn)
		        .add("mostFanOut", mostFanOut)
		        .add("mostFanIn", mostFanIn)
		        .add("totalNumberOfStates", totalNumberOfStates)
		        .add("urls", urls)
		        .toString();
	}

}

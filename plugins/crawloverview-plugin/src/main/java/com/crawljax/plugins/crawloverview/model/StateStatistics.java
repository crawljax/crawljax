package com.crawljax.plugins.crawloverview.model;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSetMultimap;

@Immutable
public class StateStatistics {

	private final StateCounter leastFanOut;
	private final StateCounter leastFanIn;
	private final StateCounter mostFanOut;
	private final StateCounter mostFanIn;
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
		this.leastFanOut = new StateCounter(tmpLeastFanOut.getName(), tmpLeastFanOut.getFanOut());
		this.leastFanIn = new StateCounter(tmpLeastFanIn.getName(), tmpLeastFanIn.getFanIn());
		this.mostFanOut = new StateCounter(tmpMostFanOut.getName(), tmpMostFanOut.getFanOut());
		this.mostFanIn = new StateCounter(tmpMostFanIn.getName(), tmpMostFanIn.getFanIn());
	}

	@JsonCreator
	public StateStatistics(@JsonProperty("leastFanOut") StateCounter leastFanOut,
	        @JsonProperty("leastFanIn") StateCounter leastFanIn,
	        @JsonProperty("mostFanOut") StateCounter mostFanOut,
	        @JsonProperty("mostFanIn") StateCounter mostFanIn,
	        @JsonProperty("totalNumberOfStates") int totalNumberOfStates,
	        @JsonProperty("urls") ImmutableSetMultimap<String, String> urls) {
		this.leastFanOut = leastFanOut;
		this.leastFanIn = leastFanIn;
		this.mostFanOut = mostFanOut;
		this.mostFanIn = mostFanIn;
		this.totalNumberOfStates = totalNumberOfStates;
		this.urls = urls;
	}

	public StateCounter getLeastFanIn() {
		return leastFanIn;
	}

	public StateCounter getLeastFanOut() {
		return leastFanOut;
	}

	public StateCounter getMostFanIn() {
		return mostFanIn;
	}

	public StateCounter getMostFanOut() {
		return mostFanOut;
	}

	public int getTotalNumberOfStates() {
		return totalNumberOfStates;
	}

	/**
	 * @return A mapping from the {@link String} url to the {@link String} state name.
	 */
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

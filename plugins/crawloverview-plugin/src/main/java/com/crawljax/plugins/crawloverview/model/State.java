package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import com.crawljax.core.state.StateVertex;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class State {

	private final String name;
	private final String url;
	private final ImmutableList<CandidateElementPosition> candidateElements;
	private final int fanIn;
	private final int fanOut;
	private final int id;
	private final ImmutableList<String> failedEvents;
	private final boolean hasNearDuplicate;
	private final String nearestState;
	private final double distToNearestState;
	private int cluster = -1;
	private long timeAdded;

	/*
	 * public State(StateVertex state, int fanIn, int fanOut,
	 * ImmutableList<CandidateElementPosition> candidates, ImmutableList<String> failedEvents) {
	 * this.fanIn = fanIn; this.fanOut = fanOut; candidateElements = candidates; this.failedEvents =
	 * failedEvents; this.name = state.getName(); this.url = state.getUrl(); this.id =
	 * state.getId(); }
	 */

	public int getCluster() {
		return cluster;
	}

	public void setCluster(int cluster) {
		this.cluster = cluster;
	}

	public double getDistToNearestState() {
		return distToNearestState;
	}

	public State(StateVertex state, int fanIn, int fanOut,
	        ImmutableList<CandidateElementPosition> candidates,
	        ImmutableList<String> failedEvents, boolean hasNearDuplicate, String nearestState,
	        double distToNearestState) {
		this.fanIn = fanIn;
		this.fanOut = fanOut;
		candidateElements = candidates;
		this.failedEvents = failedEvents;
		this.name = state.getName();
		this.url = state.getUrl();
		this.id = state.getId();
		this.hasNearDuplicate = hasNearDuplicate;
		this.nearestState = nearestState;
		this.distToNearestState = distToNearestState;
	}

	/*
	 * @JsonCreator public State(
	 * @JsonProperty("name") String name,
	 * @JsonProperty("url") String url,
	 * @JsonProperty("candidateElements") ImmutableList<CandidateElementPosition> candidateElements,
	 * @JsonProperty("fanIn") int fanIn, @JsonProperty("fanOut") int fanOut,
	 * @JsonProperty("id") int id,
	 * @JsonProperty("failedEvents") ImmutableList<String> failedEvents) { super(); this.name =
	 * name; this.url = url; this.candidateElements = candidateElements; this.fanIn = fanIn;
	 * this.fanOut = fanOut; this.id = id; this.failedEvents = failedEvents; }
	 */

	public long getTimeAdded() {
		return timeAdded;
	}

	public void setTimeAdded(long timeAdded) {
		this.timeAdded = timeAdded;
	}

	@JsonCreator
	public State(
	        @JsonProperty("name") String name,
	        @JsonProperty("url") String url,
	        @JsonProperty("candidateElements") ImmutableList<CandidateElementPosition> candidateElements,
	        @JsonProperty("fanIn") int fanIn, @JsonProperty("fanOut") int fanOut,
	        @JsonProperty("id") int id,
	        @JsonProperty("failedEvents") ImmutableList<String> failedEvents,
	        @JsonProperty("hasNearDuplicate") boolean hasNearDuplicate,
	        @JsonProperty("nearDuplicate") String nearestState,
	        @JsonProperty("distToNearestState") double distToNearestState,
	        @JsonProperty("cluster") int cluster,
	        @JsonProperty("timeAdded") long timeAdded) {
		super();
		this.name = name;
		this.url = url;
		this.candidateElements = candidateElements;
		this.fanIn = fanIn;
		this.fanOut = fanOut;
		this.id = id;
		this.failedEvents = failedEvents;
		this.hasNearDuplicate = hasNearDuplicate;
		this.nearestState = nearestState;
		this.distToNearestState = distToNearestState;
		this.cluster = cluster;
		this.timeAdded = timeAdded;
	}

	public State(StateVertex state, int fanIn, int fanOut,
	        ImmutableList<CandidateElementPosition> candidates,
	        ImmutableList<String> failedEvents, boolean hasNearDuplicate, String nearestState,
	        double distToNearestState,
	        long timeAdded) {
		this.fanIn = fanIn;
		this.fanOut = fanOut;
		candidateElements = candidates;
		this.failedEvents = failedEvents;
		this.name = state.getName();
		this.url = state.getUrl();
		this.id = state.getId();
		this.hasNearDuplicate = hasNearDuplicate;
		this.nearestState = nearestState;
		this.distToNearestState = distToNearestState;
		this.timeAdded = timeAdded;
		this.cluster = state.getCluster();
	}

	public boolean isHasNearDuplicate() {
		return hasNearDuplicate;
	}

	public String getNearestState() {
		return nearestState;
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

	public int getId() {
		return id;
	}

	public ImmutableList<String> getFailedEvents() {
		return failedEvents;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name, url, candidateElements, fanIn, fanOut,
		        id, failedEvents);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof State) {
			State that = (State) object;
			return Objects.equal(this.id, that.id)
			        && Objects.equal(this.name, that.name)
			        && Objects.equal(this.url, that.url)
			        && Objects.equal(this.candidateElements,
			                that.candidateElements)
			        && Objects.equal(this.fanIn, that.fanIn)
			        && Objects.equal(this.fanOut, that.fanOut)
			        && Objects.equal(this.failedEvents,
			                that.failedEvents);
		}
		return false;
	}

	/*
	 * @Override public String toString() { return MoreObjects.toStringHelper(this).add("name",
	 * name).add("id", id) .add("url", url).add("candidateElements", candidateElements)
	 * .add("fanIn", fanIn).add("fanOut", fanOut) .add("failedEvents", failedEvents).toString(); }
	 */
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", name).add("id", id)
		        .add("url", url).add("candidateElements", candidateElements)
		        .add("fanIn", fanIn).add("fanOut", fanOut)
		        .add("failedEvents", failedEvents)
		        .add("hasNearDuplicate", hasNearDuplicate)
		        .add("nearestState", nearestState)
		        .add("distToNearestState", distToNearestState)
		        .add("timeAdded", timeAdded)
		        .toString();
	}

}

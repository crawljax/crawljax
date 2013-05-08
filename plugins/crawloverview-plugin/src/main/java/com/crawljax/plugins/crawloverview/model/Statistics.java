package com.crawljax.plugins.crawloverview.model;

import java.text.DecimalFormat;
import java.util.Date;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@Immutable
public class Statistics {

	private final String duration;
	private final int crawlPaths;
	private final String averageDomSize;
	private final int edges;
	private final Date startDate;
	private final StateStatistics stateStats;
	private final int failedEvents;

	public Statistics(CrawlSession session, StateStatistics stateStats, Date startDate,
	        int failedEvents) {
		this.stateStats = stateStats;
		this.failedEvents = failedEvents;
		this.startDate = new Date(startDate.getTime());
		StateFlowGraph stateFlowGraph = session.getStateFlowGraph();
		this.duration = calculateDuration(session);
		this.edges = stateFlowGraph.getAllEdges().size();
		this.crawlPaths = session.getCrawlPaths().size();
		double bytes = stateFlowGraph.getMeanStateStringSize();
		this.averageDomSize = new DecimalFormat().format(bytes / 1000) + " kB";
	}

	@JsonCreator
	public Statistics(@JsonProperty("duration") String duration,
	        @JsonProperty("crawlPaths") int crawlPaths,
	        @JsonProperty("averageDomSize") String averageDomSize,
	        @JsonProperty("edges") int edges, @JsonProperty("startDate") Date startDate,
	        @JsonProperty("stateStats") StateStatistics stateStats,
	        @JsonProperty("failedEvents") int failedEvents) {
		this.duration = duration;
		this.crawlPaths = crawlPaths;
		this.averageDomSize = averageDomSize;
		this.edges = edges;
		this.startDate = startDate;
		this.stateStats = stateStats;
		this.failedEvents = failedEvents;
	}

	private String calculateDuration(CrawlSession session) {
		long start = session.getStartTime();
		long stop = System.currentTimeMillis();
		return DurationFormatUtils.formatDurationWords(stop - start, true, true);
	}

	public String getAverageDomSize() {
		return averageDomSize;
	}

	public String getDuration() {
		return duration;
	}

	public int getCrawlPaths() {
		return crawlPaths;
	}

	public int getEdges() {
		return edges;
	}

	public StateStatistics getStateStats() {
		return stateStats;
	}

	public Date getStartDate() {
		return new Date(startDate.getTime());
	}

	public int getFailedEvents() {
		return failedEvents;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(duration, crawlPaths, averageDomSize, edges,
		        stateStats, failedEvents);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Statistics) {
			Statistics that = (Statistics) object;
			return Objects.equal(this.duration, that.duration)
			        && Objects.equal(this.crawlPaths, that.crawlPaths)
			        && Objects.equal(this.averageDomSize, that.averageDomSize)
			        && Objects.equal(this.edges, that.edges)
			        && Objects.equal(this.stateStats, that.stateStats)
			        && Objects.equal(this.failedEvents, that.failedEvents);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("duration", duration)
		        .add("failedEvents", failedEvents)
		        .add("crawlPaths", crawlPaths)
		        .add("averageDomSize", averageDomSize)
		        .add("edges", edges)
		        .add("startDate", startDate)
		        .add("stateStats", stateStats)
		        .toString();
	}

}

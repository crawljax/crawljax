package com.crawljax.plugins.crawloverview.model;

import java.text.DecimalFormat;
import java.util.Date;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;
import com.google.common.base.Objects;

@Immutable
public class Statistics {

	private final String duration;
	private final int crawlPaths;
	private final String averageDomSize;
	private final int edges;
	private final Date startDate;
	private final StateStatistics stateStats;

	public Statistics(CrawlSession session, StateStatistics stateStats, Date startDate) {
		this.stateStats = stateStats;
		this.startDate = new Date(startDate.getTime());
		StateFlowGraph stateFlowGraph = session.getStateFlowGraph();
		this.duration = calculateDuration(session);
		this.edges = stateFlowGraph.getAllEdges().size();
		this.crawlPaths = session.getCrawlPaths().size();
		double bytes = stateFlowGraph.getMeanStateStringSize();
		this.averageDomSize = new DecimalFormat().format(bytes / 1000) + " kB";
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

	public int getNumberOfEdges() {
		return edges;
	}

	public int getNumberOfStates() {
		return stateStats.getTotalNumberOfStates();
	}

	public StateStatistics getStateStats() {
		return stateStats;
	}

	public Date getStartDate() {
		return new Date(startDate.getTime());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(duration, crawlPaths, averageDomSize, edges, startDate,
		        stateStats);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Statistics) {
			Statistics that = (Statistics) object;
			return Objects.equal(this.duration, that.duration)
			        && Objects.equal(this.crawlPaths, that.crawlPaths)
			        && Objects.equal(this.averageDomSize, that.averageDomSize)
			        && Objects.equal(this.edges, that.edges)
			        && Objects.equal(this.startDate, that.startDate)
			        && Objects.equal(this.stateStats, that.stateStats);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		        .add("duration", duration)
		        .add("crawlPaths", crawlPaths)
		        .add("averageDomSize", averageDomSize)
		        .add("edges", edges)
		        .add("startDate", startDate)
		        .add("stateStats", stateStats)
		        .toString();
	}

}

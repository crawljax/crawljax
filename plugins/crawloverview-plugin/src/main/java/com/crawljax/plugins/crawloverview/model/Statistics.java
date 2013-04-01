package com.crawljax.plugins.crawloverview.model;

import java.text.DecimalFormat;
import java.util.Date;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;

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
		this.startDate = startDate;
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
		return startDate;
	}

}

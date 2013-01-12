package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;

@Immutable
public class Statistics {

	private final String duration;
	private final int crawlPaths;
	private final int averageDomSize;
	private final int edges;
	private final StateStatistics stateStats;

	public Statistics(CrawlSession session, StateStatistics stateStats) {
		this.stateStats = stateStats;
		StateFlowGraph stateFlowGraph = session.getStateFlowGraph();
		this.duration = calculateDuration(session);
		this.edges = stateFlowGraph.getAllEdges().size();
		this.crawlPaths = session.getCrawlPaths().size();

		// Divide by two to get the number of characters.
		this.averageDomSize = stateFlowGraph.getMeanStateStringSize() / 2;
	}

	private String calculateDuration(CrawlSession session) {
		long start = session.getStartTime();
		long stop = System.currentTimeMillis();
		return DurationFormatUtils.formatDurationWords(stop - start, true, true);
	}

	public int getAverageDomSize() {
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

}

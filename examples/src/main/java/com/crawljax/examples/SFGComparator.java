package com.crawljax.examples;

import org.jgrapht.DirectedGraph;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.ScalableSFG;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;

public class SFGComparator implements PostCrawlingPlugin {

	@Override
	public void postCrawling(CrawlSession session, ExitStatus exitReason) {
		// TODO Auto-generated method stub

		ScalableSFG scalableSFG =
		        new ScalableSFG(CorrectnessExperiment.DB_PATH, CorrectnessExperiment.root,
		                CorrectnessExperiment.nodeIndex, CorrectnessExperiment.edgeIndex,
		                new ExitNotifier(0));
		DirectedGraph<StateVertex, Eventable> scablabeGraph = scalableSFG.buildJgraphT();

		CompareGraphs(scalableSFG, session.getStateFlowGraph());

	}

	public static boolean CompareGraphs(StateFlowGraph first, StateFlowGraph second) {

		for (Eventable firstEdge : first.getAllEdges()) {
			boolean found = false;
			for (Eventable secondEdge : second.getAllEdges()) {

				if (firstEdge.toString().equals(secondEdge.toString())
				        && firstEdge.getTargetStateVertex().equals(
				                secondEdge.getTargetStateVertex())
				        && firstEdge.getSourceStateVertex().equals(
				                secondEdge.getSourceStateVertex())) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}

		return true;
	}
}
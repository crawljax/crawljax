package com.crawljax.examples;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.ScalableSFG;

public class DB_Path_Getter implements PostCrawlingPlugin {

	@Override
	public void postCrawling(CrawlSession session, ExitStatus exitReason) {
		// TODO Auto-generated method stub

		CorrectnessExperiment.DB_PATH = ScalableSFG.DB_PATH;
		CorrectnessExperiment.edgeIndex = ScalableSFG.edgesIndex;
		CorrectnessExperiment.nodeIndex = ScalableSFG.nodeIndex;
		CorrectnessExperiment.root = ScalableSFG.root;

	}
}
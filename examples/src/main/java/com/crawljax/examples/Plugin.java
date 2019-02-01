package com.crawljax.examples;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;

/*
 *  In order to run test steps to get to initial state. 
 *  		https://github.com/aminmf/crawljax/blob/master/core/src/main/java/com/crawljax/core/plugin/ExecuteInitialPathsPlugin.java
		extends plugin with ExecuteInitialPathsPlugin which can CrawljaxConfiguration config, CrawlTaskConsumer firstConsumer
 */

public class Plugin implements OnNewStatePlugin, OnUrlLoadPlugin, PostCrawlingPlugin, PreCrawlingPlugin {


	private String testCaseFile;

	private int brokenStep;

	private String templateToMatch = null;

	public Plugin() {
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postCrawling(CrawlSession session, ExitStatus exitReason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUrlLoad(CrawlerContext context) {
		/*// TODO Auto-generated method stub
		StateFlowGraph graph = context.getSession().getStateFlowGraph();
		
		StateVertex currVertex = context.stateMachine.newStateFor(context.getBrowser());
		if(graph.hasClone(currVertex)) {
			StateVertex clone = graph.getStateInGraph(currVertex);
			if(!clone.getName().equalsIgnoreCase("index")){
				System.out.println("index has changed to : " + clone.getName());
				context.stateMachine.setCurrentState(clone);
				if(!context.stateMachine.onURLSet.contains(clone))
					context.stateMachine.onURLSet.add(clone);
			}
		}
		else {
			System.out.println("Could not find an existing state for the URL load ");
		}*/
	}

	@Override
	public void onNewState(CrawlerContext context, StateVertex newState) {
		// TODO Auto-generated method stub
		
	}
}

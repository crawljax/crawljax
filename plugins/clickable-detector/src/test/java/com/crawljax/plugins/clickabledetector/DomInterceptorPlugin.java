package com.crawljax.plugins.clickabledetector;

import java.util.LinkedList;
import java.util.List;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

/**
 * Saves the captured doms to a list
 */
public class DomInterceptorPlugin implements OnNewStatePlugin {

	private final List<String> intercepted = new LinkedList<>();

	@Override
	public void onNewState(CrawlerContext context, StateVertex newState) {
		intercepted.add(newState.getDom());
	}

	public List<String> getIntercepted() {
		return intercepted;
	}
}

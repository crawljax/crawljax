package com.crawljax.core.plugin;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.state.StateVertex;

public interface ClickablesDetectorPlugin extends Plugin {

	public void onNewState(CrawlerContext context, StateVertex newState);

	boolean isEnabled(String dom);
}


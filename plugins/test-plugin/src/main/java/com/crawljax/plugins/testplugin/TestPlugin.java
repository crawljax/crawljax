package com.crawljax.plugins.testplugin;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.HostInterface;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.StateVertex;

public class TestPlugin implements OnNewStatePlugin,
		PreCrawlingPlugin {

	private HostInterface hostInterface;

	public TestPlugin(HostInterface hostInterface) {
		this.hostInterface = hostInterface;
	}

	@Override
	public void onNewState(CrawlerContext context, StateVertex newState) {
		try {
			String dom = context.getBrowser().getStrippedDom();
			File file = new File(hostInterface.getOutputDirectory(), context.getCurrentState().getName() + ".html");

			FileWriter fw = new FileWriter(file, false);
			fw.write(dom);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {
		try {
			File file = new File(hostInterface.getOutputDirectory(), "parameters.txt");
			FileWriter fw = new FileWriter(file, false);
			for(Map.Entry<String, String> parameter : hostInterface.getParameters().entrySet()) {
				fw.write(parameter.getKey() + ": " + parameter.getValue() + System.getProperty("line.separator"));
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

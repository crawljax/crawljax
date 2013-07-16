package com.crawljax.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

/**
 * This example shows how to add your own plugin. The plugin just prints the DOM when a new state is
 * detected.
 */
public class PluginExample {

	private static final Logger LOG = LoggerFactory.getLogger(PluginExample.class);

	public static void main(String[] args) {

		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor("http://demo.crawljax.com/");
		builder.addPlugin(new OnNewStatePlugin() {

			@Override
			public void onNewState(CrawlerContext context, StateVertex newState) {
				// This will print the DOM when a new state is detected. You should see it in your
				// console.
				LOG.info("Found a new dom! Here it is:\n{}", context.getBrowser().getStrippedDom());
			}

			@Override
			public String toString() {
				return "Our example plugin";
			}
		});
		CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
		crawljax.call();
	}
}

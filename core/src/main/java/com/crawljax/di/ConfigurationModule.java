package com.crawljax.di;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.core.state.DbStateFlowGraph;
import com.crawljax.core.state.DefaultStateFlowGraph;
import com.crawljax.core.state.GraphDBStateFlowGraphNoRam;
import com.crawljax.core.state.Neo4jBackedStateFlowGraphInMemoryPathFinder;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateFlowGraph.StateFlowGraphType;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.util.Providers;

/**
 * Binds the configuration elements so they are injectable.
 */
public class ConfigurationModule extends AbstractModule {

	private final CrawljaxConfiguration config;

	public ConfigurationModule(CrawljaxConfiguration config) {
		this.config = config;

	}

	@Override
	protected void configure() {
		bind(URL.class).annotatedWith(BaseUrl.class).toInstance(config.getUrl());
		bind(CrawljaxConfiguration.class).toInstance(config);
		bind(CrawlRules.class).toInstance(config.getCrawlRules());
		bind(ProxyConfiguration.class).toInstance(config.getProxyConfiguration());
		bind(Plugins.class).toInstance(config.getPlugins());

		BrowserConfiguration browserConfig = config.getBrowserConfig();
		bind(BrowserConfiguration.class).toInstance(browserConfig);

		if (browserConfig.isDefaultBuilder()) {
			bind(EmbeddedBrowser.class).toProvider(WebDriverBrowserBuilder.class);
		} else {
			bind(EmbeddedBrowser.class).toProvider(
			        Providers.guicify(browserConfig.getBrowserBuilder()));
		}

		if (config.getGraphType() == StateFlowGraphType.DEAFAULT) {
			bind(StateFlowGraph.class).to(DefaultStateFlowGraph.class);
		} else if (config.getGraphType() == StateFlowGraphType.DB1) {
			bind(StateFlowGraph.class).to(DbStateFlowGraph.class);
		} else if (config.getGraphType() == StateFlowGraphType.DB_IN_RAM_PATH) {
			bind(StateFlowGraph.class).to(Neo4jBackedStateFlowGraphInMemoryPathFinder.class);
		} else if (config.getGraphType() == StateFlowGraphType.DB_NO_RAM) {
			bind(StateFlowGraph.class).to(GraphDBStateFlowGraphNoRam.class);
		}
	}

	@BindingAnnotation
	@Target({ FIELD, PARAMETER, METHOD })
	@Retention(RUNTIME)
	public @interface BaseUrl {
	}

}

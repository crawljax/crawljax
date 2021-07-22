package com.crawljax.core;

import com.codahale.metrics.MetricRegistry;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.crawljax.fragmentation.FragmentManager;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * A context for each {@link Crawler} that can be handed to a {@link Plugin}.
 */
public class CrawlerContext {

	private EmbeddedBrowser browser;
	private final Provider<CrawlSession> sessionProvider;
	private final CrawljaxConfiguration config;
	private final ExitNotifier exitNotifier;
	private final MetricRegistry registry;

	private StateMachine stateMachine;
	private CrawlPath crawlpath;
	private FragmentManager fragmentManager;

	public void setFragmentManager(FragmentManager fragmentManager) {
		this.fragmentManager = fragmentManager;
	}

	@Inject
	public CrawlerContext(EmbeddedBrowser browser,
			CrawljaxConfiguration config,
			Provider<CrawlSession> sessionProvider, ExitNotifier exitNotifier,
			MetricRegistry registry) {
		this.browser = browser;
		this.config = config;
		this.sessionProvider = sessionProvider;
		this.exitNotifier = exitNotifier;
		this.registry = registry;
		this.fragmentManager  = null;
	}

	/**
	 * @return The browser of the current session. If you have configured
	 * multiple browsers this will return the {@link EmbeddedBrowser}
	 * that caused the given {@link Plugin} to fire.
	 */
	public EmbeddedBrowser getBrowser() {
		return browser;
	}
	
	public EmbeddedBrowser updateBrowser(EmbeddedBrowser browser) {
		this.browser = browser;
		return browser;
	}

	/**
	 * @return The {@link CrawlSession}
	 */
	public CrawlSession getSession() {
		return sessionProvider.get();
	}

	/**
	 * @return The {@link CrawljaxConfiguration} for this crawl.
	 */
	public CrawljaxConfiguration getConfig() {
		return config;
	}

	void setStateMachine(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	/**
	 * Tells Crawljax to stop with Exit status {@link ExitStatus#STOPPED}. This
	 * is equivalent to calling {@link CrawljaxRunner#stop()}.
	 */
	public void stop() {
		exitNotifier.stop();
	}

	StateMachine getStateMachine() {
		return stateMachine;
	}
	
	/**
	 * @return The current {@link StateVertex} or <code>null</code> when the
	 * {@link Crawler} isn't initialized yet.
	 */
	public StateVertex getCurrentState() {
		if (stateMachine == null) {
			return null;
		} else {
			return stateMachine.getCurrentState();
		}
	}

	public MetricRegistry getRegistry() {
		return registry;
	}

	/**
	 * @param path The current {@link CrawlPath} in this context.
	 */
	protected void setCrawlPath(CrawlPath path) {
		crawlpath = path;
	}

	/**
	 * @return The current {@link CrawlPath} in this context.
	 */
	public CrawlPath getCrawlPath() {
		return crawlpath.immutableCopy();
	}

	public FragmentManager getFragmentManager() {
		return this.fragmentManager;
	}

}

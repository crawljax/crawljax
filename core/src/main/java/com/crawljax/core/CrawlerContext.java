package com.crawljax.core;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;

/**
 * A context for each {@link Crawler} that can be handed to a {@link Plugin}.
 */
public class CrawlerContext {

	private final EmbeddedBrowser browser;
	private final Provider<CrawlSession> sessionProvider;
	private final CrawljaxConfiguration config;
	private StateMachine stateMachine;

	@Inject
	public CrawlerContext(EmbeddedBrowser browser,
	        CrawljaxConfiguration config, Provider<CrawlSession> sessionProvider) {
		this.browser = browser;
		this.config = config;
		this.sessionProvider = sessionProvider;
	}

	public EmbeddedBrowser getBrowser() {
		return browser;
	}

	public CrawlSession getSession() {
		return sessionProvider.get();
	}

	public CrawljaxConfiguration getConfig() {
		return config;
	}

	void setStateMachine(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	/**
	 * @return The curren t {@link StateVertex} or <code>null</code> when the {@link Crawler} isn't
	 *         initialized yet.
	 */
	public StateVertex getCurrentState() {
		if (stateMachine == null) {
			return null;
		} else {
			return stateMachine.getCurrentState();
		}
	}

}

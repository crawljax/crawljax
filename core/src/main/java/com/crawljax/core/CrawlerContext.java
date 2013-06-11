package com.crawljax.core;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.ExitNotifier.ExitStatus;
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
	private final ExitNotifier exitNotifier;

	private StateMachine stateMachine;

	@Inject
	public CrawlerContext(EmbeddedBrowser browser,
	        CrawljaxConfiguration config, Provider<CrawlSession> sessionProvider,
	        ExitNotifier exitNotifier) {
		this.browser = browser;
		this.config = config;
		this.sessionProvider = sessionProvider;
		this.exitNotifier = exitNotifier;
	}

	/**
	 * @return The browser of the current session. If you have configured multiple browsers this
	 *         will return the {@link EmbeddedBrowser} that caused the given {@link Plugin} to fire.
	 */
	public EmbeddedBrowser getBrowser() {
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
	 * Tells Crawljax to stop with Exit status {@link ExitStatus#STOPPED}. This is equivalent to
	 * calling {@link CrawljaxRunner#stop()}.
	 */
	public void stop() {
		exitNotifier.stop();
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

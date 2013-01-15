package com.crawljax.core.plugin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.ProxyConfiguration;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.StateMachine;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.Lists;

/**
 * Class for invoking plugins. The methods in this class are invoked from the Crawljax Core.
 */
public final class CrawljaxPluginsUtil {

	/**
	 * Make a new Log4j object used to do the logging.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CrawljaxPluginsUtil.class
	        .getName());
	private static final List<Plugin> PLUGINS = Lists.newArrayList();

	/**
	 * Non instanceable constructor; does nothing never used, this constructor prevents the
	 * CrawljaxPluginsUtil to be instantiated as a Object. All methods of this class must be used
	 * statically.
	 * 
	 * @throws CrawljaxException
	 *             this exception is always thrown when instanced.
	 */
	private CrawljaxPluginsUtil() throws CrawljaxException {
		LOGGER.error("As this contructor is private and never used interal "
		        + "in the CrawljaxPluginsUtil, this message may never appear");
		throw new CrawljaxException("Called private never used contructor CrawljaxPluginsUtil()");
	}

	/**
	 * Set the Plugins, first removes all the currently loaded plugins and add the plugins supplied.
	 * 
	 * @param plugins
	 *            the list of plugins, if plugins is null no plugins are added.
	 */
	public static void loadPlugins(List<Plugin> plugins) {
		PLUGINS.clear();
		if (plugins == null || plugins.size() == 0) {
			LOGGER.warn("No plugins loaded because CrawljaxConfiguration is empty");
			return;
		}
		CrawljaxPluginsUtil.PLUGINS.addAll(plugins);
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			/**
			 * Log the name of the plugin loaded
			 */
			LOGGER.info(plugin.getClass().getName());
		}
	}

	/**
	 * load and run the PreCrawlingPlugins. PreCrawlingPlugins are plugins that are ran before the
	 * crawling starts and before the initial url has been loaded. This kind of plugins can be used
	 * to do for example 'once in a crawlsession' operations like logging in or reset the database
	 * to a 'clean' state. The argument offered to the Plugin is a the current running instance of
	 * EmbeddedBrowser. Warning the instance of the browser offered is not a clone but the current
	 * and after wards used browser instance, changes and operations may cause 'strange' behaviour.
	 * 
	 * @see EmbeddedBrowser
	 * @param browser
	 *            the browser instance to load to the plugin.
	 */
	public static void runPreCrawlingPlugins(EmbeddedBrowser browser) {
		LOGGER.info("Running PreCrawlingPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof PreCrawlingPlugin) {
				((PreCrawlingPlugin) plugin).preCrawling(browser);
			}
		}
	}

	/**
	 * load and run the OnUrlLoadPlugins. The OnURLloadPlugins are run just after the Browser has
	 * gone to the initial url. Not only the first time but also every time the Core navigates back.
	 * Warning the instance of the browser offered is not a clone but the current and after wards
	 * used browser instance, changes and operations may cause 'strange' behaviour.
	 * 
	 * @param browser
	 *            the embedded browser instance to load in the plugin.
	 */
	public static void runOnUrlLoadPlugins(EmbeddedBrowser browser) {
		LOGGER.info("Running OnUrlLoadPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof OnUrlLoadPlugin) {
				((OnUrlLoadPlugin) plugin).onUrlLoad(browser);
			}
		}
	}

	/**
	 * load and run the OnNewStatePlugins. OnNewStatePlugins are plugins that are ran when a new
	 * state was found. This also happens for the Index State. Warning the session is not a clone,
	 * chaning the session can cause strange behaviour of Crawljax.
	 * 
	 * @param session
	 *            the session to load in the plugin
	 */
	public static void runOnNewStatePlugins(CrawlSession session) {
		LOGGER.info("Running OnNewStatePlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof OnNewStatePlugin) {
				((OnNewStatePlugin) plugin).onNewState(session);
			}
		}
	}

	/**
	 * Run the OnInvariantViolation plugins when an Invariant is violated. Invariant are checked
	 * when the state machine is updated that is when the dom is changed after a click on a
	 * clickable. When a invariant fails this kind of plugins are executed. Warning the session is
	 * not a clone, chaning the session can cause strange behaviour of Crawljax.
	 * 
	 * @param invariant
	 *            the failed invariants
	 * @param session
	 *            the session to load in the plugin
	 */
	public static void runOnInvriantViolationPlugins(Invariant invariant, CrawlSession session) {
		LOGGER.info("Running OnInvriantViolationPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof OnInvariantViolationPlugin) {
				((OnInvariantViolationPlugin) plugin).onInvariantViolation(invariant, session);
			}
		}
	}

	/**
	 * load and run the postCrawlingPlugins. PostCrawlingPlugins are executed after the crawling is
	 * finished Warning: changing the session can change the behavior of other post crawl plugins.
	 * It is not a clone!
	 * 
	 * @param session
	 *            the session to load in the plugin
	 */
	public static void runPostCrawlingPlugins(CrawlSession session) {
		LOGGER.info("Running PostCrawlingPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof PostCrawlingPlugin) {
				((PostCrawlingPlugin) plugin).postCrawling(session);
			}
		}
	}

	/**
	 * load and run the onRevisitStateValidator. As a difference to other SessionPlugins this plugin
	 * needs an explicit current state because the session.getCurrentState() does not contain the
	 * correct current state because we are in back-tracking
	 * 
	 * @param session
	 *            the session to load in the plugin
	 * @param currentState
	 *            the state the 'back tracking' operation is currently in
	 */
	public static void runOnRevisitStatePlugins(CrawlSession session, StateVertex currentState) {
		LOGGER.info("Running OnRevisitStatePlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof OnRevisitStatePlugin) {
				LOGGER.info("Calling plugin " + plugin.getClass().getName());
				((OnRevisitStatePlugin) plugin).onRevisitState(session, currentState);
			}
		}
	}

	/**
	 * load and run the PreStateCrawlingPlugins. Method that is called before the current state is
	 * crawled (before firing events on the current DOM state). Example: filter candidate elements.
	 * Warning the session and candidateElements are not clones, changes will result in changed
	 * behaviour.
	 * 
	 * @param session
	 *            the crawl session.
	 * @param candidateElements
	 *            the elements which crawljax is about to crawl
	 */
	public static void runPreStateCrawlingPlugins(CrawlSession session,
	        List<CandidateElement> candidateElements) {
		LOGGER.info("Running PreStateCrawlingPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof PreStateCrawlingPlugin) {
				LOGGER.info("Calling plugin " + plugin.getClass().getName());
				((PreStateCrawlingPlugin) plugin).preStateCrawling(session, candidateElements);
			}
		}
	}

	/**
	 * Load and run the proxyServerPlugins. proxyServerPlugins are used to Starts the proxy server
	 * and provides Crawljax with the correct settings such as port number. Warning the config
	 * argument is not a clone, changes will influence the behaviour of the Browser. Changes should
	 * be returned as new Object.
	 * 
	 * @param config
	 *            The ProxyConfiguration to use.
	 */
	public static void runProxyServerPlugins(ProxyConfiguration config) {
		LOGGER.info("Running ProxyServerPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof ProxyServerPlugin) {
				LOGGER.info("Calling plugin " + plugin.getClass().getName());
				((ProxyServerPlugin) plugin).proxyServer(config);
			}
		}
	}

	/**
	 * Load and run the guidedCrawlingPlugins. guidedServerPlugins are used to have control of the
	 * crawling on certain states.
	 * 
	 * @param controller
	 *            the crawljax controller instance.
	 * @param session
	 *            the current crawl session.
	 * @param exactEventPaths
	 *            the exact crawled event paths. Used to bring the browser back to the state the
	 *            crawler was before guided crawling.
	 * @param stateMachine
	 *            the state machine.
	 */
	public static void runGuidedCrawlingPlugins(CrawljaxController controller,
	        CrawlSession session, final List<Eventable> exactEventPaths,
	        final StateMachine stateMachine) {
		LOGGER.info("Running GuidedCrawlingPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof GuidedCrawlingPlugin) {
				LOGGER.info("Calling plugin " + plugin.getClass().getName());
				StateVertex currentState = session.getCurrentState();
				((GuidedCrawlingPlugin) plugin).guidedCrawling(currentState, controller, session,
				        exactEventPaths, stateMachine);
			}
		}
	}

	/**
	 * Load and run the OnFireEventFailedPlugins, this call has been made from the fireEvent when
	 * the event is not fireable. the Path is the Path leading TO this eventable (not included).
	 * 
	 * @param eventable
	 *            the eventable not able to fire.
	 * @param path
	 *            the path TO this eventable.
	 */
	public static void runOnFireEventFailedPlugins(Eventable eventable, List<Eventable> path) {
		LOGGER.info("Running OnFireEventFailedPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof OnFireEventFailedPlugin) {
				LOGGER.info("Calling plugin " + plugin.getClass().getName());
				((OnFireEventFailedPlugin) plugin).onFireEventFailed(eventable, path);
			}
		}
	}

	/**
	 * Load and run the OnBrowserCreatedPlugins, this call has been made from the browserpool when a
	 * new browser has been created and ready to be used by the Crawler. The PreCrawling plugins are
	 * executed before these plugins are executed except that the precrawling plugins are only
	 * executed on the first created browser.
	 * 
	 * @param newBrowser
	 *            the new created browser object
	 */
	public static void runOnBrowserCreatedPlugins(EmbeddedBrowser newBrowser) {
		LOGGER.info("Running OnBrowserCreatedPlugins...");
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof OnBrowserCreatedPlugin) {
				LOGGER.info("Calling plugin " + plugin.getClass().getName());
				((OnBrowserCreatedPlugin) plugin).onBrowserCreated(newBrowser);
			}
		}
	}

	/**
	 * Load and run the DomChangeNotifierPlugin.
	 * 
	 * 
	 * 
	 */
	public static boolean runDomChangeNotifierPlugins(final StateVertex stateBefore,
	        final Eventable e, final StateVertex stateAfter, final EmbeddedBrowser browser) {
		LOGGER.info("Checking for DomChangeNotifierPlugin...");
		Plugin latest = null;
		for (Plugin plugin : CrawljaxPluginsUtil.PLUGINS) {
			if (plugin instanceof DomChangeNotifierPlugin) {
				LOGGER.info("Found plugin " + plugin.getClass().getName());
				latest = plugin;
			}
		}

		if (latest != null) {
			LOGGER.debug("Calling plugin ", latest.getClass().getName());
			return ((DomChangeNotifierPlugin) latest).isDomChanged(stateBefore.getDom(), e,
			        stateAfter.getDom(), browser);
		}

		LOGGER.debug("No DomChangeNotifierPlugin found. Performing default DOM comparison...");

		// default DOM comparison behavior
		boolean isChanged = !stateAfter.equals(stateBefore);

		if (isChanged) {
			LOGGER.info("Dom is Changed!");
		} else {
			LOGGER.debug("Dom not Changed!");
		}

		return isChanged;

	}

}

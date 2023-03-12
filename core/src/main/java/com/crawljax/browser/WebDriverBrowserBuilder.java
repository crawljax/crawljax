package com.crawljax.browser;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ProxyConfiguration.ProxyType;
import com.crawljax.core.plugin.Plugins;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import javax.inject.Inject;
import javax.inject.Provider;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the EmbeddedBrowserBuilder based on Selenium WebDriver API.
 */
public class WebDriverBrowserBuilder implements Provider<EmbeddedBrowser> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverBrowserBuilder.class);
    private static final boolean SYSTEM_OFFLINE = false;
    private final CrawljaxConfiguration configuration;
    private final Plugins plugins;

    @Inject
    public WebDriverBrowserBuilder(CrawljaxConfiguration configuration, Plugins plugins) {
        this.configuration = configuration;
        this.plugins = plugins;
    }

    /**
     * Build a new WebDriver based EmbeddedBrowser.
     *
     * @return the new build WebDriver based embeddedBrowser
     */
    @Override
    public EmbeddedBrowser get() {
        LOGGER.debug("Setting up a Browser");
        // Retrieve the config values used
        ImmutableSortedSet<String> filterAttributes =
                configuration.getCrawlRules().getPreCrawlConfig().getFilterAttributeNames();
        long crawlWaitReload = configuration.getCrawlRules().getWaitAfterReloadUrl();
        long crawlWaitEvent = configuration.getCrawlRules().getWaitAfterEvent();

        // Determine the requested browser type
        EmbeddedBrowser browser = null;
        EmbeddedBrowser.BrowserType browserType =
                configuration.getBrowserConfig().getBrowserType();
        try {
            switch (browserType) {
                case CHROME:
                    browser = newChromeBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent, false);
                    break;
                case CHROME_HEADLESS:
                    browser = newChromeBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent, true);
                    break;
                case FIREFOX:
                    browser = newFirefoxBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent, false);
                    break;
                case FIREFOX_HEADLESS:
                    browser = newFirefoxBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent, true);
                    break;
                case EDGE:
                    browser = newEdgeBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent, false);
                    break;
                case EDGE_HEADLESS:
                    browser = newEdgeBrowser(filterAttributes, crawlWaitReload, crawlWaitEvent, true);
                    break;
                case REMOTE:
                    browser = WebDriverBackedEmbeddedBrowser.withRemoteDriver(
                            configuration.getBrowserConfig().getRemoteHubUrl(),
                            filterAttributes,
                            crawlWaitEvent,
                            crawlWaitReload,
                            configuration.getBrowserConfig().getDesiredCapabilities());
                    break;
                default:
                    throw new IllegalStateException("Unrecognized browser type "
                            + configuration.getBrowserConfig().getBrowserType());
            }
        } catch (IllegalStateException e) {
            LOGGER.error("Crawling with {} failed: {}", browserType, e.getMessage());
            throw e;
        }

        /* for Retina display. */
        if (browser instanceof WebDriverBackedEmbeddedBrowser) {
            int pixelDensity =
                    this.configuration.getBrowserConfig().getBrowserOptions().getPixelDensity();
            if (pixelDensity != -1) {
                ((WebDriverBackedEmbeddedBrowser) browser).setPixelDensity(pixelDensity);
            }

            boolean USE_CDP =
                    this.configuration.getBrowserConfig().getBrowserOptions().isUSE_CDP();

            if ((browserType == EmbeddedBrowser.BrowserType.CHROME_HEADLESS
                            || browserType == EmbeddedBrowser.BrowserType.CHROME)
                    && USE_CDP) {
                ((WebDriverBackedEmbeddedBrowser) browser).setUSE_CDP(true);
            }
        }

        plugins.runOnBrowserCreatedPlugins(browser);
        return browser;
    }

    private EmbeddedBrowser newEdgeBrowser(
            ImmutableSortedSet<String> filterAttributes, long crawlWaitReload, long crawlWaitEvent, boolean headless) {
        EdgeOptions edgeOptions = new EdgeOptions();

        if (headless) {
            edgeOptions.addArguments("headless");
            edgeOptions.addArguments("disable-gpu");
        }

        EdgeDriver driver = (EdgeDriver)
                WebDriverManager.edgedriver().capabilities(edgeOptions).create();

        return WebDriverBackedEmbeddedBrowser.withDriver(driver, filterAttributes, crawlWaitReload, crawlWaitEvent);
    }

    private EmbeddedBrowser newFirefoxBrowser(
            ImmutableSortedSet<String> filterAttributes, long crawlWaitReload, long crawlWaitEvent, boolean headless) {

        FirefoxProfile profile = null;

        if (configuration.getBrowserConfig().getBrowserOptions().getProfile() != null) {
            profile = configuration.getBrowserConfig().getBrowserOptions().getProfile();
        } else {
            profile = new FirefoxProfile();

            // disable download dialog (downloads directly without the need for a confirmation)
            profile.setPreference("browser.contentblocking.category", "strict");
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            // profile.setPreference("browser.download.dir","downloads");
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv, application/octet-stream");

            // REMOVE once CDP support has been removed:
            profile.setPreference("fission.bfcacheInParent.enabled", false);
            profile.setPreference("fission.bfcacheInParent", false);
            profile.setPreference("fission.webContentIsolationStrategy", 0);
        }

        if (configuration.getProxyConfiguration() != null) {
            String lang = configuration.getBrowserConfig().getLangOrNull();
            if (!Strings.isNullOrEmpty(lang)) {
                profile.setPreference("intl.accept_languages", lang);
            }

            profile.setPreference(
                    "network.proxy.http", configuration.getProxyConfiguration().getHostname());
            profile.setPreference(
                    "network.proxy.http_port",
                    configuration.getProxyConfiguration().getPort());
            profile.setPreference(
                    "network.proxy.type",
                    configuration.getProxyConfiguration().getType().toInt());
            /* use proxy for everything, including localhost */
            profile.setPreference("network.proxy.no_proxies_on", "");
        }

        FirefoxOptions firefoxOptions = new FirefoxOptions();
        // firefoxOptions.setCapability("marionette", true);
        firefoxOptions.setProfile(profile);

        /* for headless Firefox. */
        if (headless) {
            firefoxOptions.addArguments("--headless");
        }

        WebDriverManager manager = WebDriverManager.firefoxdriver();
        manager.capabilities(firefoxOptions);
        FirefoxDriver driver = (FirefoxDriver) manager.create();

        return WebDriverBackedEmbeddedBrowser.withDriver(driver, filterAttributes, crawlWaitReload, crawlWaitEvent);
    }

    private EmbeddedBrowser newChromeBrowser(
            ImmutableSortedSet<String> filterAttributes, long crawlWaitReload, long crawlWaitEvent, boolean headless) {

        ChromeOptions optionsChrome = new ChromeOptions();

        /* enables headless Chrome. */
        if (headless) {
            optionsChrome.addArguments("--headless");
        }

        if (configuration.getProxyConfiguration() != null
                && configuration.getProxyConfiguration().getType() != ProxyType.NOTHING) {

            String lang = configuration.getBrowserConfig().getLangOrNull();
            if (!Strings.isNullOrEmpty(lang)) {
                optionsChrome.addArguments("--lang=" + lang);
            }
            optionsChrome.addArguments("--proxy-server=http://"
                    + configuration.getProxyConfiguration().getHostname() + ":"
                    + configuration.getProxyConfiguration().getPort());
        }

        // Issue 587 fix for Chrome 111
        optionsChrome.addArguments("--remote-allow-origins=*");

        WebDriverManager manager = WebDriverManager.getInstance(DriverManagerType.CHROME);
        manager.capabilities(optionsChrome);
        ChromeDriver driverChrome = (ChromeDriver) manager.create();

        Dimension d = new Dimension(1200, 890);
        // Resize current window to the set dimension
        driverChrome.manage().window().setSize(d);
        return WebDriverBackedEmbeddedBrowser.withDriver(
                driverChrome, filterAttributes, crawlWaitEvent, crawlWaitReload);
    }
}

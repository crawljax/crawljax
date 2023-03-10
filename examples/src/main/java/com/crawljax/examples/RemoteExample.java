package com.crawljax.examples;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.crawljax.plugins.testcasegenerator.TestConfiguration;
import com.crawljax.plugins.testcasegenerator.TestSuiteGenerator;
import com.crawljax.plugins.testcasegenerator.TestConfiguration.StateEquivalenceAssertionMode;

public class RemoteExample {

    private static final long WAIT_TIME_AFTER_EVENT = 200;
    private static final long WAIT_TIME_AFTER_RELOAD = 200;

    /**
     * Run this method to start the crawl.
     */
    public static void main(String[] args) {
        String remoteUrl = "http://localhost:4444/wd/hub";
        int numberOfBrowsers = 1;
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("http://demo.crawljax.com/");
        builder.crawlRules().setFormFillMode(FormFillMode.NORMAL);

        builder.crawlRules().clickDefaultElements();

        builder.setMaximumDepth(3);
        builder.setMaximumRunTime(10, TimeUnit.MINUTES);

        builder.crawlRules().clickElementsInRandomOrder(false);
        builder.crawlRules().crawlHiddenAnchors(true);
        builder.crawlRules().crawlFrames(false);

        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(Browser.CHROME.browserName());
        builder.setBrowserConfig(BrowserConfiguration.remoteConfig(numberOfBrowsers, remoteUrl, desiredCapabilities));
        /* plugins. */
        builder.addPlugin(new CrawlOverview());
        builder.addPlugin(new TestSuiteGenerator(new TestConfiguration(StateEquivalenceAssertionMode.BOTH)));

        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        crawljax.call();
    }
}

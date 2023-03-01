package com.crawljax.examples;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.crawljax.plugins.testcasegenerator.TestConfiguration;
import com.crawljax.plugins.testcasegenerator.TestSuiteGenerator;
import java.util.concurrent.TimeUnit;

public class ClickableDetectorExample {

    private static final long WAIT_TIME_AFTER_EVENT = 200;
    private static final long WAIT_TIME_AFTER_RELOAD = 200;

    public static void main(String[] args) {

        String appURL = "http://dictionary.com";

        // Pixel density for your screen can be found by using
        // the command "devicePixelRatio" in Chrome browser console.
        // int pixelDensity = BrowserOptions.MACBOOK_PRO_RETINA_PIXEL_DENSITY;

        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(appURL);
        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.RANDOM);
        builder.setUnlimitedCrawlDepth();
        builder.setMaximumRunTime(10, TimeUnit.MINUTES);
        builder.crawlRules().clickElementsInRandomOrder(false);
        builder.crawlRules().crawlHiddenAnchors(true);
        builder.crawlRules().crawlFrames(false);
        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

        // configuration to activate clickable detection
        builder.crawlRules().clickElementsWithClickEventHandler();

        // browserOptions.setPixelDensity(pixelDensity);
        // BrowserOptions.USE_CDP should be set to true

        BrowserConfiguration browserConfiguration =
                new BrowserConfiguration(EmbeddedBrowser.BrowserType.CHROME_HEADLESS, 1, new BrowserOptions(true));

        builder.setBrowserConfig(browserConfiguration);

        /* plugins. */
        builder.addPlugin(new CrawlOverview());
        builder.addPlugin(
                new TestSuiteGenerator(new TestConfiguration(TestConfiguration.StateEquivalenceAssertionMode.FRAG)));

        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        crawljax.call();
    }
}

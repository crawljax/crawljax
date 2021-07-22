package com.crawljax.examples;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.crawljax.plugins.testcasegenerator.TestConfiguration;
import com.crawljax.plugins.testcasegenerator.TestConfiguration.StateEquivalenceAssertionMode;
import com.crawljax.stateabstractions.visual.imagehashes.DHashStateVertexFactory;
import com.crawljax.plugins.testcasegenerator.TestSuiteGenerator;

import java.util.concurrent.TimeUnit;

public final class VisualCrawlExample {

    private static final long WAIT_TIME_AFTER_EVENT = 200;
    private static final long WAIT_TIME_AFTER_RELOAD = 200;

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Provide the URL of the web app as the first CLI argument.");
            return;
        }

        String appURL = args[0];
        if (!appURL.startsWith("http://") && !appURL.startsWith("https://")) {
            appURL = "http://" + appURL;
        }

        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(appURL);
        builder.crawlRules().setFormFillMode(FormFillMode.NORMAL);
        builder.setStateVertexFactory(new DHashStateVertexFactory());

        builder.crawlRules().clickDefaultElements();

        //builder.setMaximumStates(10);
        builder.setMaximumDepth(3);
        builder.setMaximumRunTime(10, TimeUnit.MINUTES);

        builder.crawlRules().clickElementsInRandomOrder(false);
        builder.crawlRules().crawlHiddenAnchors(true);
        builder.crawlRules().crawlFrames(false);

        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);
        BrowserConfiguration browserConfiguration;
        if (args.length == 2 && "retina".equalsIgnoreCase(args[1])) {
            browserConfiguration = new BrowserConfiguration(BrowserType.CHROME, 1,
                    new BrowserOptions(false, BrowserOptions.MACBOOK_PRO_RETINA_PIXEL_DENSITY));
        } else {
            browserConfiguration = new BrowserConfiguration(BrowserType.CHROME, 1);
        }
        builder.setBrowserConfig(browserConfiguration);

        /* plugins. */
        builder.addPlugin(new CrawlOverview());
        builder.addPlugin(new TestSuiteGenerator(new TestConfiguration(StateEquivalenceAssertionMode.BOTH)));

        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        crawljax.call();

    }

}

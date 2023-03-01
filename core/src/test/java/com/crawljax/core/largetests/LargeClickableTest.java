package com.crawljax.core.largetests;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.BrowserTest;
import java.util.concurrent.TimeUnit;
import org.junit.experimental.categories.Category;

@Category(BrowserTest.class)
public class LargeClickableTest extends LargeTestBase {

    static void addCrawlElements(CrawljaxConfiguration.CrawljaxConfigurationBuilder builder) {
        CrawlRules.CrawlRulesBuilder rules = builder.crawlRules();
        rules.clickElementsWithClickEventHandler();

        rules.dontClick("button").withText("DONT_CLICK_ME_BECAUSE_OF_CONDITION");
        rules.dontClick("div").withText("DONT_CLICK_ME_BECAUSE_I_AM_A_NORMALE_DIV");
        rules.dontClick("a").withText(DONT_CLICK_TEXT);
        rules.dontClick("div").withText(DONT_CLICK_TEXT);
        rules.dontClick("a").withAttribute(ATTRIBUTE, DONT_CLICK_TEXT);
        rules.dontClick("a").underXPath("//DIV[@id='" + DONT_CLICK_UNDER_XPATH_ID + "']");
    }

    @Override
    BrowserConfiguration getBrowserConfiguration() {
        BrowserOptions browserOptions = new BrowserOptions();
        browserOptions.setUSE_CDP(true);
        return new BrowserConfiguration(BrowserType.CHROME_HEADLESS, 1, browserOptions);
    }

    @Override
    long getTimeOutAfterReloadUrl() {
        return 100;
    }

    @Override
    long getTimeOutAfterEvent() {
        return 100;
    }

    @Override
    protected CrawljaxConfiguration getCrawljaxConfiguration() {

        CrawljaxConfiguration.CrawljaxConfigurationBuilder builder =
                CrawljaxConfiguration.builderFor(WEB_SERVER.getSiteUrl());
        builder.crawlRules().waitAfterEvent(getTimeOutAfterEvent(), TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterReloadUrl(getTimeOutAfterReloadUrl(), TimeUnit.MILLISECONDS);
        builder.setMaximumDepth(3);
        builder.crawlRules().clickOnce(true);

        builder.setBrowserConfig(getBrowserConfiguration());

        addCrawlElements(builder);

        builder.crawlRules().setInputSpec(getInputSpecification());
        builder.crawlRules().setFormFillMode(CrawlRules.FormFillMode.RANDOM);

        addCrawlConditions(builder);
        addOracleComparators(builder);
        addInvariants(builder);
        addWaitConditions(builder);
        addPlugins(builder);

        return builder.build();
    }
}

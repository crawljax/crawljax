package com.crawljax.examples;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawlRules.FormFillMode;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.OnBrowserCreatedPlugin;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.crawljax.util.FSUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v108.network.Network;

/**
 * Example of running Crawljax on Google's Crawl Maze. Default output dir is "out".
 */
public final class CrawlMazeExample {

    private static final long WAIT_TIME_AFTER_EVENT = 2000;
    private static final long WAIT_TIME_AFTER_RELOAD = 2000;
    private static final String URL = "https://security-crawl-maze.app/";

    /**
     * Run this method to start the crawl.
     *
     * @throws IOException when the output folder cannot be created or emptied.
     */
    public static void main(String[] args) throws IOException {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(URL);

        builder.crawlRules().setFormFillMode(FormFillMode.RANDOM);
        // builder.setStateVertexFactory(new FragGenStateVertexFactory(0, builder, true));

        builder.crawlRules().crawlHiddenAnchors(true);
        builder.crawlRules().crawlFrames(true);
        builder.setUnlimitedCrawlDepth();
        builder.setUnlimitedRuntime();
        builder.setUnlimitedStates();

        builder.crawlRules().clickElementsInRandomOrder(false);

        // Set timeouts
        builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
        builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

        builder.crawlRules().clickElementsWithClickEventHandler();

        builder.setBrowserConfig(new BrowserConfiguration(BrowserType.CHROME_HEADLESS, 1, new BrowserOptions(true)));

        // CrawlOverview
        builder.addPlugin(new CrawlOverview());

        Set<String> urls = new TreeSet<>();

        builder.addPlugin((OnBrowserCreatedPlugin) (browser) -> {
            DevTools devTools = ((ChromeDriver) browser.getWebDriver()).getDevTools();
            devTools.createSession();
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
            devTools.addListener(Network.requestWillBeSent(), request -> {
                try {
                    System.out.println(
                            "New network request: " + request.getRequest().getUrl());
                    urls.add(new URL(request.getRequest().getUrl()).getPath());
                } catch (MalformedURLException e) {
                    System.err.println(e.getMessage());
                }
            });
        });

        builder.addPlugin((OnNewStatePlugin) (context, state) -> {
            System.out.println(" new state with url: " + state.getUrl());
            try {
                urls.add(new URL(state.getUrl()).getPath());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        builder.addPlugin((PostCrawlingPlugin) (context, session) -> {
            try {
                String folderName = "out/crawlmaze/";
                FSUtils.directoryCheck(folderName);
                FileWriter fileWriter = new FileWriter(
                        Paths.get(folderName + "detected-urls.json").toFile());
                gson.toJson(urls, fileWriter);
                fileWriter.flush();
                fileWriter.close();
                System.out.println("Total number of urls DETECTED: " + urls.size());

                String expectedUrls =
                        Resources.toString(Resources.getResource("expected-results.json"), Charsets.UTF_8);
                TreeSet<String> expectedSet =
                        gson.fromJson(expectedUrls, new TypeToken<TreeSet<String>>() {}.getType());
                System.out.println("Total number of urls EXPECTED: " + expectedSet.size());
                Set<String> intersection = new TreeSet<>(expectedSet);
                intersection.retainAll(urls);

                System.out.println("intersection size: " + intersection.size());
                System.out.println("intersection: " + intersection);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        crawljax.call();
    }
}

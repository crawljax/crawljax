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
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v112.network.Network;

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

        Set<String> detectedUrls = new TreeSet<>();

        builder.addPlugin((OnBrowserCreatedPlugin) (browser) -> {
            DevTools devTools = ((ChromeDriver) browser.getWebDriver()).getDevTools();
            devTools.createSession();
            devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
            devTools.addListener(Network.requestWillBeSent(), request -> {
                try {
                    System.out.println(
                            "New network request: " + request.getRequest().getUrl());
                    detectedUrls.add(new URL(request.getRequest().getUrl()).getPath());
                } catch (MalformedURLException e) {
                    System.err.println(e.getMessage());
                }
            });
        });

        builder.addPlugin((OnNewStatePlugin) (context, state) -> {
            System.out.println("New state with url: " + state.getUrl());
            try {
                detectedUrls.add(new URL(state.getUrl()).getPath());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        builder.addPlugin((PostCrawlingPlugin) (context, session) -> {
            try {
                String folderName = "out/crawlmaze/";
                FSUtils.directoryCheck(folderName);
                WriteJsonToFile(detectedUrls, gson, folderName + "all-detected-urls.json");
                System.out.println("Total number of urls DETECTED: " + detectedUrls.size());

                String expectedUrls =
                        Resources.toString(Resources.getResource("expected-results.json"), Charsets.UTF_8);
                TreeSet<String> expectedSet =
                        gson.fromJson(expectedUrls, new TypeToken<TreeSet<String>>() {}.getType());
                System.out.println("Total number of urls EXPECTED: " + expectedSet.size());
                Set<String> intersection = new TreeSet<>(expectedSet);
                intersection.retainAll(detectedUrls);

                System.out.println("Covered urls size: " + intersection.size());
                System.out.println("Covered urls: " + intersection);
                WriteJsonToFile(intersection, gson, folderName + "covered-urls.json");

                System.out.println("Detected URLs are saved in: " + folderName + "all-detected-urls.json");
                System.out.println("Covered URLs are saved in: " + folderName + "covered-urls.json");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
        crawljax.call();
    }

    private static void WriteJsonToFile(Set<String> urls, Gson gson, String filePath) throws IOException {
        Writer fileWriter = Files.newBufferedWriter(Paths.get(filePath).toFile().toPath(), Charset.defaultCharset());
        gson.toJson(urls, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }
}

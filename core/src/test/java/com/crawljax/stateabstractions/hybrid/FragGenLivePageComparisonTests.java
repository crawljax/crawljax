package com.crawljax.stateabstractions.hybrid;

import static com.google.common.base.Preconditions.checkArgument;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.fragmentation.FragmentationPlugin;
import com.crawljax.test.WarchiveServer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class FragGenLivePageComparisonTests {

    @Rule
    public final BrowserProvider provider = new BrowserProvider();

    WarchiveServer server;
    EmbeddedBrowser browser;

    File TEST_OUTPUT_DIR = new File("testOutput");

    @Before
    public void setup() {
        if (!TEST_OUTPUT_DIR.exists()) {
            boolean created = TEST_OUTPUT_DIR.mkdir();
            checkArgument(created, "Could not create testOutput dir");
        }

        browser = provider.newEmbeddedBrowser();
    }

    public URI getUrl(String pagePart) throws URISyntaxException {
        return new URI(
                server.getSiteUrl().getScheme(),
                server.getSiteUrl().getUserInfo(),
                server.getSiteUrl().getHost(),
                server.getSiteUrl().getPort(),
                pagePart,
                null,
                null);
    }

    public void startServer(String warchDir) {
        server = new WarchiveServer("src/test/resources/warchives/" + warchDir, 8080);
        new Thread(server).start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assert.assertTrue(
                "server not started properly",
                server.getSiteUrl().toString().equalsIgnoreCase("http://localhost:8080/"));
    }

    private FragGenStateVertexFactory factory;

    public StateVertex getState(int id, URI url, EmbeddedBrowser browser, FragmentManager manager) {
        browser.goToUrl(url);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        factory = new FragGenStateVertexFactory(0, CrawljaxConfiguration.builderFor(url), false);
        StateVertex state = factory.newStateVertex(
                id,
                browser.getCurrentUrl(),
                "state" + id,
                browser.getStrippedDom(),
                browser.getStrippedDomWithoutIframeContent(),
                browser);
        FragmentationPlugin.fragmentState(state, manager, browser, TEST_OUTPUT_DIR, true);
        return state;
    }

    @Test
    public void petclinicNDTests() throws URISyntaxException, InterruptedException {
        startServer("petclinic");
        URI url1 = getUrl("/replay/20230128041350/http://host.docker.internal:9966/petclinic/owners/2.html");
        URI url2 = getUrl("/replay/20230128041350/http://host.docker.internal:9966/petclinic/owners/10.html");

        FragmentManager fragmentManager = new FragmentManager(null);
        BrowserProvider provider = new BrowserProvider();

        StateVertex state1 = getState(0, url1, browser, fragmentManager);

        StateVertex state2 = getState(1, url2, browser, fragmentManager);

        StateComparision comp = fragmentManager.cacheStateComparision(state1, state2, true);

        //    showPicture(((HybridStateVertexImpl)state1).getImage(), ((HybridStateVertexImpl)state2).getImage());

        Assert.assertEquals(StateComparision.NEARDUPLICATE2, comp);
    }

    public void showPicture(BufferedImage state1Annotated, BufferedImage state1Annotated2) throws InterruptedException {

        JLabel picLabel = new JLabel(new ImageIcon(state1Annotated));
        JFrame frame = new JFrame();
        frame.add(picLabel);
        frame.setSize(1200, 890);
        frame.setVisible(true);

        JLabel picLabel2 = new JLabel(new ImageIcon(state1Annotated2));
        JFrame frame2 = new JFrame();
        frame2.add(picLabel2);
        frame2.setSize(1200, 890);
        frame2.setVisible(true);

        Object lock = new Object();
        Thread t = new Thread() {
            public void run() {
                synchronized (lock) {
                    while (frame.isVisible() && frame2.isVisible()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Closing!!");
                }
            }
        };
        t.start();

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent arg0) {
                synchronized (lock) {
                    frame.setVisible(false);
                    frame2.setVisible(false);

                    lock.notify();
                }
            }
        });

        t.join();
    }

    @After
    public void cleanup() {
        try {
            FileUtils.deleteDirectory(TEST_OUTPUT_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.stop();
        browser.close();
    }
}

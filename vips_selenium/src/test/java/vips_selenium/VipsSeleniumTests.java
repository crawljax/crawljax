package vips_selenium;

import static com.google.common.base.Preconditions.checkArgument;

import com.crawljax.vips_selenium.DomUtils;
import com.crawljax.vips_selenium.VipsRectangle;
import com.crawljax.vips_selenium.VipsSelenium;
import com.crawljax.vips_selenium.VipsUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.w3c.dom.Document;

public class VipsSeleniumTests {

    WarchiveServer server;

    WebDriver driver;

    File TEST_OUTPUT_DIR = new File("testOutput");

    @Before
    public void setup() {
        if (!TEST_OUTPUT_DIR.exists()) {
            boolean created = TEST_OUTPUT_DIR.mkdir();
            checkArgument(created, "Could not create testOutput dir");
        }

        WebDriverManager wdm = WebDriverManager.chromedriver();
        ChromeOptions optionsChrome = new ChromeOptions();
        optionsChrome.addArguments("--headless");
        optionsChrome.addArguments("--remote-allow-origins=*");
        wdm.capabilities(optionsChrome);
        driver = wdm.create();
    }

    private Document getDomTree(WebDriver driver) {
        try {
            Document dom = DomUtils.asDocument(driver.getPageSource());
            boolean offline = false;
            VipsUtils.cleanDom(dom, offline);
            return dom;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public BufferedImage getScreenShotAsBufferedImage() {
        ChromeDriver chromeDriver = (ChromeDriver) driver;
        long width = (long) chromeDriver.executeScript("return document.body.scrollWidth");
        long height = (long) chromeDriver.executeScript("return document.body.scrollHeight");
        long scale = (long) chromeDriver.executeScript("return window.devicePixelRatio");
        int intscale = (int) scale;

        HashMap<String, Object> setDeviceMetricsOverride = new HashMap<>();
        setDeviceMetricsOverride.put("deviceScaleFactor", scale);
        setDeviceMetricsOverride.put("mobile", false);
        setDeviceMetricsOverride.put("width", width);
        setDeviceMetricsOverride.put("height", height);
        chromeDriver.executeCdpCommand("Emulation.setDeviceMetricsOverride", setDeviceMetricsOverride);

        Map<String, Object> result = chromeDriver.executeCdpCommand("Page.captureScreenshot", new HashMap<>());
        String data = (String) result.get("data");
        byte[] image = Base64.getDecoder().decode((data));
        InputStream is = new ByteArrayInputStream(image);
        try {
            BufferedImage img = ImageIO.read(is);
            BufferedImage resizedImage = new BufferedImage(
                    img.getWidth() / intscale, img.getHeight() / intscale, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(img, 0, 0, img.getWidth() / intscale, img.getHeight() / intscale, Color.WHITE, null);
            g.dispose();
            return resizedImage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    @Test
    public void testOutput() throws URISyntaxException, MalformedURLException, InterruptedException {
        startServer("petclinic");
        URI url1 = getUrl("/replay/20230128041350/http://host.docker.internal:9966/petclinic/owners/2.html");

        driver.navigate().to(url1.toURL());
        Thread.sleep(2000);
        Document dom = getDomTree(driver);

        BufferedImage screenshot = getScreenShotAsBufferedImage();

        VipsSelenium vips = new VipsSelenium(driver, dom, screenshot, 10, TEST_OUTPUT_DIR, "state1", true, false);
        List<VipsRectangle> rectangles = vips.startSegmentation();

        // Number of fragments with the highest number of iterations
        Assert.assertEquals(41, rectangles.size());

        // No of vips-blocks in the root fragment
        Assert.assertEquals(53, rectangles.get(0).getNestedBlocks().size());

        vips = new VipsSelenium(driver, dom, screenshot, 4, TEST_OUTPUT_DIR, "state2", true, false);
        rectangles = vips.startSegmentation();

        // Number of fragments with 4 iterations
        Assert.assertEquals(38, rectangles.size());

        vips = new VipsSelenium(driver, dom, screenshot, 1, TEST_OUTPUT_DIR, "state3", true, false);
        rectangles = vips.startSegmentation();

        // Number of fragments with least number of iterations
        Assert.assertEquals(9, rectangles.size());
    }

    @After
    public void cleanup() {
        try {
            FileUtils.deleteDirectory(TEST_OUTPUT_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.stop();
        WebDriverManager.getInstance().quit();
    }
}

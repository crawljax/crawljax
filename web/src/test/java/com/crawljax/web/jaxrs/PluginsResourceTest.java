package com.crawljax.web.jaxrs;

import com.google.common.collect.Lists;
import com.thoughtworks.selenium.DefaultSelenium;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class PluginsResourceTest {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsResourceTest.class);
	private static Server server;
	private static DefaultSelenium selenium;
	private static WebDriver driver;

	private String pluginId = "crawloverview-plugin";
	private String pluginName = "Crawl Overview Plugin";

	@BeforeClass
	public static void setup() throws Exception {
		LOG.debug("Starting Jetty");
		server = new Server(0);

		String url = setupJetty();
		LOG.info("Jetty started on {}", url);
		driver = new FirefoxDriver();
		LOG.debug("Starting selenium");
		selenium = new WebDriverBackedSelenium(driver, url);

		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	private static String setupJetty() throws Exception {
		HandlerList list = new HandlerList();
		System.setProperty("outputFolder", System.getProperty("user.home") + File.separatorChar + "crawljax");
		list.addHandler(buildOutputContext(System.getProperty("outputFolder")));
		list.addHandler(buildWebAppContext());
		server.setHandler(list);
		server.start();
		int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		String url = "http://localhost:" + port;
		return url;
	}

	private static WebAppContext buildWebAppContext() throws Exception {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/");
		webAppContext.setWar(new File("src/main/webapp/").getAbsolutePath());
		return webAppContext;
	}

	private static WebAppContext buildOutputContext(String outputFolder) throws Exception {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/output");
		webAppContext.setWar(new File(outputFolder).getAbsolutePath());
		return webAppContext;
	}

	@Test
	public void addNewPlugin() {
		selenium.open("/#/plugins");

		final List<WebElement> existingPlugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + pluginName + "')\").toArray();");

		WebElement fileInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"input[type='file']\")[0];");
		fileInput.sendKeys(new File("src/test/resources/" + pluginId + ".jar").getAbsolutePath());

		List<WebElement> uploadLink = driver.findElements(By.linkText("Upload"));
		uploadLink.get(0).click();

		ExpectedCondition<Boolean> uploaded = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				List<WebElement> plugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
						"return $(\"td:contains('" + pluginName + "')\").toArray();");
				return plugins.size() > existingPlugins.size();
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(uploaded);

		List<WebElement> plugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + pluginName + "')\").toArray();");
		assertEquals(plugins.size(), existingPlugins.size() + 1);
	}

	private List<WebElement> visibleElementsByCss(String selector) {
		List<WebElement> elements = driver.findElements(By.cssSelector(selector));
		List<WebElement> visible = Lists.newArrayListWithExpectedSize(elements.size());
		for (WebElement webElement : elements) {
			if (webElement.isDisplayed()) {
				visible.add(webElement);
			}
		}
		return visible;
	}

	@AfterClass
	public static void tearDown() throws Exception {
		server.stop();
		selenium.stop();
	}
}

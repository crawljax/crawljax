package com.crawljax.web.jaxrs;

import com.crawljax.web.CrawljaxServer;
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

	public static class CrawljaxServerRunner implements Runnable {
		public void run() {
			try {
				CrawljaxServer.main(new String[]{"-p 0"});
			} catch (Exception e) {
				LOG.debug("Failed to run CrawljaxServer.\n{}", e.getStackTrace().toString());
			}
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsResourceTest.class);
	private static DefaultSelenium selenium;
	private static WebDriver driver;

	private String pluginId = "crawloverview-plugin";
	private String pluginName = "Crawl Overview Plugin";
	private String configurationName = "TestConfiguration";

	@BeforeClass
	public static void setup() throws Exception {

		CrawljaxServerRunner serverRunner = new CrawljaxServerRunner();
		Thread serverThread = new Thread(serverRunner);
		serverThread.setDaemon(true); //so that serverThread is terminated when test is complete.
		serverThread.start();

		int maxWait_s = 10, currentWait_s = 0;
		while(CrawljaxServer.url == null) {
			currentWait_s += 0.5;
			Thread.sleep(500);
			assertTrue(currentWait_s < maxWait_s);
		}

		driver = new FirefoxDriver();
		LOG.debug("Starting selenium");
		selenium = new WebDriverBackedSelenium(driver, CrawljaxServer.url);

		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
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

	@Test
	public void addPluginToConfiguration() {
		selenium.open("/#/configurations");
		List<WebElement> configurationLink = driver.findElements(By.linkText(configurationName));
		assertFalse(configurationLink.isEmpty());
		configurationLink.get(0).click();

		WebElement pluginsLink = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"a:contains('Overview')\").parent().find(\"~ li > a:contains('Plugins')\")[0];");
		assertNotNull(pluginsLink);
		pluginsLink.click();

		List<WebElement> addPluginLink = driver.findElements(By.linkText("Add Plugin"));
		addPluginLink.get(0).click();

		WebElement pluginSelectOption = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"select > option:contains('" + pluginId + "')\")[0];");
		assertNotNull(pluginSelectOption);

		pluginSelectOption.click();
		List<WebElement> saveLink = driver.findElements(By.linkText("Save Configuration"));
		saveLink.get(0).click();

		driver.navigate().refresh();

		WebElement pluginTitle = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"legend:contains('" + pluginId + "')\")[0];");
		assertNotNull(pluginTitle);
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
		selenium.stop();
	}
}

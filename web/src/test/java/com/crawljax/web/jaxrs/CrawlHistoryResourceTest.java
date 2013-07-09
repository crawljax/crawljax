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

public class CrawlHistoryResourceTest {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsResourceTest.class);
	private static Server server;
	private static DefaultSelenium selenium;
	private static WebDriver driver;

	private String configurationName = "TestConfiguration";


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
	public void runConfiguration() {
		selenium.open("/#/configurations");

		List<WebElement> configurationLink = driver.findElements(By.linkText(configurationName));
		configurationLink.get(0).click();

		WebElement empty = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"li:contains('Crawl Execution Queue')\").parent().find(\"li:contains('empty')\")[0];");
		assertNotNull(empty);

		List<WebElement> runConfigurationLink = driver.findElements(By.linkText("Run Configuration"));
		runConfigurationLink.get(0).click();

		ExpectedCondition<Boolean> isRunning = new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					WebElement running = (WebElement) ((JavascriptExecutor)driver).executeScript(
							"return $(\"li:contains('Crawl Execution Queue')\").parent().find(\"li:contains('running')\")[0];");
					return running != null;
				}
			};
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(isRunning);

		ExpectedCondition<Boolean> isComplete = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement success = (WebElement) ((JavascriptExecutor)driver).executeScript(
						"return $(\"li:contains('Crawl Execution Queue')\").parent().find(\"li:contains('success')\")[0];");
				return success != null;
			}
		};
		wait = new WebDriverWait(driver, 60);
		wait.until(isComplete);

		WebElement link = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"li:contains('Crawl Execution Queue')\").parent().find(\"li > span:contains('" + configurationName + "')\").parent().find(\"a\")[0];");

		link.click();

		List<WebElement> logLink = driver.findElements(By.linkText("Log"));
		assertFalse(logLink.isEmpty());
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

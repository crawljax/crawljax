package com.crawljax.web.jaxrs;

import com.crawljax.web.CrawljaxServer;
import com.google.common.collect.Lists;
import com.thoughtworks.selenium.DefaultSelenium;
import org.apache.xerces.impl.dv.xs.SchemaDateTimeException;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CrawlHistoryResourceTest {

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
	public void runConfigurationAndViewResult() {
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

		List<WebElement> historyLink = driver.findElements(By.linkText("History"));
		historyLink.get(0).click();
		WebElement dateContainer = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td > a:contains('" + configurationName + "')\").first().parent().next()[0];");
		assertNotNull(dateContainer);
		String displayedDate = dateContainer.getText();
		SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss");
		Date date = null;
		try {
			date = dateParser.parse(displayedDate);
		} catch (ParseException e) {

		}
		assertNotNull(date);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, -1);
		Date oneMinuteBack = cal.getTime();

		assertTrue(date.after(oneMinuteBack));
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

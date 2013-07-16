package com.crawljax.web.jaxrs;

import com.crawljax.web.CrawljaxServer;
import com.google.common.collect.Lists;
import com.thoughtworks.selenium.DefaultSelenium;
import org.apache.xerces.impl.dv.xs.SchemaDateTimeException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
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

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsResourceTest.class);
	private static DefaultSelenium selenium;
	private static WebDriver driver;

	private String configurationName = "TestConfiguration";

	@Rule
	public TestRule globalTimeout = new Timeout(120 * 1000);

	@ClassRule
	public static final CrawljaxServerResource SERVER = new CrawljaxServerResource();

	@BeforeClass
	public static void setup() throws Exception {
		driver = new FirefoxDriver();
		LOG.debug("Starting selenium");
		selenium = new WebDriverBackedSelenium(driver, SERVER.getUrl());
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void canRunConfigurationAndViewResults() {
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

		assertTrue(date.after(timeNSecondsAgo(70)));
	}

	private Date timeNSecondsAgo(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.SECOND, -n);
		return cal.getTime();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		selenium.stop();
	}
}

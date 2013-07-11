package com.crawljax.web.jaxrs;

import com.crawljax.web.CrawljaxServer;
import com.google.common.collect.Lists;
import com.thoughtworks.selenium.DefaultSelenium;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConfigurationsResourceTest {

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
	private String configurationUrl = "http://www.testsite.com";


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
	public void createNewConfiguration() {
		selenium.open("/#/configurations");
		List<WebElement> newConfigurationLink = driver.findElements(By.linkText("New Configuration"));
		assertFalse(newConfigurationLink.isEmpty());
		newConfigurationLink.get(0).click();
		List<WebElement> textBoxes = visibleElementsByCss(".ember-text-field");
		assertFalse(textBoxes.isEmpty());
		textBoxes.get(0).sendKeys(configurationName);
		textBoxes.get(1).clear();
		textBoxes.get(1).sendKeys(configurationUrl);
		List<WebElement> saveConfigurationLink = driver.findElements(By.linkText("Save Configuration"));
		assertFalse(saveConfigurationLink.isEmpty());
		saveConfigurationLink.get(0).click();
		List<WebElement> runConfigurationLink = driver.findElements(By.linkText("Run Configuration"));
		assertFalse(runConfigurationLink.isEmpty());

		WebElement nameSpan = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Name:')\").parent().find('div > span')[0];");
		assertTrue(nameSpan.getText().equals(configurationName));

		WebElement urlInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Site:')\").parent().find('div > input')[0];");
		assertTrue(urlInput.getAttribute("value").equals(configurationUrl));
	}

	@Test
	public void updateConfiguration() {
		selenium.open("/#/configurations");
		List<WebElement> configurationLink = driver.findElements(By.linkText(configurationName));
		assertFalse(configurationLink.isEmpty());
		configurationLink.get(0).click();

		WebElement maxCrawlDepthInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl Depth:')\").parent().find('div > input')[0];");
		maxCrawlDepthInput.clear();
		maxCrawlDepthInput.sendKeys("1");

		WebElement maxCrawlStates = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl States:')\").parent().find('div > input')[0];");
		maxCrawlStates.clear();
		maxCrawlStates.sendKeys("3");

		List<WebElement> saveConfigurationLink = driver.findElements(By.linkText("Save Configuration"));
		saveConfigurationLink.get(0).click();

		List<WebElement> configurationsLink = driver.findElements(By.linkText("Configurations"));
		configurationsLink.get(0).click();

		configurationLink = driver.findElements(By.linkText(configurationName));
		configurationLink.get(0).click();

		maxCrawlDepthInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl Depth:')\").parent().find('div > input')[0];");
		assertTrue(maxCrawlDepthInput.getAttribute("value").equals("1"));

		maxCrawlStates = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl States:')\").parent().find('div > input')[0];");
		assertTrue(maxCrawlStates.getAttribute("value").equals("3"));
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

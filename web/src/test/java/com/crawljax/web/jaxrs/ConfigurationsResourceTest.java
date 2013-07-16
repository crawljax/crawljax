package com.crawljax.web.jaxrs;

import com.crawljax.web.CrawljaxServer;
import com.google.common.collect.Lists;
import com.thoughtworks.selenium.DefaultSelenium;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConfigurationsResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationsResourceTest.class);
	private static DefaultSelenium selenium;
	private static WebDriver driver;

	private String configurationName = "TestConfiguration";
	private String configurationUrl = "http://www.testsite.com";

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
	public void canCreateNewConfiguration() {
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
	public void canUpdateConfiguration() {
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

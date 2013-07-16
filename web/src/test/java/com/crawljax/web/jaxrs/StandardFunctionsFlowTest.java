package com.crawljax.web.jaxrs;

import com.google.common.collect.Lists;
import com.thoughtworks.selenium.DefaultSelenium;
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

public class StandardFunctionsFlowTest {

	private static final Logger LOG = LoggerFactory.getLogger(StandardFunctionsFlowTest.class);
	private static DefaultSelenium selenium;
	private static WebDriver driver;

	private String configurationName = "TestConfiguration";
	private String configurationUrl = "http://crawljax.com";

	private String pluginName = "Dummy Plugin";
	private String pluginId = "dummy-plugin";

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
	public void canPerformStandardFunctions() {
		createNewConfiguration();
		updateConfiguration();

		runConfigurationAndViewResults();

		uploadPlugin();
		addPluginToConfiguration();
		deletePlugin();

		deleteConfiguration();
	}

	private void createNewConfiguration() {
		open("configurations");
		List<WebElement> newConfigurationLink = driver.findElements(By.linkText("New Configuration"));
		assertFalse(newConfigurationLink.isEmpty());
		followLink(newConfigurationLink.get(0));
		List<WebElement> textBoxes = visibleElementsByCss(".ember-text-field");
		assertFalse(textBoxes.isEmpty());
		textBoxes.get(0).sendKeys(configurationName);
		textBoxes.get(1).clear();
		textBoxes.get(1).sendKeys(configurationUrl);
		List<WebElement> saveConfigurationLink = driver.findElements(By.linkText("Save Configuration"));
		assertFalse(saveConfigurationLink.isEmpty());
		followLink(saveConfigurationLink.get(0));

		WebElement nameSpan = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Name:')\").parent().find('div > span')[0];");
		assertTrue(nameSpan.getText().equals(configurationName));

		WebElement urlInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Site:')\").parent().find('div > input')[0];");
		assertTrue(urlInput.getAttribute("value").equals(configurationUrl));
	}

	private void updateConfiguration() {

		openConfiguration();

		WebElement maxCrawlDepthInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl Depth:')\").parent().find('div > input')[0];");
		maxCrawlDepthInput.clear();
		maxCrawlDepthInput.sendKeys("1");

		WebElement maxCrawlStates = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl States:')\").parent().find('div > input')[0];");
		maxCrawlStates.clear();
		maxCrawlStates.sendKeys("3");

		List<WebElement> saveConfigurationLink = driver.findElements(By.linkText("Save Configuration"));
		followLink(saveConfigurationLink.get(0));

		driver.navigate().refresh();

		maxCrawlDepthInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl Depth:')\").parent().find('div > input')[0];");
		assertTrue(maxCrawlDepthInput.getAttribute("value").equals("1"));

		maxCrawlStates = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl States:')\").parent().find('div > input')[0];");
		assertTrue(maxCrawlStates.getAttribute("value").equals("3"));
	}

	private void runConfigurationAndViewResults() {
		openConfiguration();

		WebElement empty = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"li:contains('Crawl Execution Queue')\").parent().find(\"li:contains('empty')\")[0];");
		assertNotNull(empty);

		List<WebElement> runConfigurationLink = driver.findElements(By.linkText("Run Configuration"));
		followLink(runConfigurationLink.get(0));

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

		followLink(link);

		List<WebElement> logLink = driver.findElements(By.linkText("Log"));
		assertFalse(logLink.isEmpty());

		List<WebElement> historyLink = driver.findElements(By.linkText("History"));
		followLink(historyLink.get(0));
		driver.navigate().refresh();

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

	private void uploadPlugin() {
		open("plugins");

		final List<WebElement> existingPlugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + pluginName + "')\").toArray();");

		WebElement fileInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"input[type='file']\")[0];");

		String fileName = getClass().getClassLoader().getResource(pluginId + ".jar").toExternalForm();
		fileInput.sendKeys(fileName);

		List<WebElement> uploadLink = driver.findElements(By.linkText("Upload"));
		followLink(uploadLink.get(0));

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
		assertEquals(existingPlugins.size() + 1, plugins.size());
	}

	private void addPluginToConfiguration() {
		openConfiguration();

		WebElement pluginsLink = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"a:contains('Overview')\").parent().find(\"~ li > a:contains('Plugins')\")[0];");
		assertNotNull(pluginsLink);
		followLink(pluginsLink);

		List<WebElement> addPluginLink = driver.findElements(By.linkText("Add Plugin"));
		followLink(addPluginLink.get(0));

		WebElement pluginSelectOption = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"select > option:contains('" + pluginId + "')\")[0];");
		assertNotNull(pluginSelectOption);

		followLink(pluginSelectOption);
		List<WebElement> saveLink = driver.findElements(By.linkText("Save Configuration"));
		followLink(saveLink.get(0));

		driver.navigate().refresh();

		WebElement pluginTitle = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"legend:contains('" + pluginId + "')\")[0];");
		assertNotNull(pluginTitle);
	}

	private void deletePlugin() {
		open("plugins");

		final List<WebElement> existingPlugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + pluginName + "')\").toArray();");

		WebElement deleteLink = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + pluginName + "')\").next().find(\"a:contains('Delete')\")[0];");
		followLink(deleteLink);

		Alert confirmDialog = driver.switchTo().alert();
		confirmDialog.accept();

		driver.navigate().refresh();

		List<WebElement> plugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + pluginName + "')\").toArray();");

		assertEquals(existingPlugins.size() - 1, plugins.size());
	}

	private void deleteConfiguration() {
		open("configurations");

		List<WebElement> existingConfigurationLinks = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"a:contains('" + configurationName + "')\").toArray();");

		openConfiguration();

		List<WebElement> deleteLink = driver.findElements(By.linkText("Delete Configuration"));
		followLink(deleteLink.get(0));

		Alert confirmDialog = driver.switchTo().alert();
		confirmDialog.accept();

		open("configurations");
		List<WebElement> configurationLinks = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"a:contains('" + configurationName + "')\").toArray();");

		assertEquals(existingConfigurationLinks.size() - 1, configurationLinks.size());
	}

	private void open(String hashLocation) {
		selenium.open("/#/" + hashLocation);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void followLink(WebElement link) {
		link.click();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void openConfiguration() {
		open("configurations");
		WebElement configLink = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td > a:contains('" + configurationName + "')\")" + //Candidate link
						".parent().next().find(\"a:contains('" + configurationUrl + "')\")" + //verify url
						".parent().next(\":contains('never')\")" + //verify new configuration
						".prev().prev().find(\"a:contains('" + configurationName + "')\")[0];"); //select the link
		configLink.click();
	}

	private Date timeNSecondsAgo(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.SECOND, -n);
		return cal.getTime();
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

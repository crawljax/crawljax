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

	private static String CONFIG_NAME = "TestConfiguration";
	private static String CONFIG_URL = "http://demo.crawljax.com/";

	private static String LOCAL_PLUGIN_NAME = "Test Plugin";
	private static String LOCAL_PLUGIN_ID = "test-plugin";

	private static String REMOTE_PLUGIN_ID = "Dummy Plugin";
	private static String REMOTE_PLUGIN_NAME = "dummy-plugin";
	private static String REMOTE_PLUGIN_URL = "https://raw.github.com/crawljax/crawljax/web-ui-plugin-integration/web/src/test/resources/dummy-plugin.jar";

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
		copyConfiguration();

		addLocalPlugin();
		addRemotePlugin();
		addPluginToConfiguration();

		runConfigurationAndViewResults();

		deleteConfiguration();
		deletePlugins();
	}

	private void createNewConfiguration() {
		open("configurations");
		List<WebElement> newConfigurationLink = driver.findElements(By.linkText("New Configuration"));
		assertFalse(newConfigurationLink.isEmpty());
		followLink(newConfigurationLink.get(0));
		List<WebElement> textBoxes = visibleElementsByCss(".ember-text-field");
		assertFalse(textBoxes.isEmpty());
		textBoxes.get(0).sendKeys(CONFIG_NAME);
		textBoxes.get(1).clear();
		textBoxes.get(1).sendKeys(CONFIG_URL);
		List<WebElement> saveConfigurationLink = driver.findElements(By.linkText("Save Configuration"));
		assertFalse(saveConfigurationLink.isEmpty());
		followLink(saveConfigurationLink.get(0));

		WebElement nameSpan = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Name:')\").parent().find('div > span')[0];");
		assertTrue(nameSpan.getText().equals(CONFIG_NAME));

		WebElement urlInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Site:')\").parent().find('div > input')[0];");
		assertTrue(urlInput.getAttribute("value").equals(CONFIG_URL));
	}

	private void updateConfiguration() {

		openConfiguration();

		WebElement maxCrawlStates = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl States:')\").parent().find('div > input')[0];");
		maxCrawlStates.clear();
		maxCrawlStates.sendKeys("3");

		maxCrawlStates = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Maximum Crawl States:')\").parent().find('div > input')[0];");
		assertTrue(maxCrawlStates.getAttribute("value").equals("3"));

		WebElement crawlRulesLink = driver.findElements(By.linkText("Crawl Rules")).get(0);
		followLink(crawlRulesLink);

		WebElement clickDefaultElements = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"legend:contains('Click Rules')\").parent().find('input[type=\"checkbox\"]')[0];");
		followLink(clickDefaultElements);

		WebElement addANewClickRule = driver.findElements(By.linkText("Add a New Click Rule")).get(0);
		followLink(addANewClickRule);

		WebElement addANewConditionLink = driver.findElements(By.linkText("Add a New Condition")).get(0);
		followLink(addANewConditionLink);

		WebElement pageConditionInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"legend:contains('Page Conditions')\").parent().find('div > input[type=\"text\"]')[0];");
		pageConditionInput.clear();
		pageConditionInput.sendKeys("crawljax");

		List<WebElement> saveConfigurationLink = driver.findElements(By.linkText("Save Configuration"));
		followLink(saveConfigurationLink.get(0));

		ExpectedCondition<Boolean> isSaved = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement notification = driver.findElements(By.id("notification")).get(0);
				return notification.getText().equals("Configuration Saved");
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(isSaved);
	}

	private void copyConfiguration() {

		openConfiguration();
		WebElement copyConfigurationLink = driver.findElements(By.linkText("New Copy")).get(0);
		followLink(copyConfigurationLink);

		WebElement nameInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Name:')\").parent().find('div > input')[0];");
		nameInput.clear();
		nameInput.sendKeys("copy");

		WebElement saveConfigurationLink = driver.findElements(By.linkText("Save Configuration")).get(0);
		followLink(saveConfigurationLink);

		WebElement siteInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Site:')\").parent().find('div > input')[0];");
		String asdf = siteInput.getAttribute("value");
		assertEquals(CONFIG_URL, siteInput.getAttribute("value"));

		List<WebElement> deleteLink = driver.findElements(By.linkText("Delete Configuration"));
		followLink(deleteLink.get(0));

		Alert confirmDialog = driver.switchTo().alert();
		confirmDialog.accept();

		ExpectedCondition<Boolean> isDeleted = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement notification = driver.findElements(By.id("notification")).get(0);
				return notification.getText().equals("Configuration Deleted");
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(isDeleted);
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

		WebElement link = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"li:contains('Crawl Execution Queue')\").parent().find(\"li > span:contains('" + CONFIG_NAME + "')\").parent().find(\"a\")[0];");

		followLink(link);

		ExpectedCondition<Boolean> isComplete = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement success = (WebElement) ((JavascriptExecutor)driver).executeScript(
						"return $(\"li:contains('Crawl Execution Queue')\").parent().find(\"li:contains('success')\")[0];");
				return success != null;
			}
		};
		wait = new WebDriverWait(driver, 60);
		wait.until(isComplete);

		List<WebElement> logLink = driver.findElements(By.linkText("Log"));
		assertFalse(logLink.isEmpty());

		List<WebElement> crawlOverviewLink = driver.findElements(By.linkText("Crawl Overview"));
		assertFalse(crawlOverviewLink.isEmpty());

		List<WebElement> pluginLink = driver.findElements(By.linkText(LOCAL_PLUGIN_NAME));
		assertFalse(pluginLink.isEmpty());

		List<WebElement> historyLink = driver.findElements(By.linkText("History"));
		followLink(historyLink.get(0));
		driver.navigate().refresh();

		WebElement dateContainer = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td > a:contains('" + CONFIG_NAME + "')\").first().parent().next()[0];");
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

	private void addLocalPlugin() {
		open("plugins");

		List<WebElement> refreshLink = driver.findElements(By.linkText("Refresh List"));
		followLink(refreshLink.get(0));

		final List<WebElement> existingPlugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + LOCAL_PLUGIN_NAME + "')\").toArray();");

		WebElement fileInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"input[type='file']\")[0];");

		String fileName = getClass().getClassLoader().getResource(LOCAL_PLUGIN_ID + ".jar").toExternalForm();
		fileInput.sendKeys(fileName);

		List<WebElement> uploadLink = driver.findElements(By.linkText("Upload"));
		followLink(uploadLink.get(0));

		ExpectedCondition<Boolean> uploaded = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				List<WebElement> plugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
						"return $(\"td:contains('" + LOCAL_PLUGIN_NAME + "')\").toArray();");
				return plugins.size() > existingPlugins.size();
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(uploaded);

		List<WebElement> plugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + LOCAL_PLUGIN_NAME + "')\").toArray();");
		assertEquals(existingPlugins.size() + 1, plugins.size());
	}

	private void addRemotePlugin() {
		open("plugins");

		List<WebElement> refreshLink = driver.findElements(By.linkText("Refresh List"));
		followLink(refreshLink.get(0));

		final List<WebElement> existingPlugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + REMOTE_PLUGIN_NAME + "')\").toArray();");

		WebElement urlInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('URL:')\").parent().find(\"input[type='text']\")[0];");
		urlInput.sendKeys(REMOTE_PLUGIN_URL);

		WebElement nameInput = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"label:contains('Plugin Name:')\").parent().find(\"input[type='text']\")[0];");
		nameInput.sendKeys(REMOTE_PLUGIN_ID);

		List<WebElement> downloadLink = driver.findElements(By.linkText("Add"));
		followLink(downloadLink.get(0));

		ExpectedCondition<Boolean> uploaded = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				List<WebElement> plugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
						"return $(\"td:contains('" + REMOTE_PLUGIN_NAME + "')\").toArray();");
				return plugins.size() > existingPlugins.size();
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(uploaded);

		List<WebElement> plugins = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"td:contains('" + REMOTE_PLUGIN_NAME + "')\").toArray();");
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
				"return $(\"select > option:contains('" + LOCAL_PLUGIN_ID + "')\").last()[0];");
		assertNotNull(pluginSelectOption);

		followLink(pluginSelectOption);
		List<WebElement> saveLink = driver.findElements(By.linkText("Save Configuration"));
		followLink(saveLink.get(0));

		driver.navigate().refresh();

		WebElement pluginTitle = (WebElement) ((JavascriptExecutor)driver).executeScript(
				"return $(\"legend:contains('" + LOCAL_PLUGIN_NAME + "')\")[0];");
		assertNotNull(pluginTitle);
	}

	private void deletePlugins() {
		open("plugins");

		List<WebElement> deleteLinks = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"a:contains('Delete')\").toArray()");

		while(deleteLinks.size() > 0) {
			final int count = deleteLinks.size();
			followLink(deleteLinks.get(0));
			Alert confirmDialog = driver.switchTo().alert();
			confirmDialog.accept();
			ExpectedCondition<Boolean> isDeleted = new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					WebElement notification = driver.findElements(By.id("notification")).get(0);
					return notification.getText().equals("Plugin Deleted");
				}
			};
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(isDeleted);
			deleteLinks = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
					"return $(\"a:contains('Delete')\").toArray()");
		}

		driver.navigate().refresh();

		deleteLinks = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"a:contains('Delete')\").toArray();");
		assertEquals(0, deleteLinks.size());
	}

	private void deleteConfiguration() {
		open("configurations");

		List<WebElement> existingConfigurationLinks = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"a:contains('" + CONFIG_NAME + "')\").toArray();");

		openConfiguration();

		List<WebElement> deleteLink = driver.findElements(By.linkText("Delete Configuration"));
		followLink(deleteLink.get(0));

		Alert confirmDialog = driver.switchTo().alert();
		confirmDialog.accept();

		open("configurations");
		List<WebElement> configurationLinks = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
				"return $(\"a:contains('" + CONFIG_NAME + "')\").toArray();");

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
				"return $(\"td > a:contains('" + CONFIG_NAME + "')\")" + //Candidate link
						".parent().next().find(\"a:contains('" + CONFIG_URL + "')\")" + //verify url
						".parent().prev().find(\"a:contains('" + CONFIG_NAME + "')\")[0];"); //select the link
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

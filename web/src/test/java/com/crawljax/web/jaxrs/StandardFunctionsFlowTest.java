package com.crawljax.web.jaxrs;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

//TODO: delete the following imports
import java.io.File;
import org.openqa.selenium.firefox.FirefoxProfile;

import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

public class StandardFunctionsFlowTest {

	private static final Logger LOG = LoggerFactory.getLogger(StandardFunctionsFlowTest.class);
	private static DefaultSelenium selenium;
	private static WebDriver driver;
	

	private static String CONFIG_NAME = "TestConfiguration";
	private static String CONFIG_URL = "http://demo.crawljax.com/";

	private static String LOCAL_PLUGIN_NAME = "Test Plugin";
	private static String LOCAL_PLUGIN_ID = "test-plugin";

	private static String REMOTE_PLUGIN_NAME = "dummy-plugin";
	private static String REMOTE_PLUGIN_URL =
	        "https://raw.github.com/crawljax/crawljax/web-ui-with-plugins/web/src/test/resources/dummy-plugin.jar";

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
		editConfiguration();
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
		List<WebElement> newConfigurationLink =
		        driver.findElements(By.linkText("New Configuration"));
		assertFalse(newConfigurationLink.isEmpty());
		followLink(newConfigurationLink.get(0));
		List<WebElement> textBoxes = visibleElementsByTagName("input");
		assertFalse(textBoxes.isEmpty());
		textBoxes.get(0).sendKeys(CONFIG_NAME);
		textBoxes.get(1).clear();
		textBoxes.get(1).sendKeys(CONFIG_URL);
		List<WebElement> saveConfigurationLink =
		        driver.findElements(By.linkText("Save Configuration"));
		assertFalse(saveConfigurationLink.isEmpty());
		followLink(saveConfigurationLink.get(0));
		
		WebElement nameSpan = driver.findElements(By.xpath(
		       "//label[contains(text(),'Name:')]/following-sibling::input")).get(0);
		assertTrue(nameSpan.getAttribute("value").equals(CONFIG_NAME));

		WebElement urlInput = driver.findElements(By.xpath(
		       "//label[contains(text(),'Site:')]/following-sibling::input")).get(0);
		assertTrue(urlInput.getAttribute("value").equals(CONFIG_URL));
	}

	private void editConfiguration() {
		WebElement maxCrawlStates =
		        driver.findElements(
		                By.xpath(
		                        "//label[contains(text(),'Maximum Crawl States:')]/following-sibling::input"))
		                .get(0);
		maxCrawlStates.clear();
		maxCrawlStates.sendKeys("3");

		maxCrawlStates =
		        driver.findElements(
		                By.xpath(
		                        "//label[contains(text(),'Maximum Crawl States:')]/following-sibling::input"))
		                .get(0);
		assertTrue(maxCrawlStates.getAttribute("value").equals("3"));

		WebElement crawlRulesLink = driver.findElements(By.linkText("Crawl Rules")).get(0);
		followLink(crawlRulesLink);

		WebElement clickDefaultElements =
		        driver.findElements(By.xpath("//label[@class='checkbox']")).get(0);
		followLink(clickDefaultElements);

		WebElement addANewClickRule =
		        driver.findElements(By.linkText("Add a New Click Rule")).get(0);
		followLink(addANewClickRule);

		WebElement addANewConditionLink =
		        driver.findElements(By.linkText("Add a New Condition")).get(0);
		followLink(addANewConditionLink);

		WebElement pageConditionInput =
		        driver.findElements(
		                By.xpath(
		                        "//legend[contains(text(),'Page Conditions')]/following-sibling::table//input[@type='text']"))
		                .get(0);
		pageConditionInput.clear();
		pageConditionInput.sendKeys("ConditionInput");

		WebElement addANewFilter = driver.findElements(By.linkText("Add a New Filter")).get(0);
		followLink(addANewFilter);
		WebElement filterInput =
		        driver.findElements(
		                By.xpath(
		                        "//legend[contains(text(),'State Filters')]/following-sibling::table//input[@type='text']"))
		                .get(0);
		filterInput.clear();
		filterInput.sendKeys("FilterInput");

		WebElement addANewField = driver.findElements(By.linkText("Add a New Field")).get(0);
		followLink(addANewField);
		WebElement fieldIdInput =
		        driver.findElements(
		                By.xpath(
		                        "//legend[contains(text(),'Form Field Input Values')]/following-sibling::table//input[@type='text']"))
		                .get(0);
		fieldIdInput.clear();
		fieldIdInput.sendKeys("FieldID");
		WebElement fieldValueInput =
		        driver.findElements(
		                By.xpath(
		                        "//legend[contains(text(),'Form Field Input Values')]/following-sibling::table//input[@type='text']"))
		                .get(1);
		fieldValueInput.clear();
		fieldValueInput.sendKeys("FieldValue");
		
		((JavascriptExecutor) driver).executeScript("scroll(250,0);");
		WebElement assertionsLink = driver.findElements(By.partialLinkText("Assertions")).get(0);
		followLink(assertionsLink);

		addANewConditionLink = driver.findElements(By.linkText("Add a New Condition")).get(0);
		followLink(addANewConditionLink);
		WebElement newConditionInput =
		        driver.findElements(
		                By.xpath(
		                        "//legend[contains(text(),'Invariants')]/following-sibling::table//input[@type='text']"))
		                .get(0);
		newConditionInput.clear();
		newConditionInput.sendKeys("crawljax");

		List<WebElement> saveConfigurationLink =
		        driver.findElements(By.linkText("Save Configuration"));
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

		WebElement copyConfigurationLink = driver.findElements(By.linkText("New Copy")).get(0);
		followLink(copyConfigurationLink);

		List<WebElement> textBoxes = visibleElementsByTagName("input");
		WebElement nameInput = textBoxes.get(0);
		nameInput.clear();
		nameInput.sendKeys("copy");

		WebElement saveConfigurationLink =
		        driver.findElements(By.linkText("Save Configuration")).get(0);
		followLink(saveConfigurationLink);

		WebElement siteInput = driver.findElement(By.xpath(
		        "//label[contains(text(),'Site:')]/following-sibling::input"));
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

		WebElement empty =
		        driver.findElement(By
		                .xpath(
		                "//li[contains(text(),'CRAWL EXECUTION QUEUE')]/following-sibling::li[contains(i,'empty')]"));
		assertNotNull(empty);

		List<WebElement> runConfigurationLink =
		        driver.findElements(By.linkText("Run Configuration"));
		followLink(runConfigurationLink.get(0));

		ExpectedCondition<Boolean> isRunning = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement running =
				        driver.findElement(By
				                .xpath(
				                "//li[contains(text(),'CRAWL EXECUTION QUEUE')]/following-sibling::li//span[contains(i,'running')]"));
				return running != null;
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(isRunning);

		ExpectedCondition<Boolean> isComplete = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				WebElement success =
				        driver.findElement(By
				                .xpath(
				                "//li[contains(text(),'CRAWL EXECUTION QUEUE')]/following-sibling::li//span[contains(i,'success')]"));
				return success != null;
			}
		};
		wait = new WebDriverWait(driver, 60);
		wait.until(isComplete);

		List<WebElement> crawlHistoryLink = driver.findElements(By.linkText("Crawl History"));
		followLink(crawlHistoryLink.get(0));

		WebElement crawlLink = driver.findElement(By.xpath(
		        "//td[following-sibling::td[contains(a,'" + CONFIG_NAME + "')]]/a"));
		followLink(crawlLink);
     
		List<WebElement> logLink = driver.findElements(By.linkText("Log"));
		assertFalse(logLink.isEmpty());

		List<WebElement> crawlOverviewLink = driver.findElements(By.linkText("Crawl Overview"));
		assertFalse(crawlOverviewLink.isEmpty());

		List<WebElement> pluginLink = driver.findElements(By.linkText(LOCAL_PLUGIN_NAME));
		assertFalse(pluginLink.isEmpty());
		
		followLink(logLink.get(0));
		assertFalse(driver.findElement(By.id("logPanel")).getAttribute("innerHTML").equals(""));

		List<WebElement> historyLink = driver.findElements(By.linkText("History"));
		followLink(historyLink.get(0));
		driver.navigate().refresh();

		WebElement dateContainer = driver.findElement(By.xpath(
		        "//td[preceding-sibling::td[contains(a,'" + CONFIG_NAME + "')]]"));
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

		WebElement fileInput = driver.findElement(By.xpath("//input[@type='file']"));

		String fileName =
		        getClass().getClassLoader().getResource(LOCAL_PLUGIN_ID + ".jar")
		                .toExternalForm();
		fileInput.sendKeys(fileName);

		List<WebElement> uploadLink = driver.findElements(By.linkText("Upload Local Plugin"));
		followLink(uploadLink.get(0));

		WebElement uploaded =
		        (new WebDriverWait(driver, 10))
		                .until(ExpectedConditions.presenceOfElementLocated(By
		                        .xpath(
		                        "//legend[contains(text(),'Available Plugins')]/following-sibling::table//tbody//tr"
		                        + "//td[contains(text(),'" + LOCAL_PLUGIN_NAME + "')]")));
		assertNotNull(uploaded);

	}

	private void addRemotePlugin() {
		open("plugins");

		List<WebElement> refreshLink = driver.findElements(By.linkText("Refresh List"));
		followLink(refreshLink.get(0));

		WebElement urlInput = driver.findElement(By.xpath(
		        "//label[contains(text(),'URL:')]/following-sibling::input[@type='text']"));
		urlInput.sendKeys(REMOTE_PLUGIN_URL);

		List<WebElement> downloadLink = driver.findElements(By.linkText("Download Remote Plugin"));
		followLink(downloadLink.get(0));

		WebElement uploaded =
		        (new WebDriverWait(driver, 10))
		                .until(ExpectedConditions.presenceOfElementLocated(By
		                        .xpath(
		                        "//legend[contains(text(),'Available Plugins')]/following-sibling::table//tbody//tr"
		                        + "//td[contains(text(),'" + REMOTE_PLUGIN_NAME + "')]")));
		assertNotNull(uploaded);

	}

	private void addPluginToConfiguration() {
		openConfiguration();

		WebElement pluginsLink =
		        driver.findElement(By
		                .xpath(
		                "//li[preceding-sibling::li[contains(a,'Overview')]]/a[contains(text(),'Plugins')]"));
		assertNotNull(pluginsLink);
		followLink(pluginsLink);

		List<WebElement> addPluginLink = driver.findElements(By.linkText("Add Plugin"));
		followLink(addPluginLink.get(0));
		Select select = new Select(driver.findElement(By.xpath("//select")));

		select.selectByVisibleText(LOCAL_PLUGIN_ID);

		List<WebElement> saveLink = driver.findElements(By.linkText("Save Configuration"));
		followLink(saveLink.get(0));

		driver.navigate().refresh();

		WebElement pluginTitle =
		        driver.findElement(By.xpath("//legend[contains(text(),'" + LOCAL_PLUGIN_NAME
		                + "')]"));
		assertNotNull(pluginTitle);
	}

	private void deletePlugins() {
		open("plugins");

		List<WebElement> deleteLinks = driver.findElements(By.linkText("Delete"));

		while (deleteLinks.size() > 0) {
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
			if (!isElementPresent(driver, By.linkText("Delete"))) {
				break;
			}
			deleteLinks = driver.findElements(By.linkText("Delete"));
		}

		driver.navigate().refresh();
		assertFalse(isElementPresent(driver, By.linkText("Delete")));
	}

	private void deleteConfiguration() {
		open("configurations");

		List<WebElement> existingConfigurationLinks =
		        driver.findElements(By.linkText(CONFIG_NAME));

		openConfiguration();

		WebElement deleteLink = driver.findElement(By.linkText("Delete Configuration"));
		followLink(deleteLink);

		Alert confirmDialog = driver.switchTo().alert();
		confirmDialog.accept();

		open("configurations");
		if (isElementPresent(driver, By.linkText(CONFIG_NAME))) {
			List<WebElement> configurationLinks = driver.findElements(By.linkText(CONFIG_NAME));
			assertEquals(existingConfigurationLinks.size() - 1, configurationLinks.size());
		}
		else {
			assertEquals(1, existingConfigurationLinks.size());
		}
	}

	private void open(String hashLocation) {
		selenium.open("/#/" + hashLocation);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void followLink(WebElement link) {
		link.click();
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void openConfiguration() {
		open("configurations");
		WebElement configLink =
		        driver.findElement(By.xpath(
		                "//td[contains(a,'" + CONFIG_NAME
		                        + "') and following-sibling::td[contains(a,'" + CONFIG_URL
		                        + "')]]/a"));
		configLink.click();
	}

	private Date timeNSecondsAgo(int n) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.SECOND, -n);
		return cal.getTime();
	}

	private List<WebElement> visibleElementsByTagName(String selector) {
		List<WebElement> elements = driver.findElements(By.tagName(selector));
		List<WebElement> visible = Lists.newArrayListWithExpectedSize(elements.size());
		for (WebElement webElement : elements) {
			if (webElement.isDisplayed()) {
				visible.add(webElement);
			}
		}
		return visible;
	}

	private boolean isElementPresent(WebDriver driver, By by) {
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		try {
			driver.findElement(by);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		selenium.stop();
	}
}

package com.crawljax.plugins.crawloverview;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.plugins.crawloverview.model.Statistics;
import com.google.common.collect.Lists;

import io.github.bonigarcia.wdm.WebDriverManager;

public class OverviewIntegrationTest {

	@ClassRule
	public static final RunHoverCrawl HOVER_CRAWL = new RunHoverCrawl();

	private static final Logger LOG = LoggerFactory.getLogger(OverviewIntegrationTest.class);

	private static String url;
	private static Server server;

	private static WebDriver driver;

	@BeforeClass
	public static void setup() throws Exception {
		LOG.debug("Starting Jetty");
		server = new Server(0);

		url = setupJetty() + "/localhost/crawl0";

		LOG.info("Jetty started on {}", url);
		LOG.debug("Starting selenium");
		
		WebDriverManager.chromedriver().setup();
		ChromeOptions optionsChrome = new ChromeOptions();
		optionsChrome.addArguments("--headless", "--disable-gpu", "--window-size=1200x600");
		driver =  new ChromeDriver(optionsChrome);

		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	private static String setupJetty() throws Exception {
		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setWar(RunHoverCrawl.getOutDir().getAbsolutePath());
		server.setHandler(webAppContext);
		server.start();
		int port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		String url = "http://localhost:" + port;
		return url;
	}

	@Test
	public void whenOpenedGraphIsShownAndBrandIsPresent() {
		driver.navigate().to(url);

		sourceHasNoVelocitySymbols();

		WebElement brand = driver.findElement(By.cssSelector("a.brand"));
		assertThat(brand.getText(), is("Crawl overview"));
		assertThat(driver.findElement(By.cssSelector("svg")), is(notNullValue()));

	}

	/**
	 * If velocity couldn't resolve a variable, it leaves behind a `$`.
	 */
	private void sourceHasNoVelocitySymbols() {
		assertThat(driver.getPageSource(), not(containsString("${")));
	}

	@Test
	public void whenClickStatisticsHeadersArePresent() {
		driver.navigate().to(url);
		driver.findElement(By.linkText("Statistics")).click();
		assertElementsText("H1", "Crawl results", "Highs and lows");
	}

	@Test
	public void whenClickConfigHeadersArePresent() {
		driver.navigate().to(url);
		driver.findElement(By.linkText("Configuration")).click();
		assertElementsText("H1", "Crawl configuration", "Version info");
	}

	@Test
	public void whenClickUrlsHeadersArePresent() {
		driver.findElement(By.linkText("URL's")).click();
		assertElementsText("H1", "URL's visited");
	}

	public void assertElementsText(String element, String... elementText) {
		List<WebElement> foundndElements = visibleElementsByCss(element);
		assertThat(foundndElements, hasSize(elementText.length));
		Iterator<WebElement> iterator = foundndElements.iterator();
		for (String text : elementText) {
			assertThat(iterator.next().getText(), is(text));
		}
	}

	@Test
	public void allUrlsAreShown() {
		driver.navigate().to(url + "/#urls");
		// selenium.open("/#urls");
		List<WebElement> tableRows = visibleElementsByCss("tr");
		int urlsExpeted =
		        HOVER_CRAWL.getResult().getStatistics().getStateStats().getUrls().size();
		assertThat(tableRows, is(hasSize(urlsExpeted)));
	}

	@Test
	public void allStatesAreShown() {
		driver.navigate().to(url + "/#graph");
		// selenium.open("/#graph");
		Statistics statistics = HOVER_CRAWL.getResult().getStatistics();
		// drawnstate -1 because the outer state is also a group.
		int drawnStates = driver.findElements(By.cssSelector("g")).size() - 1;
		List<WebElement> drawnEdges =
		        driver.findElements(By.cssSelector("path[marker-end=\"url(#Triangle)\"]"));
		assertThat(drawnStates, is(statistics.getStateStats().getTotalNumberOfStates()));
		assertThat(drawnEdges, hasSize(statistics.getEdges()));
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
		driver.close();
	}
}

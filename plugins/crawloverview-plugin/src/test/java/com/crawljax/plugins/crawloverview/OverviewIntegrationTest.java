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

import com.crawljax.plugins.crawloverview.model.Statistics;
import com.google.common.collect.Lists;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;
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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverviewIntegrationTest {

	@ClassRule
	public static final RunHoverCrawl HOVER_CRAWL = new RunHoverCrawl();

	private static final Logger LOG = LoggerFactory.getLogger(OverviewIntegrationTest.class);

	private static Server server;
	private static DefaultSelenium selenium;

	private static WebDriver driver;

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
		selenium.open("/");

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
		selenium.open("/");
		driver.findElement(By.linkText("Statistics")).click();
		assertElementsText("H1", "Crawl results", "Highs and lows");
	}

	@Test
	public void whenClickConfigHeadersArePresent() {
		selenium.open("/");
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
		selenium.open("/#urls");
		List<WebElement> tableRows = visibleElementsByCss("tr");
		int urlsExpeted =
		        HOVER_CRAWL.getResult().getStatistics().getStateStats().getUrls().size();
		assertThat(tableRows, is(hasSize(urlsExpeted)));
	}

	@Test
	public void allStatesAreShown() {
		selenium.open("/#graph");
		Statistics statistics = HOVER_CRAWL.getResult().getStatistics();
		// drawnstate -1 because the outer state is also a group.
		int drawnStates = driver.findElements(By.cssSelector("g")).size() - 1;
		List<WebElement> drawnEdges = visibleElementsByCss("path");
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
		selenium.stop();
	}
}

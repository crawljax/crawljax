package com.crawljax.plugins.crawloverview;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.DefaultSelenium;

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
		int port = ((AbstractConnector) server.getConnectors()[0]).getLocalPort();
		String url = "http://localhost:" + port;
		return url;
	}

	@Test
	public void verifyAllPagesPresentAndNoVelocityMistakes() {
		selenium.open("/");
		WebElement brand = driver.findElement(By.cssSelector("a.brand"));
		assertThat(brand.getText(), is("Crawl overview"));
		assertThat(driver.findElement(By.cssSelector("svg")), is(notNullValue()));
		driver.findElement(By.linkText("Statistics")).click();
		sourceHasNoDollerSigns();
		List<WebElement> foundndElements = driver.findElements(By.cssSelector("H1"));
		assertThat(foundndElements, hasSize(2));
		assertThat(foundndElements.iterator().next().getText(), is("Crawl results"));

		driver.findElement(By.linkText("URL's")).click();
		sourceHasNoDollerSigns();
		assertThat(driver.findElement(By.cssSelector("h1")).getText(), is("URL's visited"));

		driver.findElement(By.linkText("Configuration")).click();
		sourceHasNoDollerSigns();
		foundndElements = driver.findElements(By.cssSelector("H1"));
		assertThat(foundndElements, hasSize(2));
		assertThat(foundndElements.iterator().next().getText(), is("Crawl configuration"));
	}

	/**
	 * If velocity couldn't resolve a variable, it leaves behind a `$`.
	 */
	private void sourceHasNoDollerSigns() {
		assertThat(driver.getPageSource(), not(containsString("$")));
	}

	@AfterClass
	public static void tearDown() throws Exception {
		server.stop();
		selenium.stop();
	}
}

package com.crawljax.core;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;

public class IFrameTest {
	static CrawljaxController crawljax;

	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException {
		CrawljaxConfiguration crawljaxConfiguration = new CrawljaxConfiguration();
		crawljaxConfiguration.setCrawlSpecification(getCrawlSpecification());
		crawljax = new CrawljaxController(crawljaxConfiguration);
	}

	@Test
	public void run() {
		/*
		 * try { crawljax.run(); assertEquals("Clickables", 9,
		 * crawljax.getStateFlowGraph().getAllEdges().size()); System.out.println(
		 * "Actually Clickables should be 10, but webdrive does not support nested iframes yet!");
		 * assertEquals("States", 9, crawljax.getStateFlowGraph().getAllStates().size()); System.out
		 * .
		 * println("Actually States should be 11, but webdrive does not support nested iframes yet!"
		 * ); } catch (ConfigurationException e) { e.printStackTrace(); fail(e.getMessage()); }
		 * catch (CrawljaxException e) { e.printStackTrace(); fail(e.getMessage()); }
		 */}

	private static CrawlSpecification getCrawlSpecification() {
		File index = new File("src/test/site/iframe/index.html");
		CrawlSpecification crawler = new CrawlSpecification("file://" + index.getAbsolutePath());
		crawler.setWaitTimeAfterEvent(100);
		crawler.setWaitTimeAfterReloadUrl(100);
		crawler.setDepth(3);
		crawler.click("a");

		return crawler;
	}

	public static void main(String[] args) {
		File index = new File("src/test/site/iframe/page0.html");
		WebDriver driver = new FirefoxDriver();
		driver.get("file://" + index.getAbsolutePath());

		/*
		 * WebElement button002 = driver.findElement(By.id("button1")); button002.click();
		 * driver.switchTo().frame("0"); WebElement button01 =
		 * driver.findElement(By.id("button01")); button01.click(); driver.switchTo().frame("0.0");
		 * WebElement button001 = driver.findElement(By.id("button001")); button001.click();
		 */

		// driver.switchTo().frame("frame0.frame0-0");
		driver.switchTo().frame("0.0");

		System.out.println(driver.getPageSource());

		WebElement button002 = driver.findElement(By.id("button002"));
		button002.click();

		// driver.close();
	}

}

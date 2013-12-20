package com.crawljax.core;

import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

@Category(BrowserTest.class)
public class NestedFramesTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site/iframe");

	private WebDriver driver;

	@Test
	public void testNestedFramesIndex() {
		driver = new FirefoxDriver();
		driver.get(SERVER.getSiteUrl().toExternalForm());

		driver.switchTo().frame(0);
		driver.switchTo().frame(0);

		WebElement button002 = driver.findElement(By.id("button002"));
		button002.click();
	}

	@After
	public void close() {
		driver.close();

	}
}

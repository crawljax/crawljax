package com.crawljax.core;

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.crawljax.test.BrowserTest;

@Category(BrowserTest.class)
public class NestedFramesTest {

	private WebDriver driver = new FirefoxDriver();

	@Test
	public void testNestedFramesIndex() {
		File index = new File("src/test/resources/site/iframe/page0.html");
		driver.get("file://" + index.getAbsolutePath());

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

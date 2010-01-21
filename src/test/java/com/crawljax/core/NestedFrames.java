package com.crawljax.core;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class NestedFrames {
	WebDriver driver = new FirefoxDriver();

	@Test
	public void testNestedFramesIndex() {
		File index = new File("src/test/site/iframe/page0.html");
		driver.get("file://" + index.getAbsolutePath());

		driver.switchTo().frame("0.0");
		System.out.println("DOM: " + driver.getPageSource());

		WebElement button002 = driver.findElement(By.id("button002"));
		try {
			button002.click();
		} catch (NoSuchElementException e) {
			e.printStackTrace();

			fail(e.getMessage());
		}
	}

	@After
	public void close() {
		driver.close();

	}
}

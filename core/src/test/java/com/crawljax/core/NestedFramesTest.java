package com.crawljax.core;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.test.BrowserTest;
import com.crawljax.test.RunWithWebServer;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Category(BrowserTest.class)
public class NestedFramesTest {

	@ClassRule
	public static final RunWithWebServer SERVER = new RunWithWebServer("/site/iframe");

	private WebDriver driver;

	@Rule
	public BrowserProvider provider = new BrowserProvider();

	@Test
	public void testNestedFramesIndex() {
		driver = provider.newBrowser();
		driver.get(SERVER.getSiteUrl().toString());

		driver.switchTo().frame(0);
		driver.switchTo().frame(0);

		WebElement button002 = driver.findElement(By.id("button002"));
		button002.click();
	}


}

package com.crawljax.browser;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.google.common.base.Strings;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.rules.ExternalResource;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BrowserProvider extends ExternalResource {

	private static final Logger LOG = LoggerFactory.getLogger(BrowserProvider.class);
	private List<RemoteWebDriver> usedBrowsers;

	public static EmbeddedBrowser.BrowserType getBrowserType() {
		// typically read from the profile in pom.xml
		String browser = System.getProperty("test.browser");
		if (!Strings.isNullOrEmpty(browser)) {
			return EmbeddedBrowser.BrowserType.valueOf(browser);
		} else {
			return BrowserType.CHROME_HEADLESS;
		}
	}

	@Override
	protected void before() {
		usedBrowsers = new LinkedList<>();
	}

	public EmbeddedBrowser newEmbeddedBrowser() {
		return WebDriverBackedEmbeddedBrowser.withDriver(newBrowser());
	}

	/**
	 * Return the browser.
	 */
	public RemoteWebDriver newBrowser() {
		RemoteWebDriver driver;
		switch (getBrowserType()) {
			case CHROME:
				WebDriverManager.chromedriver().setup();
				driver = new ChromeDriver();
				break;
			case CHROME_HEADLESS:
				WebDriverManager.chromedriver().setup();
				ChromeOptions optionsChrome = new ChromeOptions();
				optionsChrome.addArguments("--headless");
				driver = new ChromeDriver(optionsChrome);
				break;
			case FIREFOX:
				WebDriverManager.firefoxdriver().setup();
				driver = new FirefoxDriver();
				break;
			case FIREFOX_HEADLESS:
				WebDriverManager.firefoxdriver().setup();
				FirefoxOptions firefoxOptions = new FirefoxOptions();
				firefoxOptions.setCapability("marionette", true);
				firefoxOptions.setHeadless(true);
				driver = new FirefoxDriver(firefoxOptions);
				break;
			case PHANTOMJS:
				WebDriverManager.phantomjs().setup();
				driver = new PhantomJSDriver();
				break;
			default:
				throw new IllegalStateException("Unsupported browser type " + getBrowserType());
		}

		/* Store the browser as a used browser so we can clean it up later. */
		usedBrowsers.add(driver);

		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
				.pageLoadTimeout(30, TimeUnit.SECONDS).setScriptTimeout(30, TimeUnit.SECONDS);

		driver.manage().deleteAllCookies();

		return driver;
	}

	@Override
	protected void after() {
		for (RemoteWebDriver browser : usedBrowsers) {
			try {

				/* Make sure we clean up properly. */
				if (!browser.toString().contains("(null)")) {
					if (getBrowserType() == BrowserType.PHANTOMJS) {
						// PhantomJS only quits the process on quit command
						browser.quit();
					} else {
						browser.close();
					}
				}

			} catch (RuntimeException e) {
				LOG.warn("Could not close the browser: {}", e.getMessage());
			}
		}
	}
}

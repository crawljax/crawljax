package com.crawljax.browser;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.util.Helper;
import com.crawljax.util.PropertyHelper;

/**
 * @author mesbah
 * @version $Id$
 */
public abstract class AbstractWebDriver implements EmbeddedBrowser {
	private static Logger logger = Logger.getLogger(WebDriver.class.getName());
	private WebDriver browser;

	/**
	 * @param browser
	 *            the browser to set
	 */
	protected void setBrowser(WebDriver browser) {
		this.browser = browser;
	}

	/**
	 * Public constructor.
	 * 
	 * @param logger
	 *            the log4j logger.
	 */
	public AbstractWebDriver(Logger logger) {
		AbstractWebDriver.logger = logger;
	}

	/**
	 * Constructor.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 */
	public AbstractWebDriver(WebDriver driver) {
		this.browser = driver;
	}

	/**
	 * @param url
	 *            The URL.
	 * @throws InterruptedException
	 * @throws CrawljaxException
	 *             if fails.
	 */
	public void goToUrl(String url) throws CrawljaxException {
		browser.navigate().to(url);
		handlePopups();
		try {
			Thread.sleep(PropertyHelper.getCrawlWaitReloadValue());
		} catch (InterruptedException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

	}

	/**
	 * alert, prompt, and confirm behave as if the OK button is always clicked.
	 */
	private void handlePopups() {
		executeJavaScript("window.alert = function(msg){return true;};"
		        + "window.confirm = function(msg){return true;};"
		        + "window.prompt = function(msg){return true;};");
	}

	/**
	 * Fires the event and waits for a specified time.
	 * 
	 * @param webElement
	 * @param eventable
	 *            The HTML event type (onclick, onmouseover, ...).
	 * @throws Exception
	 *             if fails.
	 */
	private boolean fireEventWait(WebElement webElement, Eventable eventable)
	        throws CrawljaxException {
		String eventType = eventable.getEventType();

		if ("onclick".equals(eventType)) {
			try {
				webElement.click();
			} catch (ElementNotVisibleException e1) {
				logger.info("Element not visible, so cannot be clicked: "
				        + webElement.getTagName().toUpperCase() + " " + webElement.getText());
				return false;
			} catch (Exception e) {
				logger.error(e.getMessage());
				return false;
			}
		} else {
			logger.info("EventType " + eventType + " not supported in WebDriver.");
		}

		try {
			Thread.sleep(PropertyHelper.getCrawlWaitEventValue());
		} catch (InterruptedException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * @see EmbeddedBrowser#close()
	 */
	public void close() {
		logger.info("Closing the browser...");
		// close browser and close every associated window.
		browser.quit();
	}

	/**
	 * @return a string representation of the borser's DOM.
	 * @throws CrawljaxException
	 *             if fails.
	 * @see com.crawljax.browser.EmbeddedBrowser#getDom()
	 */
	public String getDom() throws CrawljaxException {
		try {
			return toUniformDOM(getDomTreeWithFrames());
		} catch (Exception e) {
			throw new CrawljaxException(e.getMessage(), e);
		}
	}

	/**
	 * @param html
	 *            The html string.
	 * @return uniform version of dom with predefined attributes stripped
	 * @throws Exception
	 *             On error.
	 */
	private static String toUniformDOM(Document doc) throws Exception {

		String html = Helper.getDocumentToString(doc);

		Pattern p =
		        Pattern.compile("<SCRIPT(.*?)</SCRIPT>", Pattern.DOTALL
		                | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		String htmlFormatted = m.replaceAll("");

		p = Pattern.compile("<\\?xml:(.*?)>");
		m = p.matcher(html);
		htmlFormatted = m.replaceAll("");

		// html = html.replace("<?xml:namespace prefix = gwt >", "");

		doc = Helper.getDocument(htmlFormatted);
		htmlFormatted = Helper.getDocumentToString(doc);
		htmlFormatted = Helper.filterAttributes(htmlFormatted);
		return htmlFormatted;
	}

	/**
	 * @see com.crawljax.browser.EmbeddedBrowser#goBack()
	 */
	public void goBack() {
		browser.navigate().back();
	}

	/**
	 * @return true if succeeds.
	 * @see com.crawljax.browser.EmbeddedBrowser#canGoBack()
	 */
	public boolean canGoBack() {
		// NOT IMPLEMENTED
		return false;
	}

	/**
	 * @param clickable
	 *            The clickable object.
	 * @param text
	 *            The input.
	 * @return true if succeeds.
	 */
	public boolean input(Eventable clickable, String text) {
		WebElement field = browser.findElement(clickable.getIdentification().getWebDriverBy());

		if (field != null) {
			field.sendKeys(text);

			// this.activeElement = field;
			return true;
		}

		return false;
	}

	/**
	 * Fires an event on an element using its identification.
	 * 
	 * @param eventable
	 *            The eventable.
	 * @return true if it is able to fire the event successfully on the element.
	 * @throws CrawljaxException
	 *             On failure.
	 */
	public synchronized boolean fireEvent(Eventable eventable) throws CrawljaxException {
		try {

			if (eventable.getRelatedFrame() != null && !eventable.getRelatedFrame().equals("")) {
				browser.switchTo().frame(eventable.getRelatedFrame());
			}

			WebElement webElement =
			        browser.findElement(eventable.getIdentification().getWebDriverBy());

			if (webElement != null) {
				return fireEventWait(webElement, eventable);
			}

			return false;
		} catch (NoSuchElementException e) {
			logger.info("Could not fire eventable: " + eventable.toString());
			return false;
		} catch (RuntimeException e) {
			logger.error("Caught Exception: " + e.getMessage(), e);

			return false;
		}
	}

	/**
	 * @return the browser instance.
	 */
	public WebDriver getBrowser() {
		return browser;
	}

	/**
	 * Execute JavaScript in the browser.
	 * 
	 * @param code
	 *            The code to execute.
	 * @return The return value of the JavaScript.
	 */
	public Object executeJavaScript(String code) {
		JavascriptExecutor js = (JavascriptExecutor) browser;
		return js.executeScript(code);
	}

	/**
	 * Determines whether locater is visible.
	 * 
	 * @param locater
	 *            The element to search for.
	 * @return Whether it is visible.
	 */
	public boolean isVisible(By locater) {
		try {
			WebElement el = browser.findElement(locater);
			return ((RenderedWebElement) el).isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @return The current browser url.
	 */
	public String getCurrentUrl() {
		return browser.getCurrentUrl();
	}

	/**
	 * @return the webdriver instance.
	 */
	public WebDriver getDriver() {
		return this.browser;
	}

	@Override
	public void closeOtherWindows() {
		if (browser instanceof FirefoxDriver) {
			for (String handle : browser.getWindowHandles()) {
				if (!handle.equals(browser.getWindowHandle())) {
					String current = browser.getWindowHandle();
					browser.switchTo().window(handle);
					logger.info("Closing other window with title \"" + browser.getTitle() + "\"");
					browser.close();
					browser.switchTo().window(current);
				}
			}
		}

	}

	/**
	 * @param file
	 *            the file to write to the filename to save the screenshot in.
	 */
	public void saveScreenShot(File file) {
		if (browser instanceof FirefoxDriver) {
			((FirefoxDriver) browser).saveScreenshot(file);
			removeCanvasGeneratedByFirefoxDriverForScreenshots();
		} else {
			logger.warn("Screenshot not supported.");
		}
	}

	private void removeCanvasGeneratedByFirefoxDriverForScreenshots() {
		String js = "";
		js += "var canvas = document.getElementById('fxdriver-screenshot-canvas');";
		js += "if(canvas != null){";
		js += "canvas.parentNode.removeChild(canvas);";
		js += "}";
		try {
			executeJavaScript(js);
		} catch (Exception e) {
			logger.info("Could not remove the screenshot canvas from the DOM.");
		}
	}

	private String getFrameIdentification(Element frame, int index) {

		Attr attr = (Attr) frame.getAttributeNode("name");
		if (attr != null && attr.getNodeValue() != null && !attr.getNodeValue().equals("")) {
			return attr.getNodeValue();
		}

		attr = (Attr) frame.getAttributeNode("id");
		if (attr != null && attr.getNodeValue() != null && !attr.getNodeValue().equals("")) {
			return attr.getNodeValue();
		}

		return "" + index;
	}

	@Override
	public abstract EmbeddedBrowser clone();

	/**
	 * @return a Document object containing the contents of iframes as well.
	 * @throws CrawljaxException
	 *             if an exception is thrown.
	 */
	private Document getDomTreeWithFrames() throws CrawljaxException {

		Document document;
		try {
			document = Helper.getDocument(browser.getPageSource());
			appendFrameContent(browser.getWindowHandle(), document.getDocumentElement(),
			        document, "");
		} catch (SAXException e) {
			throw new CrawljaxException(e.getMessage(), e);
		} catch (IOException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

		return document;
	}

	private void appendFrameContent(String windowHandle, Element orig, Document document,
	        String topFrame) throws SAXException, IOException {

		NodeList frameNodes = orig.getElementsByTagName("IFRAME");

		int nodes = frameNodes.getLength();
		browser.switchTo().window(windowHandle);

		for (int i = 0; i < nodes; i++) {
			String frameIdentification = "";

			if (topFrame != null && !topFrame.equals("")) {
				frameIdentification += topFrame + ".";
			}

			Element frameElement = (Element) frameNodes.item(i);
			frameIdentification += getFrameIdentification(frameElement, i);

			logger.info("frame-identification: " + frameIdentification);

			String toAppend = browser.switchTo().frame(frameIdentification).getPageSource();

			Element toAppendElement = Helper.getDocument(toAppend).getDocumentElement();
			toAppendElement = (Element) document.importNode(toAppendElement, true);
			frameElement.appendChild(toAppendElement);

			appendFrameContent(windowHandle, toAppendElement, document, frameIdentification);

			browser.switchTo().window(windowHandle);
		}

	}
}
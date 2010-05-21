package com.crawljax.browser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.NotSupportedException;

import org.apache.log4j.Logger;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FileHandler;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import com.crawljax.forms.RandomInputValueGenerator;
import com.crawljax.util.Helper;

/**
 * @author mesbah
 * @author Frank Groeneveld
 * @version $Id$
 */
public final class WebDriverBackedEmbeddedBrowser implements EmbeddedBrowser {
	private final long crawlWaitEvent;
	private static final Logger LOGGER = Logger.getLogger(WebDriverBackedEmbeddedBrowser.class);
	private final WebDriver browser;

	private final List<String> filterAttributes;
	private final long crawlWaitReload;

	/**
	 * Constructor.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param logger
	 *            the logger instance.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 */
	private WebDriverBackedEmbeddedBrowser(WebDriver driver, List<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		this.browser = driver;
		this.filterAttributes = filterAttributes;
		this.crawlWaitEvent = crawlWaitEvent;
		this.crawlWaitReload = crawlWaitReload;
	}

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 * 
	 * @param hubUrl
	 *            Url of the server.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl,
	        List<String> filterAttributes, long crawlWaitEvent, long crawlWaitReload) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setPlatform(Platform.ANY);
		URL url;
		try {
			url = new URL(hubUrl);
		} catch (MalformedURLException e) {
			LOGGER.error("The given hub url of the remote server is malformed can not continue!",
			        e);
			return null;
		}
		HttpCommandExecutor executor = null;
		try {
			executor = new HttpCommandExecutor(url);
		} catch (Exception e) {
			LOGGER.error("Received unknown exception while creating the "
			        + "HttpCommandExecutor, can not continue!", e);
			return null;
		}
		return WebDriverBackedEmbeddedBrowser.withDriver(new RemoteWebDriver(executor,
		        capabilities), filterAttributes, crawlWaitEvent, crawlWaitReload);
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 * 
	 * @param driver
	 *            The WebDriver to use.
	 * @param filterAttributes
	 *            the attributes to be filtered from DOM.
	 * @param crawlWaitReload
	 *            the period to wait after a reload.
	 * @param crawlWaitEvent
	 *            the period to wait after an event is fired.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver,
	        List<String> filterAttributes, long crawlWaitEvent, long crawlWaitReload) {
		return new WebDriverBackedEmbeddedBrowser(driver, filterAttributes, crawlWaitEvent,
		        crawlWaitReload);
	}

	/**
	 * @param url
	 *            The URL.
	 * @throws CrawljaxException
	 *             if fails.
	 */
	public void goToUrl(String url) throws CrawljaxException {
		browser.navigate().to(url);
		try {
			Thread.sleep(this.crawlWaitReload);
			handlePopups();
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
	 *            the element to fire event on.
	 * @param eventable
	 *            The HTML event type (onclick, onmouseover, ...).
	 * @return true if firing event is successful.
	 * @throws CrawljaxException
	 *             if fails.
	 */
	protected boolean fireEventWait(WebElement webElement, Eventable eventable)
	        throws CrawljaxException {

		switch (eventable.getEventType()) {
			case click:
				try {
					webElement.click();
				} catch (ElementNotVisibleException e1) {
					LOGGER.info("Element not visible, so cannot be clicked: "
					        + webElement.getTagName().toUpperCase() + " " + webElement.getText());
					return false;
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
					return false;
				}
				break;
			case hover:
				// todo
				break;

			default:
				LOGGER.info("EventType " + eventable.getEventType()
				        + " not supported in WebDriver.");
				return false;
		}

		try {
			Thread.sleep(this.crawlWaitEvent);
		} catch (InterruptedException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

		return true;
	}

	@Override
	public void close() {
		LOGGER.info("Closing the browser...");
		// close browser and close every associated window.
		browser.quit();
	}

	@Override
	public String getDom() throws CrawljaxException {
		try {
			return toUniformDOM(Helper.getDocumentToString(getDomTreeWithFrames()));
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
	private String toUniformDOM(String html) throws Exception {

		Pattern p =
		        Pattern.compile("<SCRIPT(.*?)</SCRIPT>", Pattern.DOTALL
		                | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		String htmlFormatted = m.replaceAll("");

		p = Pattern.compile("<\\?xml:(.*?)>");
		m = p.matcher(html);
		htmlFormatted = m.replaceAll("");

		// html = html.replace("<?xml:namespace prefix = gwt >", "");

		Document doc = Helper.getDocument(htmlFormatted);
		htmlFormatted = Helper.getDocumentToString(doc);
		htmlFormatted = filterAttributes(htmlFormatted);
		return htmlFormatted;
	}

	/**
	 * Filters attributes from the HTML string.
	 * 
	 * @param html
	 *            The HTML to filter.
	 * @return The filtered HTML string.
	 */
	private String filterAttributes(String html) {
		if (this.filterAttributes != null) {
			for (String attribute : this.filterAttributes) {
				String regex = "\\s" + attribute + "=\"[^\"]*\"";
				Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(html);
				html = m.replaceAll("");
			}
		}
		return html;
	}

	@Override
	public void goBack() {
		browser.navigate().back();
	}

	/**
	 * @param identification
	 *            The identification object.
	 * @param text
	 *            The input.
	 * @return true if succeeds.
	 */
	public boolean input(Identification identification, String text) {
		WebElement field = browser.findElement(identification.getWebDriverBy());

		if (field != null) {
			// first clear the field
			field.clear();
			// then fill in
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

			boolean handleChanged = false;
			boolean result = false;

			if (eventable.getRelatedFrame() != null && !eventable.getRelatedFrame().equals("")) {
				LOGGER.debug("switching to frame: " + eventable.getRelatedFrame());
				try {
					browser.switchTo().frame(eventable.getRelatedFrame());
				} catch (NoSuchFrameException e) {
					LOGGER.debug("Frame not found, possibily while back-tracking..", e);
					// TODO Stefan, This exception is catched to prevent stopping from working
					// This was the case on the Gmail case; findout if not switching (catching)
					// Results in good performance...
				}
				handleChanged = true;
			}

			WebElement webElement =
			        browser.findElement(eventable.getIdentification().getWebDriverBy());

			if (webElement != null) {
				result = fireEventWait(webElement, eventable);
			}

			if (handleChanged) {
				browser.switchTo().defaultContent();
			}

			return result;

		} catch (NoSuchElementException e) {

			LOGGER.warn("Could not fire eventable: " + eventable.toString());
			return false;
		} catch (RuntimeException e) {
			LOGGER.error("Caught Exception: " + e.getMessage(), e);

			return false;
		}
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
	 * Determines whether the corresponding element is visible.
	 * 
	 * @param identification
	 *            The element to search for.
	 * @return true if the element is visible
	 */
	public boolean isVisible(Identification identification) {
		try {
			WebElement el = browser.findElement(identification.getWebDriverBy());
			if (el != null) {
				return ((RenderedWebElement) el).isDisplayed();
			}

			return false;
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

	@Override
	public void closeOtherWindows() {
		String current = browser.getWindowHandle();
		for (String handle : browser.getWindowHandles()) {
			if (!handle.equals(browser.getWindowHandle())) {

				browser.switchTo().window(handle);
				LOGGER.info("Closing other window with title \"" + browser.getTitle() + "\"");
				browser.close();
				// browser.switchTo().defaultContent();
				browser.switchTo().window(current);
			}
		}
	}

	/**
	 * @return a Document object containing the contents of iframes as well.
	 * @throws CrawljaxException
	 *             if an exception is thrown.
	 */
	private Document getDomTreeWithFrames() throws CrawljaxException {

		Document document;
		try {
			String s = "";
			try {
				s = browser.getPageSource();
			} catch (WebDriverException e) {
				if (e.getMessage().contains(
				        "Utils.getDocument(respond.context)."
				                + "getElementsByTagName(\\\"html\\\")[0] is undefined")) {
					// There is no html tag... ignore!
					// TODO Stefan find out if this error is a Webdriver bug??
					LOGGER.info("Skiped parsing dom tree because no html content is defined");
				} else {
					throw new CrawljaxException(e.getMessage(), e);
				}
			}
			document = Helper.getDocument(s);
			appendFrameContent(document.getDocumentElement(), document, "");
		} catch (SAXException e) {
			throw new CrawljaxException(e.getMessage(), e);
		} catch (IOException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

		return document;
	}

	private void appendFrameContent(Element orig, Document document, String topFrame)
	        throws SAXException, IOException {

		NodeList frameNodes = orig.getElementsByTagName("IFRAME");

		List<Element> nodeList = new ArrayList<Element>();

		for (int i = 0; i < frameNodes.getLength(); i++) {
			Element frameElement = (Element) frameNodes.item(i);
			nodeList.add(frameElement);
		}

		for (int i = 0; i < nodeList.size(); i++) {
			String frameIdentification = "";

			if (topFrame != null && !topFrame.equals("")) {
				frameIdentification += topFrame + ".";
			}

			Element frameElement = nodeList.get(i);

			String nameId = Helper.getFrameIdentification(frameElement);

			if (nameId != null) {
				frameIdentification += nameId;

				String handle = new String(browser.getWindowHandle());

				LOGGER.debug("The current H: " + handle);

				try {

					LOGGER.debug("switching to frame: " + frameIdentification);
					browser.switchTo().frame(frameIdentification);
					String toAppend = new String(browser.getPageSource());

					LOGGER.debug("frame dom: " + toAppend);

					browser.switchTo().defaultContent();

					LOGGER.debug("default handle window source: " + browser.getPageSource());

					Element toAppendElement = Helper.getDocument(toAppend).getDocumentElement();
					Element importedElement =
					        (Element) document.importNode(toAppendElement, true);
					frameElement.appendChild(importedElement);

					appendFrameContent(importedElement, document, frameIdentification);

				} catch (Exception e) {
					LOGGER.info("Got exception while inspecting a frame:" + frameIdentification
					        + " continuing...", e);
				}

			}
		}

	}

	/**
	 * @return the dom without the iframe contents.
	 * @throws CrawljaxException
	 *             if it fails.
	 * @see com.crawljax.browser.EmbeddedBrowser#getDomWithoutIframeContent().
	 */
	public String getDomWithoutIframeContent() throws CrawljaxException {

		try {
			String dom = browser.getPageSource();
			// logger.debug("driver.source: " + dom);
			String result = toUniformDOM(dom);
			// logger.debug("driver.source toUniformDom: " + result);
			return result;
		} catch (Exception e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

	}

	/**
	 * @param input
	 *            the input to be filled.
	 * @return FormInput with random value assigned if possible
	 */
	public FormInput getInputWithRandomValue(FormInput input) {

		WebElement webElement;
		try {
			webElement = browser.findElement(input.getIdentification().getWebDriverBy());
			if (!((RenderedWebElement) webElement).isDisplayed()) {
				return null;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}

		Set<InputValue> values = new HashSet<InputValue>();

		// create some random value

		if (input.getType().toLowerCase().startsWith("text")) {
			values.add(new InputValue(new RandomInputValueGenerator()
			        .getRandomString(FormHandler.RANDOM_STRING_LENGTH), true));
		} else if (input.getType().equalsIgnoreCase("checkbox")
		        || input.getType().equalsIgnoreCase("radio") && !webElement.isSelected()) {
			if (new RandomInputValueGenerator().getCheck()) {
				values.add(new InputValue("1", true));
			} else {
				values.add(new InputValue("0", false));

			}
		} else if (input.getType().equalsIgnoreCase("select")) {
			try {
				Select select = new Select(webElement);
				WebElement option =
				        (WebElement) new RandomInputValueGenerator().getRandomOption(select
				                .getOptions());
				values.add(new InputValue(option.getText(), true));
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				return null;
			}
		}

		if (values.size() == 0) {
			return null;
		}
		input.setInputValues(values);
		return input;

	}

	@Override
	public String getFrameDom(String iframeIdentification) {

		LOGGER.debug("switching to frame: " + iframeIdentification);
		browser.switchTo().frame(iframeIdentification);

		// make a copy of the dom before changing into the top page
		String frameDom = new String(browser.getPageSource());

		browser.switchTo().defaultContent();

		return frameDom;
	}

	/**
	 * @param identification
	 *            the identification of the element.
	 * @return true if the element can be found in the DOM tree.
	 */
	public boolean elementExists(Identification identification) {
		WebElement el = browser.findElement(identification.getWebDriverBy());
		return el != null;
	}

	/**
	 * @param identification
	 *            the identification of the element.
	 * @return the found element.
	 */
	public WebElement getWebElement(Identification identification) {
		return browser.findElement(identification.getWebDriverBy());
	}

	/**
	 * @return the period to wait after an event.
	 */
	protected long getCrawlWaitEvent() {
		return crawlWaitEvent;
	}

	/**
	 * @return the list of attributes to be filtered from DOM.
	 */
	protected List<String> getFilterAttributes() {
		return filterAttributes;
	}

	/**
	 * @return the period to waint after a reload.
	 */
	protected long getCrawlWaitReload() {
		return crawlWaitReload;
	}

	@Override
	public void saveScreenShot(File file) throws NotSupportedException {
		if (browser instanceof TakesScreenshot) {
			File tmpfile = ((TakesScreenshot) browser).getScreenshotAs(OutputType.FILE);

			try {
				FileHandler.copy(tmpfile, file);
			} catch (IOException e) {
				throw new WebDriverException(e);
			}

			removeCanvasGeneratedByFirefoxDriverForScreenshots();
		} else {
			throw new NotSupportedException("Your current WebDriver doesn't support screenshots.");
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
			LOGGER.warn("Could not remove the screenshot canvas from the DOM.");
		}
	}

}
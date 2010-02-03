package com.crawljax.browser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
 * @version $Id$
 */
public abstract class AbstractWebDriver implements EmbeddedBrowser {
	private final long crawlWaitEvent;
	private final Logger logger;
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
	public AbstractWebDriver(WebDriver driver, Logger logger, List<String> filterAttributes,
	        long crawlWaitReload, long crawlWaitEvent) {
		this.browser = driver;
		this.logger = logger;
		this.filterAttributes = filterAttributes;
		this.crawlWaitEvent = crawlWaitEvent;
		this.crawlWaitReload = crawlWaitReload;
	}

	/**
	 * @param url
	 *            The URL.
	 * @throws CrawljaxException
	 *             if fails.
	 */
	public void goToUrl(String url) throws CrawljaxException {
		browser.navigate().to(url);
		handlePopups();
		try {
			Thread.sleep(this.crawlWaitReload);
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

		switch (eventable.getEventType()) {
			case click:
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
				break;
			case hover:
				// todo
				break;

			default:
				logger.info("EventType " + eventable.getEventType()
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

	/**
	 * @see com.crawljax.browser.EmbeddedBrowser#goBack()
	 */
	public void goBack() {
		browser.navigate().back();
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
			String handle = browser.getWindowHandle();

			boolean result = false;

			if (eventable.getRelatedFrame() != null && !eventable.getRelatedFrame().equals("")) {
				browser.switchTo().frame(eventable.getRelatedFrame());
			}

			WebElement webElement =
			        browser.findElement(eventable.getIdentification().getWebDriverBy());

			if (webElement != null) {
				result = fireEventWait(webElement, eventable);
			}

			browser.switchTo().window(handle);

			return result;

		} catch (NoSuchElementException e) {

			logger.warn("Could not fire eventable: " + eventable.toString());
			return false;
		} catch (RuntimeException e) {
			logger.error("Caught Exception: " + e.getMessage(), e);

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

		browser.switchTo().window(windowHandle);

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

				logger.debug("frame-identification: " + frameIdentification);

				String toAppend = browser.switchTo().frame(frameIdentification).getPageSource();

				Element toAppendElement = Helper.getDocument(toAppend).getDocumentElement();
				Element importedElement = (Element) document.importNode(toAppendElement, true);
				frameElement.appendChild(importedElement);

				appendFrameContent(windowHandle, importedElement, document, frameIdentification);
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
			return toUniformDOM(browser.getPageSource());
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
			logger.error(e.getMessage(), e);
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
				logger.error(e.getMessage(), e);
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
		String handle = browser.getWindowHandle();

		browser.switchTo().frame(iframeIdentification);

		// make a copy of the dom before changing into the top page
		String frameDom = new String(browser.getPageSource());

		browser.switchTo().window(handle);

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

}
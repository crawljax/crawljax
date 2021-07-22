package com.crawljax.browser;

import com.assertthat.selenium_shutterbug.core.Shutterbug;
import com.assertthat.selenium_shutterbug.utils.web.ScrollStrategy;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.AcceptAllFramesChecker;
import com.crawljax.core.configuration.IgnoreFrameChecker;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import com.crawljax.forms.RandomInputValueGenerator;
import com.crawljax.util.DomUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Files;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ErrorHandler.UnknownServerException;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;
import ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebDriverBackedEmbeddedBrowser implements EmbeddedBrowser {

	private static final Logger LOGGER =
			LoggerFactory.getLogger(WebDriverBackedEmbeddedBrowser.class);

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 *
	 * @param hubUrl           Url of the server.
	 * @param filterAttributes the attributes to be filtered from DOM.
	 * @param crawlWaitReload  the period to wait after a reload.
	 * @param crawlWaitEvent   the period to wait after an event is fired.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl,
			ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
			long crawlWaitReload) {
		return WebDriverBackedEmbeddedBrowser.withDriver(buildRemoteWebDriver(hubUrl),
				filterAttributes, crawlWaitEvent,
				crawlWaitReload);
	}

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 *
	 * @param hubUrl             Url of the server.
	 * @param filterAttributes   the attributes to be filtered from DOM.
	 * @param crawlWaitReload    the period to wait after a reload.
	 * @param crawlWaitEvent     the period to wait after an event is fired.
	 * @param ignoreFrameChecker the checker used to determine if a certain frame must be ignored.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl,
			ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
			long crawlWaitReload,
			IgnoreFrameChecker ignoreFrameChecker) {
		return WebDriverBackedEmbeddedBrowser.withDriver(buildRemoteWebDriver(hubUrl),
				filterAttributes, crawlWaitEvent,
				crawlWaitReload, ignoreFrameChecker);
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 *
	 * @param driver           The WebDriver to use.
	 * @param filterAttributes the attributes to be filtered from DOM.
	 * @param crawlWaitReload  the period to wait after a reload.
	 * @param crawlWaitEvent   the period to wait after an event is fired.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver,
			ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
			long crawlWaitReload) {
		return new WebDriverBackedEmbeddedBrowser(driver, filterAttributes, crawlWaitEvent,
				crawlWaitReload);
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 *
	 * @param driver             The WebDriver to use.
	 * @param filterAttributes   the attributes to be filtered from DOM.
	 * @param crawlWaitReload    the period to wait after a reload.
	 * @param crawlWaitEvent     the period to wait after an event is fired.
	 * @param ignoreFrameChecker the checker used to determine if a certain frame must be ignored.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver,
			ImmutableSortedSet<String> filterAttributes, long crawlWaitEvent,
			long crawlWaitReload,
			IgnoreFrameChecker ignoreFrameChecker) {
		return new WebDriverBackedEmbeddedBrowser(driver, filterAttributes, crawlWaitEvent,
				crawlWaitReload,
				ignoreFrameChecker);
	}

	/**
	 * Create a RemoteWebDriver backed EmbeddedBrowser.
	 *
	 * @param hubUrl Url of the server.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withRemoteDriver(String hubUrl) {
		return WebDriverBackedEmbeddedBrowser.withDriver(buildRemoteWebDriver(hubUrl));
	}

	/**
	 * Private used static method for creation of a RemoteWebDriver. Taking care of the default
	 * Capabilities and using the HttpCommandExecutor.
	 *
	 * @param hubUrl the url of the hub to use.
	 * @return the RemoteWebDriver instance.
	 */
	private static RemoteWebDriver buildRemoteWebDriver(String hubUrl) {
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
			// TODO Stefan; refactor this catch, this will definitely result in
			// NullPointers, why
			// not throw RuntimeException direct?
			LOGGER.error(
					"Received unknown exception while creating the "
							+ "HttpCommandExecutor, can not continue!",
					e);
			return null;
		}
		return new RemoteWebDriver(executor, capabilities);
	}

	private int pixelDensity = -1;

	private final ImmutableSortedSet<String> filterAttributes;
	private final WebDriver browser;

	private long crawlWaitEvent;
	private long crawlWaitReload;
	private IgnoreFrameChecker ignoreFrameChecker = new AcceptAllFramesChecker();

	/**
	 * Constructor without configuration values.
	 *
	 * @param driver The WebDriver to use.
	 */
	private WebDriverBackedEmbeddedBrowser(WebDriver driver) {
		this.browser = driver;
		filterAttributes = ImmutableSortedSet.of();
	}

	/**
	 * Constructor.
	 *
	 * @param driver           The WebDriver to use.
	 * @param filterAttributes the attributes to be filtered from DOM.
	 * @param crawlWaitReload  the period to wait after a reload.
	 * @param crawlWaitEvent   the period to wait after an event is fired.
	 */
	private WebDriverBackedEmbeddedBrowser(WebDriver driver,
			ImmutableSortedSet<String> filterAttributes,
			long crawlWaitReload, long crawlWaitEvent) {
		this.browser = driver;
		this.filterAttributes = Preconditions.checkNotNull(filterAttributes);
		this.crawlWaitEvent = crawlWaitEvent;
		this.crawlWaitReload = crawlWaitReload;
	}

	/**
	 * Constructor.
	 *
	 * @param driver             The WebDriver to use.
	 * @param filterAttributes   the attributes to be filtered from DOM.
	 * @param crawlWaitReload    the period to wait after a reload.
	 * @param crawlWaitEvent     the period to wait after an event is fired.
	 * @param ignoreFrameChecker the checker used to determine if a certain frame must be ignored.
	 */
	private WebDriverBackedEmbeddedBrowser(WebDriver driver,
			ImmutableSortedSet<String> filterAttributes,
			long crawlWaitReload, long crawlWaitEvent, IgnoreFrameChecker ignoreFrameChecker) {
		this(driver, filterAttributes, crawlWaitReload, crawlWaitEvent);
		this.ignoreFrameChecker = ignoreFrameChecker;
	}

	/**
	 * Create a WebDriver backed EmbeddedBrowser.
	 *
	 * @param driver The WebDriver to use.
	 * @return The EmbeddedBrowser.
	 */
	public static WebDriverBackedEmbeddedBrowser withDriver(WebDriver driver) {
		return new WebDriverBackedEmbeddedBrowser(driver);
	}

	/**
	 * @param url The URL.
	 */
	@Override
	public void goToUrl(URI url) {
		try {
			browser.navigate().to(url.toString());
			Thread.sleep(this.crawlWaitReload);
			handlePopups();
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return;
		} catch (InterruptedException e) {
			LOGGER.debug("goToUrl got interrupted while waiting for the page to be loaded", e);
			Thread.currentThread().interrupt();
			return;
		}
	}

	/**
	 * alert, prompt, and confirm behave as if the OK button is always clicked.
	 */
	@Override
	public void handlePopups() {
		/*
		 * try { executeJavaScript("window.alert = function(msg){return true;};" +
		 * "window.confirm = function(msg){return true;};" +
		 * "window.prompt = function(msg){return true;};"); } catch (CrawljaxException e) {
		 * LOGGER.error("Handling of PopUp windows failed", e); }
		 */

		/* Workaround: Popups handling currently not supported in PhantomJS. */
		if (browser instanceof PhantomJSDriver) {
			return;
		}

		if (ExpectedConditions.alertIsPresent().apply(browser) != null) {
			try {
				browser.switchTo().alert().accept();
				LOGGER.info("Alert accepted");
			} catch (Exception e) {
				LOGGER.error("Handling of PopUp windows failed");
			}
		}
	}

	/**
	 * Fires the event and waits for a specified time.
	 *
	 * @param webElement the element to fire event on.
	 * @param eventable  The HTML event type (onclick, onmouseover, ...).
	 * @return true if firing event is successful.
	 * @throws InterruptedException when interrupted during the wait.
	 */
	private boolean fireEventWait(WebElement webElement, Eventable eventable)
			throws ElementNotVisibleException, InterruptedException {
		switch (eventable.getEventType()) {
			case click:
				try {
					webElement.click();
				} catch (ElementNotVisibleException e) {
					throw e;
				} catch (WebDriverException e) {
					throwIfConnectionException(e);
					throwIfNotInteractableException(e);
					return false;
				}
				break;
			case enter:
				try {
					webElement.sendKeys(Keys.RETURN);
				} catch (ElementNotVisibleException e) {
					throw e;
				} catch (WebDriverException e) {
					throwIfConnectionException(e);
					throwIfNotInteractableException(e);
					return false;
				}
				break;
			case hover:
				LOGGER.info("EventType hover called but this isn't implemented yet");
				break;
			default:
				LOGGER.info("EventType {} not supported in WebDriver.", eventable.getEventType());
				return false;
		}

		Thread.sleep(this.crawlWaitEvent);
		return true;
	}

	@Override
	public void close() {
		LOGGER.info("Closing the browser...");
		try {
			// close browser and close every associated window.
			browser.quit();
		} catch (WebDriverException e) {
			if (e.getCause() instanceof InterruptedException
					|| e.getCause().getCause() instanceof InterruptedException) {
				LOGGER.info(
						"Interrupted while waiting for the browser to close. It might not close correctly");
				Thread.currentThread().interrupt();
				return;
			}
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
		LOGGER.debug("Browser closed...");
	}

	@Override
	@Deprecated
	public String getDom() {
		return getStrippedDom();
	}

	@Override
	public String getStrippedDom() {

		try {
//			String dom = toUniformDOM(DomUtils.getDocumentToString(getDomTreeWithFrames_GoldStandards()));
			String dom = toUniformDOM(DomUtils.getDocumentToString(getDomTreeWithFrames()));
			LOGGER.trace(dom);
			return dom;
		} catch (WebDriverException | CrawljaxException e) {
			LOGGER.warn("Could not get the dom", e);
			return "";
		}
	}

	@Override
	public String getUnStrippedDom() {
		return browser.getPageSource();
	}

	/**
	 * @param html The html string.
	 * @return uniform version of dom with predefined attributes stripped
	 */
	private String toUniformDOM(String html) {

		Pattern p = Pattern.compile("<SCRIPT(.*?)</SCRIPT>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		String htmlFormatted = m.replaceAll("");

		p = Pattern.compile("<\\?xml:(.*?)>");
		m = p.matcher(htmlFormatted);
		htmlFormatted = m.replaceAll("");

		htmlFormatted = filterAttributes(htmlFormatted);
		return htmlFormatted;
	}

	/**
	 * Filters attributes from the HTML string.
	 *
	 * @param html The HTML to filter.
	 * @return The filtered HTML string.
	 */
	private String filterAttributes(String html) {
		String filteredHtml = html;
		for (String attribute : this.filterAttributes) {
			String regex = "\\s" + attribute + "=\"[^\"]*\"";
			Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(html);
			filteredHtml = m.replaceAll("");
		}
		return filteredHtml;
	}

	@Override
	public void goBack() {
		try {
			browser.navigate().back();
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	/**
	 * @param identification The identification object.
	 * @param text           The input.
	 * @return true if succeeds.
	 */
	@Override
	public boolean input(Identification identification, String text) {
		try {
			WebElement field = browser.findElement(identification.getWebDriverBy());
			if (field != null) {
				field.clear();
				field.sendKeys(text);

				return true;
			}
			return false;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return false;
		}
	}

	/**
	 * Fires an event on an element using its identification.
	 *
	 * @param eventable The eventable.
	 * @return true if it is able to fire the event successfully on the element.
	 * @throws InterruptedException when interrupted during the wait.
	 */
	@Override
	public synchronized boolean fireEventAndWait(Eventable eventable)
			throws ElementNotVisibleException, NoSuchElementException, InterruptedException {
		try {

			boolean handleChanged = false;
			boolean result = false;

			if (eventable.getRelatedFrame() != null && !eventable.getRelatedFrame().equals("")) {
				LOGGER.debug("switching to frame: " + eventable.getRelatedFrame());
				try {

					switchToFrame(eventable.getRelatedFrame());
				} catch (NoSuchFrameException e) {
					LOGGER.debug("Frame not found, possibly while back-tracking..", e);
					// TODO Stefan, This exception is caught to prevent stopping
					// from working
					// This was the case on the Gmail case; find out if not switching
					// (catching)
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
		} catch (ElementNotVisibleException | NoSuchElementException e) {
			throw e;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			throwIfNotInteractableException(e);
			return false;
		}
	}

	/**
	 * Execute JavaScript in the browser.
	 *
	 * @param code The code to execute.
	 * @return The return value of the JavaScript.
	 * @throws CrawljaxException when javascript execution failed.
	 */
	@Override
	public Object executeJavaScript(String code) throws CrawljaxException {
		try {
			JavascriptExecutor js = (JavascriptExecutor) browser;
			return js.executeScript(code);
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			throw new CrawljaxException(e);
		}
	}

	/**
	 * Determines whether the corresponding element is visible.
	 *
	 * @param identification The element to search for.
	 * @return true if the element is visible
	 */
	@Override
	public boolean isVisible(Identification identification) {
		try {
			WebElement el = browser.findElement(identification.getWebDriverBy());
			if (el != null) {
				return el.isDisplayed();
			}

			return false;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return false;
		}
	}

	/**
	 * @return The current browser url.
	 */
	@Override
	public String getCurrentUrl() {
		try {
			return browser.getCurrentUrl();
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	@Override
	public void closeOtherWindows() {
		try {
			String current = browser.getWindowHandle();
			for (String handle : browser.getWindowHandles()) {
				if (!handle.equals(browser.getWindowHandle())) {
					browser.switchTo().window(handle);
					LOGGER.debug("Closing other window with title \"{}\"", browser.getTitle());
					browser.close();
					browser.switchTo().window(current);
				}
			}
		} catch (UnhandledAlertException e) {
			LOGGER.warn("While closing the window, an alert got ignored: {}", e.getMessage());
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	/**
	 * @return a Document object containing the contents of iframes as well.
	 * @throws CrawljaxException if an exception is thrown.
	 */
	private Document getDomTreeWithFrames() throws CrawljaxException {

		try {
			Document document = DomUtils.asDocument(browser.getPageSource());
			appendFrameContent(document.getDocumentElement(), document, "");
			return document;
		} catch (IOException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

	}
	
	
	/**
	 * @return a Document object containing the contents of iframes as well.
	 * @throws CrawljaxException if an exception is thrown.
	 */
	private Document getDomTreeWithFrames_GoldStandards() throws CrawljaxException {

		try {
			Document document = DomUtils.asDocument(browser.getPageSource());
			if(document.getElementsByTagName("title").item(0).getTextContent().equalsIgnoreCase("Meeting Room Booking System")) {
				document = DomUtils.removeHiddenInputs(document);
			}
			if(document.getElementsByTagName("title").item(0).getTextContent().contains("MantisBT")) {
				document = DomUtils.removeHiddenInputs(document);
				document = DomUtils.removeHead(document);
				try {
				document = DomUtils.removeElementsUnderXpath(document, "/html[1]/body[1]/table[1]/tbody[1]/tr[1]");
				}catch(Exception ex) {
					LOGGER.warn("Could not remove time element for Mantisbt");
				}
			}
			
			if(document.getElementsByTagName("title").item(0).getTextContent().contains("retrospect")) {
				document = DomUtils.removeHead(document);
				document = DomUtils.removeComments(document);
			}
			appendFrameContent(document.getDocumentElement(), document, "");
			return document;
		} catch (IOException e) {
			throw new CrawljaxException(e.getMessage(), e);
		}

	}

	private void appendFrameContent(Element orig, Document document, String topFrame) {

		NodeList frameNodes = orig.getElementsByTagName("IFRAME");

		List<Element> nodeList = new ArrayList<>();

		for (int i = 0; i < frameNodes.getLength(); i++) {
			Element frameElement = (Element) frameNodes.item(i);
			nodeList.add(frameElement);
		}

		// Added support for FRAMES
		frameNodes = orig.getElementsByTagName("FRAME");
		for (int i = 0; i < frameNodes.getLength(); i++) {
			Element frameElement = (Element) frameNodes.item(i);
			nodeList.add(frameElement);
		}

		for (int i = 0; i < nodeList.size(); i++) {
			try {
				locateFrameAndGetSource(document, topFrame, nodeList.get(i));
			} catch (UnknownServerException | NoSuchFrameException e) {
				LOGGER.warn("Could not add frame contents for element {}", nodeList.get(i));
				LOGGER.debug("Could not load frame because of {}", e.getMessage(), e);
			}
		}
	}

	private void locateFrameAndGetSource(Document document, String topFrame, Element frameElement)
			throws NoSuchFrameException {
		String frameIdentification = "";

		if (topFrame != null && !topFrame.equals("")) {
			frameIdentification += topFrame + ".";
		}

		String nameId = DomUtils.getFrameIdentification(frameElement);

		if (nameId != null && !ignoreFrameChecker.isFrameIgnored(frameIdentification + nameId)) {
			frameIdentification += nameId;

			String handle = browser.getWindowHandle();

			LOGGER.debug("The current H: " + handle);
			try {
				switchToFrame(frameIdentification);
			} catch (InvalidSelectorException e) {
				LOGGER.info("Invalid frame selector: " + frameIdentification + ", continuing...",
						e);
				browser.switchTo().defaultContent();
				return;
			}

			String toAppend = browser.getPageSource();

			LOGGER.debug("frame dom: " + toAppend);

			browser.switchTo().defaultContent();

			try {
				Element toAppendElement = DomUtils.asDocument(toAppend).getDocumentElement();
				Element importedElement = (Element) document.importNode(toAppendElement, true);
				frameElement.appendChild(importedElement);

				appendFrameContent(importedElement, document, frameIdentification);
			} catch (DOMException | IOException e) {
				LOGGER.info("Got exception while inspecting a frame:" + frameIdentification
						+ " continuing...", e);
			}
		}
	}

	private void switchToFrame(String frameIdentification) throws NoSuchFrameException {
		LOGGER.debug("frame identification: " + frameIdentification);

		if (frameIdentification.contains(".")) {
			String[] frames = frameIdentification.split("\\.");

			for (String frameId : frames) {
				LOGGER.debug("switching to frame: " + frameId);
				browser.switchTo().frame(frameId);
			}

		} else {
			browser.switchTo().frame(frameIdentification);
		}

	}

	/**
	 * @return the dom without the iframe contents.
	 * @see com.crawljax.browser.EmbeddedBrowser#getStrippedDomWithoutIframeContent()
	 */
	@Override
	public String getStrippedDomWithoutIframeContent() {
		try {
			String dom = browser.getPageSource();
			return toUniformDOM(dom);
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return "";
		}
	}

	/**
	 * @param input the input to be filled.
	 * @return FormInput with random value assigned if possible. If no values were set it returns
	 * <code>null</code>
	 */
	@Override
	public FormInput getInputWithRandomValue(FormInput input) {

		WebElement webElement;
		try {
			webElement = browser.findElement(input.getIdentification().getWebDriverBy());
			if (!webElement.isDisplayed()) {
				return null;
			}
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return null;
		}

		Set<InputValue> values = new HashSet<>();
		try {
			setRandomValues(input, webElement, values);
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return null;
		}
		if (values.isEmpty()) {
			return null;
		}
		input.inputValues(values);
		return input;

	}

	private void setRandomValues(FormInput input, WebElement webElement, Set<InputValue> values) {
		switch (input.getType()) {
			case TEXT:
			case INPUT:
			case TEXTAREA:
				values.add(new InputValue(
						new RandomInputValueGenerator()
								.getRandomString(FormHandler.RANDOM_STRING_LENGTH),
						true));
				break;
			case CHECKBOX:
			case RADIO:
				if (!webElement.isSelected()) {
					if (new RandomInputValueGenerator().getCheck()) {
						values.add(new InputValue("1", true));
					} else {
						values.add(new InputValue("0", false));
					}
				}
				else {
					values.add(new InputValue("1", true));
				}
				break;
			case NUMBER:
				LOGGER.info("Adding number to form {}", webElement.getTagName());
				values.add(new InputValue(
						new RandomInputValueGenerator()
								.getRandomNumber(),
						true));
				break;
			case SELECT:
				Select select = new Select(webElement);
				if (!select.getOptions().isEmpty()) {
					WebElement option =
							new RandomInputValueGenerator().getRandomItem(select.getOptions());
					values.add(new InputValue(option.getText(), true));
				}
				break;
			default:
				break;
		}

		/*
		 * if (type.equals(InputType.TEXT) || type.equals(InputType.TEXTAREA)) { values.add(new
		 * InputValue(new RandomInputValueGenerator()
		 * .getRandomString(FormHandler.RANDOM_STRING_LENGTH), true)); } else if
		 * (type.equals("checkbox") || type.equals("radio") && !webElement.isSelected()) { if (new
		 * RandomInputValueGenerator().getCheck()) { values.add(new InputValue("1", true)); } else {
		 * values.add(new InputValue("0", false)); } } else if (type.equals("select")) { Select
		 * select = new Select(webElement); if (!select.getOptions().isEmpty()) { WebElement option
		 * = new RandomInputValueGenerator().getRandomItem(select.getOptions()); values.add(new
		 * InputValue(option.getText(), true)); } }
		 **/
	}

	@Override
	public String getFrameDom(String iframeIdentification) {
		try {

			switchToFrame(iframeIdentification);

			// make a copy of the dom before changing into the top page
			String frameDom = browser.getPageSource();

			browser.switchTo().defaultContent();

			return frameDom;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return "";
		}
	}

	/**
	 * @param identification the identification of the element.
	 * @return true if the element can be found in the DOM tree.
	 */
	@Override
	public boolean elementExists(Identification identification) {
		try {
			WebElement el = browser.findElement(identification.getWebDriverBy());
			// TODO Stefan; I think el will never be null as a
			// NoSuchElementException will be
			// thrown, caught below.
			return el != null;
		} catch (WebDriverException e) {
			throwIfConnectionException(e);
			return false;
		}
	}

	/**
	 * @param identification the identification of the element.
	 * @return the found element.
	 */
	@Override
	public WebElement getWebElement(Identification identification) {
		try {
			return browser.findElement(identification.getWebDriverBy());
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	@Override
	public WebDriver getWebDriver() {
		return browser;
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
	protected ImmutableSortedSet<String> getFilterAttributes() {
		return filterAttributes;
	}

	/**
	 * @return the period to wait after a reload.
	 */
	protected long getCrawlWaitReload() {
		return crawlWaitReload;
	}

	@Override
	public void saveScreenShot(File file) throws CrawljaxException {
		try {
			File tmpFile = takeScreenShotOnBrowser(browser, OutputType.FILE);
			try {
				Files.copy(tmpFile, file);
			} catch (IOException e) {
				throw new CrawljaxException(e);
			}
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
		}
	}

	private <T> T takeScreenShotOnBrowser(WebDriver driver, OutputType<T> outType)
			throws CrawljaxException {
		if (driver instanceof TakesScreenshot) {
			T screenshot = ((TakesScreenshot) driver).getScreenshotAs(outType);
			removeCanvasGeneratedByFirefoxDriverForScreenshots();
			return screenshot;
		} else if (driver instanceof RemoteWebDriver) {
			WebDriver augmentedWebDriver = new Augmenter().augment(driver);
			return takeScreenShotOnBrowser(augmentedWebDriver, outType);
		} else if (driver instanceof WrapsDriver) {
			return takeScreenShotOnBrowser(((WrapsDriver) driver).getWrappedDriver(), outType);
		} else {
			throw new CrawljaxException("Your current WebDriver doesn't support screenshots.");
		}
	}

	@Override
	public BufferedImage getScreenShotAsBufferedImage(int scrollTime) {

		if (this.pixelDensity != -1) {
			// BufferedImage img = Shutterbug.shootPage(getWebDriver(),
			// ScrollStrategy.WHOLE_PAGE_CHROME,true).getImage();
			BufferedImage img = Shutterbug
					.shootPage(getWebDriver(), ScrollStrategy.BOTH_DIRECTIONS, scrollTime, true)
					.getImage();
			BufferedImage resizedImage = new BufferedImage(img.getWidth() / pixelDensity,
					img.getHeight() / pixelDensity, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(img, 0, 0, img.getWidth() / pixelDensity,
					img.getHeight() / pixelDensity,
					Color.WHITE, null);
			g.dispose();
			return resizedImage;
		}

		try {
			final ShootingStrategy pasting =
					new ViewportPastingDecorator(new SimpleShootingStrategy())
							.withScrollTimeout(scrollTime);
			return pasting.getScreenshot(browser);

		} catch (IllegalStateException e) {
			Thread.currentThread().interrupt();
			throw new CrawljaxException(e);
		}
	}

	@Override
	public String getScreenShotAsBase64() throws CrawljaxException {
		try {
			return takeScreenShotOnBrowser(browser, OutputType.BASE64);
		} catch (WebDriverException e) {
			throw wrapWebDriverExceptionIfConnectionException(e);
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
		} catch (CrawljaxException e) {
			LOGGER.error(
					"Removing of Canvas Generated By FirefoxDriver failed,"
							+ " most likely leaving it in the browser",
					e);
		}
	}

	/**
	 * @return the WebDriver used as an EmbeddedBrowser.
	 */
	public WebDriver getBrowser() {
		return browser;
	}

	private boolean exceptionIsConnectionException(WebDriverException exception) {
		return exception != null && exception.getCause() != null
				&& exception.getCause() instanceof IOException;
	}

	private RuntimeException wrapWebDriverExceptionIfConnectionException(
			WebDriverException exception) {
		if (exceptionIsConnectionException(exception)) {
			return new BrowserConnectionException(exception);
		}
		return exception;
	}

	private void throwIfConnectionException(WebDriverException exception) {
		if (exceptionIsConnectionException(exception)) {
			throw wrapWebDriverExceptionIfConnectionException(exception);
		}
	}
	
	private void throwIfNotInteractableException(WebDriverException exception) {
		boolean b = (exception.getCause() instanceof ElementNotInteractableException);
		if(exceptionIsInteractableException(exception)) {
			throw new ElementNotInteractableException("not interactable");
		}
	}

	private boolean exceptionIsInteractableException(WebDriverException exception) {
		return exception != null
				&& exception instanceof ElementNotInteractableException;
	}

	public void setPixelDensity(int pixelDensity) {
		this.pixelDensity = pixelDensity;
	}

}

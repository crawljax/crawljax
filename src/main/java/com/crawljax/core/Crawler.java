package com.crawljax.core;

import java.util.List;

import org.apache.log4j.Logger;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.browserwaiter.WaitConditionChecker;
import com.crawljax.core.plugin.CrawljaxPluginsUtil;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import com.crawljax.forms.FormHandler;
import com.crawljax.forms.FormInput;
import com.crawljax.util.ElementResolver;
import com.crawljax.util.PropertyHelper;

/**
 * Class that performs crawl actions.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class Crawler {

	private static final Logger LOGGER = Logger.getLogger(Crawler.class.getName());

	private final WaitConditionChecker waitConditionChecker;
	private final EmbeddedBrowser browser;

	/**
	 * @param browser
	 *            the current browser
	 * @param waitConditionChecker
	 *            checks the wait conditions
	 */
	public Crawler(EmbeddedBrowser browser, WaitConditionChecker waitConditionChecker) {
		this.waitConditionChecker = waitConditionChecker;
		this.browser = browser;
	}

	/**
	 * Brings the browser to the initial state.
	 * 
	 * @throws CrawljaxException
	 *             an exception.
	 */
	public void goToInitialURL() throws CrawljaxException {
		LOGGER.info("Loading Page " + PropertyHelper.getSiteUrlValue());
		browser.goToUrl(PropertyHelper.getSiteUrlValue());
		waitConditionChecker.wait(browser);
		CrawljaxPluginsUtil.runOnUrlLoadPlugins(browser);
	}

	/**
	 * @param eventable
	 *            the eventable to fire
	 * @return true iff the event is fired
	 */
	public boolean fireEvent(final Eventable eventable) {
		try {
			String xpath = eventable.getIdentification().getValue();
			String eventType = eventable.getEventType();
			ElementResolver er = new ElementResolver(eventable, browser);
			String newXPath = er.resolve();
			if (newXPath != null) {
				if (!xpath.equals(newXPath)) {
					LOGGER.info("XPath changed from " + xpath + " to " + newXPath);
				}
				boolean isFired =
				        browser.fireEvent(new Eventable(new Identification("xpath", newXPath),
				                eventType));
				if (!isFired) {
					return false;
				}
				waitConditionChecker.wait(browser);
				browser.closeOtherWindows();
				return true;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Enters the form data. First, the related input elements (if any) to the eventable are filled
	 * in and then it tries to fill in the remaining input elements
	 * 
	 * @param eventable
	 *            the eventable element.
	 */
	public void handleInputElements(Eventable eventable) {
		List<FormInput> formInputs = eventable.getRelatedFormInputs();
		FormHandler formHandler = new FormHandler(browser);
		for (FormInput formInput : formHandler.getFormInputs()) {
			if (!formInputs.contains(formInput)) {
				formInputs.add(formInput);
			}
		}
		eventable.setRelatedFormInputs(formInputs);
		formHandler.handleFormElements(formInputs);
	}

}

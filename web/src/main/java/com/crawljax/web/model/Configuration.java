package com.crawljax.web.model;

import java.util.Date;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;

public class Configuration {
	private String id;
	private String name;
	private String url;
	private BrowserType browser = BrowserType.firefox;
	private int numBrowsers;
	private boolean bootBrowser = true;
	private int reloadWaitTime = 500;
	private int eventWaitTime = 500;
	private int maxDepth = 2;
	private int maxState;
	private int maxDuration = 60;
	private boolean clickOnce = true;
	private boolean randomFormInput = true;
	private Date lastModified = null;
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the browser
	 */
	public BrowserType getBrowser() {
		return browser;
	}
	/**
	 * @param browser the browser to set
	 */
	public void setBrowser(BrowserType browser) {
		this.browser = browser;
	}
	/**
	 * @return the numBrowsers
	 */
	public int getNumBrowsers() {
		return numBrowsers;
	}
	/**
	 * @param numBrowsers the numBrowsers to set
	 */
	public void setNumBrowsers(int numBrowsers) {
		this.numBrowsers = numBrowsers;
	}
	/**
	 * @return the reloadWaitTime
	 */
	public int getReloadWaitTime() {
		return reloadWaitTime;
	}
	/**
	 * @param reloadWaitTime the reloadWaitTime to set
	 */
	public void setReloadWaitTime(int reloadWaitTime) {
		this.reloadWaitTime = reloadWaitTime;
	}
	/**
	 * @return the eventWaitTime
	 */
	public int getEventWaitTime() {
		return eventWaitTime;
	}
	/**
	 * @param eventWaitTime the eventWaitTime to set
	 */
	public void setEventWaitTime(int eventWaitTime) {
		this.eventWaitTime = eventWaitTime;
	}
	/**
	 * @return the maxDepth
	 */
	public int getMaxDepth() {
		return maxDepth;
	}
	/**
	 * @param maxDepth the maxDepth to set
	 */
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	/**
	 * @return the maxState
	 */
	public int getMaxState() {
		return maxState;
	}
	/**
	 * @param maxState the maxState to set
	 */
	public void setMaxState(int maxState) {
		this.maxState = maxState;
	}
	/**
	 * @return the maxDuration
	 */
	public int getMaxDuration() {
		return maxDuration;
	}
	/**
	 * @param maxDuration the maxDuration to set
	 */
	public void setMaxDuration(int maxDuration) {
		this.maxDuration = maxDuration;
	}
	/**
	 * @return the clickOnce
	 */
	public boolean isClickOnce() {
		return clickOnce;
	}
	/**
	 * @param clickOnce the clickOnce to set
	 */
	public void setClickOnce(boolean clickOnce) {
		this.clickOnce = clickOnce;
	}
	/**
	 * @return the randomFormInput
	 */
	public boolean isRandomFormInput() {
		return randomFormInput;
	}
	/**
	 * @param randomFormInput the randomFormInput to set
	 */
	public void setRandomFormInput(boolean randomFormInput) {
		this.randomFormInput = randomFormInput;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the bootBrowser
	 */
	public boolean isBootBrowser() {
		return bootBrowser;
	}
	/**
	 * @param bootBrowser the bootBrowser to set
	 */
	public void setBootBrowser(boolean bootBrowser) {
		this.bootBrowser = bootBrowser;
	}
	/**
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}
	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	} 
}

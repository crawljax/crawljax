package com.crawljax.web.model;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlRecord {
	private int id;
	private String configurationId;
	private String configurationName;
	private Date createTime;
	private Date startTime;
	private long duration;
	private String outputFolder;
	private CrawlStatusType crawlStatus = CrawlStatusType.idle;
	private ConcurrentHashMap<String, Plugin> plugins = new ConcurrentHashMap<String, Plugin>();

	public enum CrawlStatusType {
		idle, queued, initializing, running, success, failure
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the configurationId
	 */
	public String getConfigurationId() {
		return configurationId;
	}

	/**
	 * @param configurationId
	 *            the configurationId to set
	 */
	public void setConfigurationId(String configurationId) {
		this.configurationId = configurationId;
	}

	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime
	 *            the createTime to set
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * @return the outputFolder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	/**
	 * @param outputFolder
	 *            the outputFolder to set
	 */
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * @return the configurationName
	 */
	public String getConfigurationName() {
		return configurationName;
	}

	/**
	 * @param configurationName
	 *            the configurationName to set
	 */
	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	/**
	 * @return the crawlStatus
	 */
	public CrawlStatusType getCrawlStatus() {
		return crawlStatus;
	}

	/**
	 * @param crawlStatus
	 *            the crawlStatus to set
	 */
	public void setCrawlStatus(CrawlStatusType crawlStatus) {
		this.crawlStatus = crawlStatus;
	}

	public ConcurrentHashMap<String, Plugin> getPlugins() {
		return plugins;
	}

	public void setPlugins(ConcurrentHashMap<String, Plugin> plugins) {
		this.plugins = plugins;
	}

}

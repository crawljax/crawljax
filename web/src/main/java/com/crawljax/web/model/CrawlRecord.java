package com.crawljax.web.model;

import java.util.Date;
import javax.xml.datatype.Duration;

public class CrawlRecord {
	private int id;
	private String configurationId;
	private Date createTime;
	private Date startTime;
	private Duration duration;
	
	public CrawlRecord(int id, String configId)
	{
		this.id = id;
		this.configurationId = configId;
		this.createTime = new Date();
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return the configurationId
	 */
	public String getConfigurationId() {
		return configurationId;
	}
	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the duration
	 */
	public Duration getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(Duration duration) {
		this.duration = duration;
	}
	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}	
}

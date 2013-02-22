package com.crawljax.web.model;

import java.util.ArrayList;
import java.util.List;

public class CrawlRecords {
	private List<CrawlRecord> crawlList;
	private int identity = 0;
	
	public CrawlRecords()
	{
		crawlList = new ArrayList<CrawlRecord>();
	}


	/**
	 * @return the crawlRecords
	 */
	public List<CrawlRecord> getCrawlList() {
		return crawlList;
	}
	
	public List<CrawlRecord> getCrawlListByConfigID(String configId) {
		List<CrawlRecord> configRecordList = new ArrayList<CrawlRecord>();
		for (CrawlRecord r : crawlList) {
			if (r.getConfigurationId().equals(configId)) configRecordList.add(r);
		}
		return configRecordList;
	}
	
	public CrawlRecord findByID(int id) {
		CrawlRecord record = null;
		for (CrawlRecord r : crawlList) {
			if (r.getId() == id) {
				record = r;
				break;
			}
		}
		return record;
	}

	public CrawlRecord add(String configId)
	{
		CrawlRecord r = new CrawlRecord(identity++, configId);
		crawlList.add(0, r);
		return r;
	}
	
}

package com.crawljax.web.fs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.crawljax.web.model.Plugin;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.web.di.CrawljaxWebModule.OutputFolder;
import com.crawljax.web.model.Configuration;
import com.crawljax.web.model.CrawlRecord;
import com.crawljax.web.model.CrawlRecord.CrawlStatusType;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

@Singleton
public class WorkDirManager {

	private static final Logger LOG = LoggerFactory.getLogger(WorkDirManager.class);

	private final File configFolder;
	private final File recordFolder;
	private final ObjectMapper mapper;

	@Inject
	public WorkDirManager(@OutputFolder File outputFolder, ObjectMapper mapper) {
		LOG.debug("Initiating the Workdir manager");
		this.configFolder = new File(outputFolder, "configurations");
		this.recordFolder = new File(outputFolder, "crawl-records");
		this.mapper = mapper;
		if (!this.configFolder.exists())
			this.configFolder.mkdirs();
		if (!this.recordFolder.exists())
			this.recordFolder.mkdirs();
	}

	public Map<String, Configuration> loadConfigurations() {
		Map<String, Configuration> configs = new ConcurrentHashMap<String, Configuration>();
		File[] configFiles = configFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("json");
			}
		});
		if (configFiles != null) {
			for (File f : configFiles) {
				Configuration c = loadConfiguration(f);
				configs.put(c.getId(), c);
			}
		}
		return configs;
	}

	private Configuration loadConfiguration(File configFile) {
		Configuration config = null;
		try {
			config = mapper.readValue(configFile, Configuration.class);
		} catch (IOException e) {
			LOG.error("Could not load config", configFile.getName());
		}
		return config;
	}

	public void saveConfiguration(Configuration config) {
		File configFile = new File(configFolder, config.getId() + ".json");
		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
			}
			mapper.writeValue(configFile, config);
		} catch (IOException e) {
			LOG.error("Could not save config {}", config);
		}
	}

	public void deleteConfiguration(Configuration config) {
		File configFile = new File(configFolder, config.getId() + ".json");
		try {
			configFile.delete();
		} catch (Exception e) {
			LOG.error("Could not delete config {}", config);
		}
	}

	public List<CrawlRecord> loadCrawlRecords() {
		List<CrawlRecord> records = new ArrayList<CrawlRecord>();
		File[] recordFiles = recordFolder.listFiles();
		if (recordFiles != null) {
			for (File f : recordFiles) {
				if (f.isDirectory()) {
					File record = new File(f, "crawl.json");
					if (record.exists()) {
						CrawlRecord c = loadCrawlRecord(record);

						// clean up records that crashed unexpectedly
						if (c.getCrawlStatus() != CrawlStatusType.success
						        && c.getCrawlStatus() != CrawlStatusType.failure)
							c.setCrawlStatus(CrawlStatusType.failure);

						int length = records.size();
						if (length > 0) {
							for (int i = 0; i < length; i++) {
								if (records.get(i).getId() < c.getId()) {
									records.add(i, c);
									break;
								}
							}
						} else
							records.add(c);
					}
				}
			}
		}
		return records;
	}

	private CrawlRecord loadCrawlRecord(File recordFile) {
		CrawlRecord record = null;
		try {
			record = mapper.readValue(recordFile, CrawlRecord.class);
		} catch (IOException e) {
			LOG.error("Could not load record", recordFile.getName());
		}
		return record;
	}

	public void saveCrawlRecord(CrawlRecord record) {
		File recordFile =
		        new File(recordFolder, Integer.toString(record.getId()) + File.separatorChar
		                + "crawl.json");
		try {
			if (!recordFile.exists()) {
				recordFile.getParentFile().mkdirs();
				recordFile.createNewFile();
				record.setOutputFolder(recordFile.getParent());
			}
			mapper.writeValue(recordFile, record);
		} catch (IOException e) {
			LOG.error("Could not save crawl record {}", record);
		}
	}

	public String readLog(int crawlId) {
		File logFile =
		        new File(recordFolder, Integer.toString(crawlId) + File.separatorChar
		                + "crawl.log");
		String content = "";
		try {
			content =
			        "<p>" + StringUtils.join(Files.readLines(logFile, Charsets.UTF_8), "</p><p>")
			                + "</p>";
		} catch (IOException e) {
			LOG.error("Could not read log", logFile.getName());
		}
		return content;
	}
}
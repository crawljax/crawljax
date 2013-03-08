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

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.web.model.Configuration;
import com.crawljax.web.model.CrawlRecord;

@Singleton
public class WorkDirManager {

	private static final Logger LOG = LoggerFactory.getLogger(WorkDirManager.class);

	private final File outputFolder;
	private final ObjectMapper mapper;

	@Inject
	public WorkDirManager(File outputFolder, ObjectMapper mapper) {
		this.outputFolder = outputFolder;
		this.mapper = mapper;
		if (!this.outputFolder.exists())
			this.outputFolder.mkdirs();
	}

	public Map<String, Configuration> loadAll() {
		Map<String, Configuration> configs = new ConcurrentHashMap<String, Configuration>();
		File[] configFiles = outputFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("json");
			}
		});
		if (configFiles != null) {
			for (File f : configFiles) {
				Configuration c = load(f);
				configs.put(c.getId(), c);
			}
		}
		return configs;
	}

	private Configuration load(File configFile) {
		Configuration config = null;
		try {
			config = mapper.readValue(configFile, Configuration.class);
		} catch (IOException e) {
			LOG.error("Could not load config", configFile.getName());
		}
		return config;
	}

	public void save(Configuration config) {
		File configFile = new File(outputFolder, config.getId() + ".json");
		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
			}
			mapper.writeValue(configFile, config);
		} catch (IOException e) {
			LOG.error("Could not save config {}", config);
		}
	}

	public void delete(Configuration config) {
		File configFile = new File(outputFolder, config.getId() + ".json");
		try {
			configFile.delete();
		} catch (Exception e) {
			LOG.error("Could not delete config {}", config);
		}
	}

	public List<CrawlRecord> loadHistory() {
		List<CrawlRecord> records = new ArrayList<CrawlRecord>();
		File[] recordFiles = outputFolder.listFiles();
		if (recordFiles != null) {
			for (File f : recordFiles) {
				if (f.isDirectory()) {
					CrawlRecord c = loadRecord(new File(f, "record.json"));
					records.add(0, c);
				}
			}
		}
		return records;
	}

	private CrawlRecord loadRecord(File recordFile) {
		CrawlRecord record = null;
		try {
			record = mapper.readValue(recordFile, CrawlRecord.class);
		} catch (IOException e) {
			LOG.error("Could not load record", recordFile.getName());
		}
		return record;
	}

	public void saveRecord(CrawlRecord record) {
		File recordFile =
		        new File(outputFolder, Integer.toString(record.getId()) + File.separatorChar
		                + "record.json");
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
}

package com.crawljax.web.fs;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.CrawljaxConfiguration;

@Singleton
public class WorkDirManager {

	private static final Logger LOG = LoggerFactory.getLogger(WorkDirManager.class);

	private final File outputFolder;
	private final ObjectMapper mapper;

	@Inject
	public WorkDirManager(File outputFolder, ObjectMapper mapper) {
		this.outputFolder = outputFolder;
		this.mapper = mapper;
	}

	public void save(CrawljaxConfiguration config) {
		File resultFile = new File(outputFolder, "result.json");
		try {
			mapper.writeValue(resultFile, config);
		} catch (IOException e) {
			LOG.error("Could not save config {}", config);
		}
	}

}

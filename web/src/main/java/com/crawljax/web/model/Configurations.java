package com.crawljax.web.model;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.crawljax.web.fs.WorkDirManager;

@Singleton
public class Configurations {
	private final Map<String, Configuration> configList;
	private final WorkDirManager workDirManager;

	@Inject
	public Configurations(WorkDirManager workDirManager) {
		this.workDirManager = workDirManager;
		configList = workDirManager.loadAll();
	}

	/**
	 * @return the configList
	 */
	public Collection<Configuration> getConfigList() {
		return configList.values();
	}

	public Configuration add(Configuration config) {
		String id = config.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
		if (configList.containsKey(id)) {
			int i = 1;
			while (configList.containsKey(id + Integer.toString(i)))
				i++;
			id += Integer.toString(i);
		}
		config.setId(id);
		configList.put(id, config);
		workDirManager.save(config);

		return config;
	}

	public Configuration update(Configuration config) {
		config.setLastModified(new Date());
		configList.put(config.getId(), config);
		workDirManager.save(config);

		return config;
	}

	public Configuration remove(Configuration config) {
		configList.remove(config.getId());
		workDirManager.delete(config);

		return config;
	}

	public Configuration findByID(final String id) {
		return configList.get(id);
	}
}

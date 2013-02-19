package com.crawljax.web.model;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class Configurations {
	private Map<String, Configuration> configList;
	
	public Configurations()
	{
		configList = new HashMap<String, Configuration>();
	}

	/**
	 * @return the configList
	 */
	public Collection<Configuration> getConfigList() {
		return configList.values();
	}
	
	public Configuration add(Configuration config)
	{
		String id = config.getName().toLowerCase().replaceAll("/[^a-z0-9]+/g", "-");
		config.setId(id);
		configList.put(id, config);
		return config;
	}
	
	public Configuration update(Configuration config)
	{
		config.setLastModified(new Date());
		configList.put(config.getId(), config);	
		return config;
	}
	
	public Configuration findByID(final String id){
		return configList.get(id);
	}	
}

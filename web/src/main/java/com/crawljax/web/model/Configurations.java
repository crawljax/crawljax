package com.crawljax.web.model;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Configurations {
	private Map<String, Configuration> configList;
	
	public Configurations()
	{
		configList = new ConcurrentHashMap<String, Configuration>();
	}

	/**
	 * @return the configList
	 */
	public Collection<Configuration> getConfigList() {
		return configList.values();
	}
	
	public Configuration add(Configuration config)
	{
		String id = config.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
		if (configList.containsKey(id))
		{
			int i = 1;
			while (configList.containsKey(id + Integer.toString(i))) i++;
			id += Integer.toString(i);
		}
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
	
	public Configuration remove(Configuration config)
	{
		configList.remove(config.getId());
		return config;
	}
	
	public Configuration findByID(final String id){
		return configList.get(id);
	}	
}

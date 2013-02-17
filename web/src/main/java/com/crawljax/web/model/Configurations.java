package com.crawljax.web.model;

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
	public Map<String, Configuration> getConfigList() {
		return configList;
	}
	
	public Configuration findByID(final String id){
		return configList.get(id);
	}	
}

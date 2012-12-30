package com.crawljax.plugins.crawloverview;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

class CachedResources {

	private final String indexTemplate;
	private final String stateTemplate;

	public CachedResources() {
		indexTemplate = read("index.vm");
		stateTemplate = read("state.vm");
	}

	private String read(String resource) {
		try {
			return Resources.toString(Resources.getResource(resource), Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not read required resource " + resource, e);
		}
	}

	public String getIndexTemplate() {
		return indexTemplate;
	}

	public String getStateTemplate() {
		return stateTemplate;
	}

}

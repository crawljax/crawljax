package com.crawljax.web.model;

import com.crawljax.core.plugin.descriptor.Parameter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Plugin {

	private String id;
	private String name;
	private String description;
	private File jarFile;
	private List<String> crawljaxVersions = new ArrayList<>();
	private List<Parameter> parameters = new ArrayList<>();

	public Plugin() {} //Default constructor

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public File getJarFile() {
		return jarFile;
	}

	public void setJarFile(File jarFile) {
		this.jarFile = jarFile;
	}

	public List<String> getCrawljaxVersions() {
		return crawljaxVersions;
	}

	public void setCrawljaxVersions(List<String> crawljaxVersions) {
		this.crawljaxVersions = crawljaxVersions;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
}

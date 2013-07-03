package com.crawljax.web.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Plugin {

	private String id;
	private String name;
	private String description;
	private URL url;
	private String implementation;
	private String crawljaxVersion;
	private List<Parameter> parameters = new ArrayList<>();

	public Plugin() {} //Default constructor

	public Plugin(Plugin plugin) { //Copy constructor
		this.id = plugin.getId();
		this.name = plugin.getName();
		this.description = plugin.getDescription();
		try {
			this.url = new URL(plugin.getUrl().toString());
		} catch (MalformedURLException e){};
		this.implementation = plugin.getImplementation();
		this.crawljaxVersion = plugin.getCrawljaxVersion();
		this.parameters = new ArrayList<>();
		for(int i = 0; i < plugin.getParameters().size(); i++) {
			Parameter param = new Parameter();
			param.setId(plugin.getParameters().get(i).getId());
			param.setDisplayName(plugin.getParameters().get(i).getDisplayName());
			param.setValue(plugin.getParameters().get(i).getValue());
			this.parameters.add(param);
		}
	}

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

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getImplementation() {
		return implementation;
	}

	public void setImplementation(String implementation) {
		this.implementation = implementation;
	}

	public String getCrawljaxVersion() {
		return crawljaxVersion;
	}

	public void setCrawljaxVersion(String crawljaxVersion) {
		this.crawljaxVersion = crawljaxVersion;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
}

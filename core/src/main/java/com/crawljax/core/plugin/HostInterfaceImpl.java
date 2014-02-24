package com.crawljax.core.plugin;

import java.io.File;
import java.util.Map;

public class HostInterfaceImpl implements HostInterface {

	private File outputDirectory;
	private Map<String, String> parameters;

	public HostInterfaceImpl(File outputDirectory, Map<String, String> parameters) {
		this.outputDirectory = outputDirectory;
		this.parameters = parameters;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}

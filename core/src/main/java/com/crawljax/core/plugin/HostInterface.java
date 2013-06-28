package com.crawljax.core.plugin;

import java.io.File;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 2013/05/31
 * Time: 12:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class HostInterface implements IHostInterface {

	private File outputDirectory;
	private Map<String, String> parameters;

	public HostInterface() {

	}

	public HostInterface(File outputDirectory, Map<String, String> parameters) {
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

package com.crawljax.web;

import java.io.File;

public class CrawljaxServerConfigurationBuilder {

	private int port;
	private File outputDir;
	private File pluginDir;

	public CrawljaxServerConfigurationBuilder() {
		port = 0;
		outputDir = new File("out");
		pluginDir = new File("plugins");
	}

	public int getPort() {
		return port;
	}

	public CrawljaxServerConfigurationBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public CrawljaxServerConfigurationBuilder setOutputDir(File outputDir) {
		this.outputDir = outputDir;
		return this;
	}

	public File getPluginDir() {
		return pluginDir;
	}

	public CrawljaxServerConfigurationBuilder setPluginDir(File pluginDir) {
		this.pluginDir = pluginDir;
		return this;
	}
}

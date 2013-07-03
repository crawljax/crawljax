package com.crawljax.core.plugin;

import java.io.File;
import java.util.Map;

public interface HostInterface {

	public File getOutputDirectory();
	public Map<String, String> getParameters();
}

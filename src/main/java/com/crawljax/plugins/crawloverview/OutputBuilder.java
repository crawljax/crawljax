package com.crawljax.plugins.crawloverview;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.io.Resources.copy;
import static com.google.common.io.Resources.getResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class OutputBuilder {

	private final static String SCREENSHOT_FOLDER_NAME = "screenshots";
	private final static String STATES_FOLDER_NAME = "states";

	private final File outputDir;
	private final File states;
	private final File screenshots;
	private final File indexFile;

	/**
	 * @param outputDir
	 *            target for the output directory. Folder must not exist or be empty.
	 */
	public OutputBuilder(File outputDir, CachedResources resources) {
		this.outputDir = outputDir;
		checkPermissions();
		states = new File(outputDir, STATES_FOLDER_NAME);
		states.mkdir();
		screenshots = new File(outputDir, SCREENSHOT_FOLDER_NAME);
		screenshots.mkdir();
		copyBasicFiles(outputDir, resources);
		indexFile = new File(outputDir, "index.html");
	}

	private void checkPermissions() {
		if (outputDir.exists()) {
			checkArgument(outputDir.isDirectory(), outputDir + " is not a directory");
			checkArgument(outputDir.list().length == 0, "Directory must be empty");
			checkArgument(outputDir.canWrite(), "Output dir not writable");
		} else {
			outputDir.mkdir();
		}
	}

	private void copyBasicFiles(File outputDir2, CachedResources resources) {
		File javascriptFolder = new File(outputDir2, "js");
		javascriptFolder.mkdir();
		File graphFile = new File(javascriptFolder, "graph.js");
		File protoTypeFile = new File(javascriptFolder, "prototype-1.4.0.js");
		try {
			OutputStream out = new FileOutputStream(graphFile);
			copy(getResource("graph.js"), out);
			out.close();
			out = new FileOutputStream(protoTypeFile);
			copy(getResource("prototype-1.4.0.js"), out);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("Could not setup output skeleton", e);
		}
	}

	public File newScreenShotFile(String name) {
		return new File(screenshots, name + ".png");
	}

	public File newStateFile(String name) {
		return new File(states, name + ".html");
	}

	public File getIndexFile() {
		return indexFile;
	}
}

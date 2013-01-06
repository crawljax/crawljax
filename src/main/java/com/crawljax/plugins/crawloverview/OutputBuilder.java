package com.crawljax.plugins.crawloverview;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

class OutputBuilder {

	private final static String SCREENSHOT_FOLDER_NAME = "screenshots";
	private final static String STATES_FOLDER_NAME = "states";

	private final File outputDir;
	private final File states;
	private final File screenshots;
	private final File indexFile;
	private final VelocityEngine ve;

	/**
	 * @param outputDir
	 *            target for the output directory. Folder must not exist or be empty.
	 */
	public OutputBuilder(File outputDir, CachedResources resources) {
		this.outputDir = outputDir;
		checkPermissions();
		copySkeleton();
		states = new File(outputDir, STATES_FOLDER_NAME);
		states.mkdir();
		screenshots = new File(outputDir, SCREENSHOT_FOLDER_NAME);
		screenshots.mkdir();
		indexFile = new File(outputDir, "index.html");
		ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
		        "org.apache.velocity.runtime.log.NullLogChute");

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

	private void copySkeleton() {
		try {
			File srcDir = new File(OutputBuilder.class.getResource("/skeleton").toURI());
			FileUtils.copyDirectory(srcDir, outputDir);
		} catch (IOException e) {
			throw new RuntimeException("Could not copy required resources: " + e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not find skeleton resource: " + e.getMessage(), e);
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

	void writeToFile(String template, VelocityContext context, File fileHTML, String name) {
		try {
			FileWriter writer = new FileWriter(fileHTML);
			ve.evaluate(context, writer, name, template);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new CrawlOverviewException("Could not write output state");
		}
	}

}

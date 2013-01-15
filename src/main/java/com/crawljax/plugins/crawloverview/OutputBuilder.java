package com.crawljax.plugins.crawloverview;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.Statistics;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;

class OutputBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(OutputBuilder.class);

	static final String SCREENSHOT_FOLDER_NAME = "screenshots";
	static final String STATES_FOLDER_NAME = "states";
	static final String JSON_OUTPUT_NAME = "result.json";
	static final String DOMS_OUTPUT_NAME = "doms";

	private final File outputDir;
	private final File states;
	private final File screenshots;
	private final File indexFile;
	private final File doms;
	private final VelocityEngine ve;

	/**
	 * @param outputDir
	 *            target for the output directory. Folder must not exist or be empty.
	 */
	public OutputBuilder(File outputDir) {
		this.outputDir = outputDir;
		checkPermissions();
		copySkeleton();

		states = new File(outputDir, STATES_FOLDER_NAME);
		states.mkdir();
		screenshots = new File(outputDir, SCREENSHOT_FOLDER_NAME);
		screenshots.mkdir();
		doms = new File(outputDir, DOMS_OUTPUT_NAME);
		doms.mkdir();

		indexFile = new File(outputDir, "index.html");
		ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
		        "org.apache.velocity.runtime.log.NullLogChute");
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

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

	public void makeThumbNail(File screenShot, String name) {
		try {
			// scale image on disk
			BufferedImage originalImage = ImageIO.read(screenShot);
			BufferedImage resizedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(originalImage, 0, 0, 200, 200, Color.WHITE, null);
			g.dispose();
			ImageIO.write(resizedImage, "jpg", new File(screenshots, name + "_small.jpg"));
		} catch (IOException e) {
			throw new CrawlOverviewException("Could not create thumbnail");
		}
	}

	public void write(OutPutModel outModel) {
		try {
			writeIndexFile(outModel);
			writeStatistics(outModel.getStatistics());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		LOG.info("Overview report generated");
	}

	private void writeIndexFile(OutPutModel model) {
		LOG.debug("Writing index file");
		VelocityContext context = new VelocityContext();
		String outModelJson = model.toJson();
		writeJsonToOutDir(outModelJson);
		context.put("outputModel", outModelJson);
		writeFile(context, indexFile, "index.html");
	}

	private void writeJsonToOutDir(String outModelJson) {
		try {
			Files.write(outModelJson, new File(this.outputDir, JSON_OUTPUT_NAME), Charsets.UTF_8);
		} catch (IOException e) {
			LOG.warn("Could not write JSON model to output dir. " + e.getMessage());
		}
	}

	private void writeStatistics(Statistics stats) {
		File file = new File(outputDir, "statistics.html");
		VelocityContext context = new VelocityContext();
		context.put("stats", stats);
		writeFile(context, file, "statistics.html");

		file = new File(outputDir, "urls.html");
		context = new VelocityContext();
		context.put("urls", stats.getStateStats().getUrls());
		writeFile(context, file, "urls.html");
	}

	private void writeFile(VelocityContext context, File outFile, String template) {
		try {
			Template templatee = ve.getTemplate(template);
			FileWriter writer = new FileWriter(outFile);
			templatee.merge(context, writer);
			// ve.evaluate(context, writer, name, template);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new CrawlOverviewException("Could not write output state");
		}
	}

	void writeState(VelocityContext context, String stateName) {
		File file = new File(states, stateName + ".html");
		writeFile(context, file, "state.html");
	}

	/**
	 * Save the dom to disk.
	 * 
	 * @param name
	 *            statename
	 * @param dom
	 *            the DOM as string
	 */
	public void persistDom(String name, @Nullable String dom) {
		try {
			Files.write(Strings.nullToEmpty(dom), new File(doms, name + ".html"), Charsets.UTF_8);
		} catch (IOException e) {
			LOG.warn("Could not save dom state for {}", name);
			LOG.debug("Could not save dom state", e);
		}
	}

	public String getDom(String name) {
		try {
			return Files.toString(new File(doms, name + ".html"), Charsets.UTF_8);
		} catch (IOException e) {
			return "Could not load DOM: " + e.getLocalizedMessage();
		}
	}

}

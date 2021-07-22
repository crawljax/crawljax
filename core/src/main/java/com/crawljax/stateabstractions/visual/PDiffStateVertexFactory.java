package com.crawljax.stateabstractions.visual;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.Crawler;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.util.FSUtils;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The default factory that creates State vertexes with a {@link Object#hashCode()} and
 * {@link Object#equals(Object)} function based on the Stripped dom.
 */
public class PDiffStateVertexFactory extends StateVertexFactory {
	static {
		OpenCVLoad.load();
	}

	private static final Logger LOG =
			LoggerFactory.getLogger(PDiffStateVertexFactory.class.getName());

	private static final int THUMBNAIL_WIDTH = 200;
	private static final int THUMBNAIL_HEIGHT = 200;

	private static double threshold = 0.0;
	
	public PDiffStateVertexFactory(double treshold) {
		threshold = treshold;
	}

	@Override
	public StateVertex newStateVertex(int id, String url, String name, String dom,
			String strippedDom,
			EmbeddedBrowser browser) {

		BufferedImage image = browser.getScreenShotAsBufferedImage(1000);
		saveImage(image, name);

		String page = Crawler.outputDir + "/screenshots/" + name + ".png";

		return new PDiffStateVertexImpl(id, url, name, dom, strippedDom, page, threshold);
	}

	private static void saveImage(BufferedImage image, String name) {
		LOG.debug("Saving screenshot for state {}", name);
		try {
			String folderName = Crawler.outputDir + "/screenshots/";
			FSUtils.directoryCheck(folderName);
			ImageIO.write(image, "PNG", new File(folderName + name + ".png"));
			writeThumbNail(new File(folderName + name + "_small.jpg"), image);

		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
	}

	private static void writeThumbNail(File target, BufferedImage screenshot) throws IOException {
		BufferedImage resizedImage =
				new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(screenshot, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Color.WHITE, null);
		g.dispose();
		ImageIO.write(resizedImage, "JPEG", target);
	}

	@Override
	public String toString() {
		return "VISUAL_PDiff_" + threshold;
	}
}

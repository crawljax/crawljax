package com.crawljax.plugins.crawloverview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.crawljax.core.CrawljaxException;

public class ImageWriter {

	private static final int THUMBNAIL_WIDTH = 200;
	private static final int THUMBNAIL_HEIGHT = 200;

	static void writeScreenShotAndThumbnail(byte[] png, File fullFile, File thumnail) {
		try {
			Image image = ImageIO.read(new ByteArrayInputStream(png));
			writeFullSizeJpeg(fullFile, image);
			writeThumbNail(thumnail, image);
		} catch (IOException e) {
			throw new CrawljaxException("Could not write screenshots to disk", e);
		}

	}

	private static void writeFullSizeJpeg(File target, Image image) throws IOException {
		int height = image.getHeight(null);
		int width = image.getWidth(null);
		BufferedImage bufImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = bufImg.createGraphics();
		graphics.drawImage(image, 0, 0, Color.WHITE, null);
		graphics.dispose();
		ImageIO.write(bufImg, "jpg", target);
	}

	private static void writeThumbNail(File target, Image screenshot) throws IOException {
		BufferedImage resizedImage =
		        new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(screenshot, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Color.WHITE, null);
		g.dispose();
		ImageIO.write(resizedImage, "jpg", target);
	}

	private ImageWriter() {
	}
}
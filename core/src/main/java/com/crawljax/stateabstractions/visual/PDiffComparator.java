package com.crawljax.stateabstractions.visual;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDiffComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDiffComparator.class);

    private static final ForkJoinPool pool = new ForkJoinPool();
    /* getting correct perceptible differences only. */
    private static final double COLOR_FACTOR = 0.0;
    private static final double FOV = 27;
    private static final double GAMMA = 4.2;
    private static final double LUMINANCE = 20.0;
    private static final boolean LUMINANCE_ONLY = false;
    private static int THRESHOLD_PIXELS = 100;
    private static final int PERCENTAGE_OF_TOTAL_IMAGE_SIZE = 0;
    private static final String differenceColor = getHexFromDecimal(PerceptualImageDifferencing.COLOR_FAIL);

    public static String getHexFromDecimal(int dec) {
        // return "#" + Integer.toHexString(dec);
        return String.format("#%06X", (0xFFFFFF & dec));
    }

    public static String getHexFromRGB(int red, int green, int blue) {
        return String.format("#%02x%02x%02x", red, green, blue);
    }

    public static double computeDistance(String page1, String page2) {

        BufferedImage imgA = null, imgB = null;
        try {
            imgA = ImageIO.read(new File(page1));
            imgB = ImageIO.read(new File(page2));
        } catch (IOException e) {
            LOGGER.debug(e.getMessage());
            LOGGER.error("Error computing distance between {} and {}", page1, page2);
        }

        double differentPixels = computeDistance(imgA, imgB);
        return differentPixels;
    }

    public static double computeDistance(BufferedImage imgA, BufferedImage imgB) {

        List<Point> differencePixels = new ArrayList<>();

        int width = Math.max(imgA.getWidth(), imgB.getWidth());
        int height = Math.max(imgA.getHeight(), imgB.getHeight());

        THRESHOLD_PIXELS = (int) (width * height * ((double) PERCENTAGE_OF_TOTAL_IMAGE_SIZE / 100));

        BufferedImage imgDiff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        PerceptualImageDifferencing.Builder builder = new PerceptualImageDifferencing.Builder();
        builder.setColorFactor(COLOR_FACTOR);
        builder.setFieldOfView(FOV);
        builder.setGamma(GAMMA);
        builder.setLuminance(LUMINANCE);
        builder.setLuminanceOnly(LUMINANCE_ONLY);
        builder.setThresholdPixels(THRESHOLD_PIXELS);

        PerceptualImageDifferencing pd = builder.build();
        pd.compare(pool, imgA, imgB, imgDiff);
        // pd.dump();

        for (int r = 0; r < imgDiff.getWidth(); r++) {
            for (int c = 0; c < imgDiff.getHeight(); c++) {
                if (getHexFromDecimal(imgDiff.getRGB(r, c)).equalsIgnoreCase(differenceColor)) {
                    differencePixels.add(new Point(r, c));
                }
            }
        }

        // System.out.println("Difference pixels = " + differencePixels.size());
        double differentPixels = (double) differencePixels.size();
        differentPixels /= (width * height);
        return differentPixels;
    }
}

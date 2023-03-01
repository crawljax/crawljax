package com.crawljax.stateabstractions.visual;

import com.crawljax.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorHistogram {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColorHistogram.class);

    private static final String NAME = "VISUAL-ColorHistogram";
    public final double thresholdCoefficient = 0.0;

    public ColorHistogram() {}

    public static double compare(Mat hist1, Mat hist2) {
        return Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
    }

    public Mat getHistogram(String img) {

        Mat mat1 = Imgcodecs.imread(img);

        return getHistogram(mat1);
    }

    private Mat getHistogram(Mat mat1) {
        Mat hist1 = new Mat();
        List<Mat> images = new ArrayList<>();
        images.add(mat1);

        Imgproc.calcHist(
                images,
                new MatOfInt(0, 1),
                new Mat(),
                hist1,
                new MatOfInt(256, 256),
                new MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f));
        return hist1;
    }

    public String getName() {
        return NAME + "_" + this.thresholdCoefficient;
    }

    public Mat getHistogram(BufferedImage thisImage) {
        try {
            Mat img = ImageUtils.BufferedImage2Mat(thisImage);
            return getHistogram(img);
        } catch (IOException e) {
            LOGGER.debug(e.getMessage());
            LOGGER.error("Error computing Histogram. Returning null.. ");
        }
        return null;
    }
}

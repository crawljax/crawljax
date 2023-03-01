package com.crawljax.stateabstractions.visual.imagehashes;

import com.crawljax.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.apache.commons.text.similarity.HammingDistance;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * DHash-like image hash. Author: Andrea Stocco (astocco@ece.ubc.ca) Based On:
 * https://www.pyimagesearch.com/2017/11/27/image-hashing-opencv-python/
 */
public class DHash {

    private static final Logger LOG = LoggerFactory.getLogger(DHash.class);

    /**
     * Calculate the Hamming distance between two hashes
     *
     * @param h1
     * @param h2
     * @return
     */
    public static Integer distance(String h1, String h2) {
        HammingDistance distance = new HammingDistance();
        return distance.apply(h1, h2);
    }

    public String getDHash(Mat objectImage) {
        Mat resized = new Mat();
        int size = 8;
        Imgproc.resize(objectImage, resized, new Size(size + 1, size));

        /*
         * 3. Compute the difference image. The difference hash algorithm works by computing the
         * difference (i.e., relative gradients) between adjacent pixels. In practice we don't
         * actually have to compute the difference — we can apply a “greater than” test (or “less
         * than”, it doesn't really matter as long as the same operation is consistently used).
         */
        String hash = "";

        for (int i = 0; i < resized.rows(); i++) {

            for (int j = 0; j < resized.cols() - 1; j++) {

                double[] pixel_left = resized.get(i, j);
                double[] pixel_right = resized.get(i, j + 1);

                hash += (pixel_left[0] > pixel_right[0] ? "1" : "0");
            }
        }

        LOG.info("DHash: " + hash);
        return hash;
    }

    public String getDHash(BufferedImage image) throws IOException {

        /*
         * 1. Convert to grayscale. The first step is to convert the input image to grayscale and
         * discard any color information. Discarding color enables us to: (1) Hash the image faster
         * since we only have to examine one channel (2) Match images that are identical but have
         * slightly altered color spaces (since color information has been removed). If, for
         * whatever reason, one is interested in keeping the color information, he can run the
         * hashing algorithm on each channel independently and then combine at the end (although
         * this will result in a 3x larger hash).
         */
        Mat objectImage = ImageUtils.BufferedImage2MatGS(image);

        /*
         * 2. Resize image. We squash the image down to 9×8 and ignore aspect ratio to ensure that
         * the resulting image hash will match similar photos regardless of their initial spatial
         * dimensions. Why 9×8? We are implementing difference hash. The difference hash algorithm
         * works by computing the difference (i.e., relative gradients) between adjacent pixels. If
         * we take an input image with 9 pixels per row and compute the difference between adjacent
         * column pixels, we end up with 8 differences. Eight rows of eight differences (i.e., 8×8)
         * is 64 which will become our 64-bit hash.
         */

        return getDHash(objectImage);
    }

    public String getDHash(String imgPath) {
        Mat objectImage = Imgcodecs.imread(imgPath, Imgcodecs.IMREAD_GRAYSCALE);
        return getDHash(objectImage);
    }

    /**
     * compares the DHash of two images and return whether they are perceptually similar (max 10
     * different pixels allowed)
     *
     * @param img1
     * @param img2
     * @return true/false
     */
    public boolean imagesPerceptuallySimilar(String img1, String img2) {
        return distance(getDHash(img1), getDHash(img2)) <= 10;
    }

    public boolean imagesPerceptuallySimilar(BufferedImage img1, BufferedImage img2) throws IOException {
        return distance(getDHash(img1), getDHash(img2)) <= 10;
    }

    /**
     * compares the DHash of two images and return whether they are perceptually similar (max
     *
     * @param img1
     * @param img2
     * @param threshold
     * @return true/false
     * for threshold different pixels allowed)
     */
    public boolean imagesPerceptuallySimilar(String img1, String img2, int threshold) {
        return distance(getDHash(img1), getDHash(img2)) <= threshold;
    }
}

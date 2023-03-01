package com.crawljax.stateabstractions.visual;

import com.crawljax.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class SSIMComparator {

    public static double computeDistance(Mat page1, Mat page2) {
        Scalar mssim = SSIM.getMSSIM(page1, page2);
        return mssim.val[0];
    }

    public static double computeDistance(BufferedImage image, BufferedImage image2) throws IOException {
        Mat img1 = ImageUtils.BufferedImage2Mat(image);
        Mat img2 = ImageUtils.BufferedImage2Mat(image2);
        return computeDistance(img1, img2);
    }
}

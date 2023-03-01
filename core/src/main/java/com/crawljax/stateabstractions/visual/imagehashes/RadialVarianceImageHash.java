package com.crawljax.stateabstractions.visual.imagehashes;

import com.crawljax.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.opencv.core.Mat;
import org.opencv.img_hash.RadialVarianceHash;

public class RadialVarianceImageHash extends VisHash {

    public RadialVarianceImageHash() {
        thresholdCoefficient = 0.0;
        maxRaw = 0.67;
        minThreshold = 0.0;
        maxThreshold = thresholdCoefficient * maxRaw;
    }

    public RadialVarianceImageHash(double thresholdCoefficient) {
        this.thresholdCoefficient = thresholdCoefficient;
        maxRaw = 0.67;
        minThreshold = 0.0;
        maxThreshold = this.thresholdCoefficient * maxRaw;
    }

    @Override
    public Mat getHash(BufferedImage img) {
        //    Mat mat = Imgcodecs.imread(img);
        Mat mat = null;
        try {
            mat = ImageUtils.BufferedImage2Mat(img);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Mat hash = new Mat();
        RadialVarianceHash.create().compute(mat, hash);
        return hash;
    }

    @Override
    public double compare(Mat hashMat, Mat hashMat2) {
        double dist = RadialVarianceHash.create().compare(hashMat, hashMat2);
        double absDist = Math.abs(dist - 1.0);
        if (Math.floor(absDist * 100) == 0.0) {
            return 0.00;
        } else {
            return absDist;
        }
    }

    @Override
    public String getHashName() {
        return "RadialVarianceHash" + "_" + this.thresholdCoefficient;
    }
}

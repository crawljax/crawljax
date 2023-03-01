package com.crawljax.stateabstractions.visual.imagehashes;

import com.crawljax.util.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.opencv.core.Mat;
import org.opencv.img_hash.AverageHash;

public class AverageImageHash extends VisHash {

    AverageImageHash() {
        thresholdCoefficient = 0.0;
        maxRaw = 9;
        minThreshold = 0.0;
        maxThreshold = thresholdCoefficient * maxRaw;
    }

    public AverageImageHash(double thresholdCoefficient) {
        this.thresholdCoefficient = thresholdCoefficient;
        maxRaw = 9;
        minThreshold = 0.0;
        maxThreshold = this.thresholdCoefficient * maxRaw;
    }

    @Override
    public Mat getHash(BufferedImage img) {
        Mat mat = null;
        try {
            mat = ImageUtils.BufferedImage2Mat(img);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Mat hash = new Mat();
        AverageHash.create().compute(mat, hash);
        return hash;
    }

    @Override
    public double compare(Mat hashMat, Mat hashMat2) {
        return AverageHash.create().compare(hashMat, hashMat2);
    }

    @Override
    public String getHashName() {
        String hashName = "AverageHash";
        return hashName + "_" + this.thresholdCoefficient;
    }
}

package com.crawljax.stateabstractions.visual.imagehashes;

import com.crawljax.stateabstractions.visual.OpenCVLoad;
import java.awt.image.BufferedImage;
import org.opencv.core.Mat;

public abstract class VisHash {

    static {
        OpenCVLoad.load();
    }

    /* threshold values range between [0..1]. */
    public double thresholdCoefficient;
    public double minThreshold;
    public double maxThreshold;
    public double maxRaw;

    public abstract Mat getHash(BufferedImage img);

    public abstract double compare(Mat hashMat1, Mat hashMat2);

    public abstract String getHashName();

    @Override
    public String toString() {
        return getHashName();
    }
}

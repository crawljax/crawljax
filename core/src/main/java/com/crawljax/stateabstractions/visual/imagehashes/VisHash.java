package com.crawljax.stateabstractions.visual.imagehashes;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_java;
import org.opencv.core.Mat;

public abstract class VisHash {

	static {
		Loader.load(opencv_java.class);
	}

	/* threshold values range between [0..1]. */
	public double thresholdCoefficient;
	public double minThreshold;
	public double maxThreshold;
	public double maxRaw;

	public abstract Mat getHash(String img);

	public abstract double compare(Mat hashMat1, Mat hashMat2);

	public abstract String getHashName();
	
	@Override
	public String toString() {
		return getHashName();
	}

}
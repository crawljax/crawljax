package com.crawljax.stateabstractions.visual.imagehashes;

import org.opencv.core.Mat;
import org.opencv.img_hash.ColorMomentHash;
import org.opencv.imgcodecs.Imgcodecs;

public class ColorMomentImageHash extends VisHash {

	public ColorMomentImageHash() {
		thresholdCoefficient = 0.0;
		maxRaw = 209.54;
		minThreshold = 0.0;
		maxThreshold = thresholdCoefficient * maxRaw;
	}

	public ColorMomentImageHash(double thresholdCoefficient) {
		this.thresholdCoefficient = thresholdCoefficient;
		maxRaw = 209.54;
		minThreshold = 0.0;
		maxThreshold = this.thresholdCoefficient * maxRaw;
	}

	@Override
	public Mat getHash(String img) {
		Mat mat = Imgcodecs.imread(img);
		Mat hash = new Mat();
		ColorMomentHash.create().compute(mat, hash);
		return hash;
	}

	@Override
	public double compare(Mat hashMat, Mat hashMat2) {
		return ColorMomentHash.create().compare(hashMat, hashMat2);
	}

	@Override
	public String getHashName() {
		String hashName = "ColorMomentHash";
		return hashName + "_" + this.thresholdCoefficient;
	}

}
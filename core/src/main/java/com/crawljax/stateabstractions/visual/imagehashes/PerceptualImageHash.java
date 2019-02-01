package com.crawljax.stateabstractions.visual.imagehashes;

import org.opencv.core.Mat;
import org.opencv.img_hash.PHash;
import org.opencv.imgcodecs.Imgcodecs;

public class PerceptualImageHash extends VisHash {

	public PerceptualImageHash() {
		thresholdCoefficient = 0.0;
		maxRaw = 32;
		minThreshold = 0.0;
		maxThreshold = thresholdCoefficient * maxRaw;
	}

	public PerceptualImageHash(double thresholdCoefficient) {
		this.thresholdCoefficient = thresholdCoefficient;
		maxRaw = 32;
		minThreshold = 0.0;
		maxThreshold = this.thresholdCoefficient * maxRaw;
	}

	@Override
	public Mat getHash(String img) {
		Mat mat = Imgcodecs.imread(img);
		Mat hash = new Mat();
		PHash.create().compute(mat, hash);
		return hash;
	}

	@Override
	public double compare(Mat hashMat, Mat hashMat2) {
		return PHash.create().compare(hashMat, hashMat2);
	}

	@Override
	public String getHashName() {
		return "PHash" + "_" + this.thresholdCoefficient;
	}

}
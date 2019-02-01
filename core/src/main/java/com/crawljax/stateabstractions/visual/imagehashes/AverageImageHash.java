package com.crawljax.stateabstractions.visual.imagehashes;

import org.opencv.core.Mat;
import org.opencv.img_hash.AverageHash;
import org.opencv.imgcodecs.Imgcodecs;

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
	public Mat getHash(String img) {
		Mat mat = Imgcodecs.imread(img);
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
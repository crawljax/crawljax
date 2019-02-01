package com.crawljax.stateabstractions.visual.imagehashes;

import org.opencv.core.Mat;
import org.opencv.img_hash.MarrHildrethHash;
import org.opencv.imgcodecs.Imgcodecs;

public class MarrHildrethImageHash extends VisHash {

	public MarrHildrethImageHash() {
		thresholdCoefficient = 0.0;
		maxRaw = 189;
		minThreshold = 0.0;
		maxThreshold = thresholdCoefficient * maxRaw;
	}

	public MarrHildrethImageHash(double thresholdCoefficient) {
		this.thresholdCoefficient = thresholdCoefficient;
		maxRaw = 189;
		minThreshold = 0.0;
		maxThreshold = this.thresholdCoefficient * maxRaw;
	}

	@Override
	public Mat getHash(String img) {
		Mat mat = Imgcodecs.imread(img);
		Mat hash = new Mat();
		MarrHildrethHash.create().compute(mat, hash);
		return hash;
	}

	@Override
	public double compare(Mat hashMat, Mat hashMat2) {
		return MarrHildrethHash.create().compare(hashMat, hashMat2);
	}

	@Override
	public String getHashName() {
		return "MarrHildrethHash" + "_" + this.thresholdCoefficient;
	}
}
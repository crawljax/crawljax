package com.crawljax.stateabstractions.visual.imagehashes;

import org.opencv.core.Mat;
import org.opencv.img_hash.BlockMeanHash;
import org.opencv.imgcodecs.Imgcodecs;

public class BlockMeanImageHash extends VisHash {

	public BlockMeanImageHash() {
		thresholdCoefficient = 0.0;
		maxRaw = 52;
		minThreshold = 0.0;
		maxThreshold = thresholdCoefficient * maxRaw;
	}

	public BlockMeanImageHash(double thresholdCoefficient) {
		this.thresholdCoefficient = thresholdCoefficient;
		maxRaw = 52;
		minThreshold = 0.0;
		maxThreshold = this.thresholdCoefficient * maxRaw;
	}

	@Override
	public Mat getHash(String img) {
		Mat mat = Imgcodecs.imread(img);
		Mat hash = new Mat();
		BlockMeanHash.create().compute(mat, hash);
		return hash;
	}

	@Override
	public double compare(Mat hashMat, Mat hashMat2) {
		return BlockMeanHash.create().compare(hashMat, hashMat2);
	}

	@Override
	public String getHashName() {
		String hashName = "BlockMeanHash";
		return hashName + "_" + this.thresholdCoefficient;
	}

}
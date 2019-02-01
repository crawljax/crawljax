package com.crawljax.stateabstractions.visual;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_java;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ColorHistogram {

	static {
		Loader.load(opencv_java.class);
	}

	private static final String NAME = "VISUAL-ColorHistogram";
	public double thresholdCoefficient = 0.0;

	public ColorHistogram() {
	}

	public Mat getHistogram(String img) {
		Mat hist1 = new Mat();
		Mat mat1 = Imgcodecs.imread(img);

		List<Mat> images = new ArrayList<>();
		images.add(mat1);

		Imgproc.calcHist(images, new MatOfInt(0, 1), new Mat(), hist1, new MatOfInt(256, 256),
				new MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f));
		return hist1;
	}

	public static double compare(Mat hist1, Mat hist2) {
		return Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
	}

	public String getName() {
		return NAME + "_" + this.thresholdCoefficient;
	}

}

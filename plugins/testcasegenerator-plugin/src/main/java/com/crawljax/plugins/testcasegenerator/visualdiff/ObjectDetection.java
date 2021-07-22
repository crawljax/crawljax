package com.crawljax.plugins.testcasegenerator.visualdiff;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.IPageObjectFactory;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.Page;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.PageObject;
import com.crawljax.stateabstractions.visual.OpenCVLoad;

/**
 * Detects objects on web pages with image processing.
 */
public class ObjectDetection {
	static {
		OpenCVLoad.load();
	}

	private Mat image;
	private Mat adjusted;
	private Mat annotated;

	private IPageObjectFactory pageObjectFactory;

	private List<PageObject> pageObjects;


	public static void directoryCheck(String dir) throws IOException {
		final File file = new File(dir);

		if (!file.exists()) {
			FileUtils.forceMkdir(file);
		}
	}

	/**
	 * @param pageObjectFactory
	 *            Factory which builds the desired {@code
	 * 			  PageObject} type.
	 * @param inputFile
	 *            Path to the screenshot of the web page to process.
	 */
	public ObjectDetection(IPageObjectFactory pageObjectFactory, String inputFile) {
		this.pageObjectFactory = pageObjectFactory;
		this.image = Imgcodecs.imread(inputFile);
		this.adjusted = zeros();
		this.annotated = zeros();
		this.pageObjects = new LinkedList<PageObject>();
	}

	/**
	 * Detect objects on the web page. The annotated image is available through {@code getAnnotated}
	 * , while the processed image is available through {@code getAdjusted}.
	 */
	public void detectObjects() {

		/* Convert to greyscale. */
		Imgproc.cvtColor(image, adjusted, Imgproc.COLOR_BGR2GRAY);

		/* Detect edges using Sobel. */
		adjusted = detectEdges(adjusted);
		Imgcodecs.imwrite("output/edges.png", adjusted);

		/*
		 * Increase sensitivity to light by adjusting the gain. This doesn't have an effect on
		 * object detection... it is purely to make the edges easier for us to see.
		 */
		adjusted = adjustGain(adjusted, 5, 0);
		Imgcodecs.imwrite("output/gain.png", adjusted);

		/* Remove horizontal and vertical lines. */
		adjusted = removeLines(adjusted);
		Imgcodecs.imwrite("output/lines.png", adjusted);

		/* Convert to binary by thresholding */
		adjusted = threshold(adjusted);
		Imgcodecs.imwrite("output/thresh.png", adjusted);

		/* Blur adjacent regions together. */
		adjusted = blur(adjusted);
		Imgcodecs.imwrite("output/blur.png", adjusted);

		/* Find and bound connected components. */
		annotated = drawContours(adjusted, image);
		Imgcodecs.imwrite("output/contours.png", annotated);

	}

	/**
	 * @return The original image.
	 */
	public Mat getImage() {
		return image;
	}

	/**
	 * @return The adjusted image.
	 */
	public Mat getAdjusted() {
		return adjusted;
	}

	/**
	 * @return The annotated image.
	 */
	public Mat getAnnotated() {
		return annotated;
	}

	/**
	 * @return The objects found on the page.
	 */
	public List<PageObject> getPageObjects() {
		return pageObjects;
	}

	/**
	 * @return the abstract page with identified objects.
	 */
	public Page getPage() {
		return new Page(image, pageObjects);
	}

	/**
	 * Adjusts what is commonly known as 'brightness' and 'contrast'. See
	 * http://docs.opencv.org/2.4/doc/tutorials/core/basic_linear_transform/basic_linear_transform.
	 * html
	 * 
	 * @param alpha
	 *            (gain/contrast) [1.0-3.0]
	 * @param beta
	 *            (bias/brightness) [0-100]
	 */
	private Mat adjustGain(Mat image, double alpha, int beta) {
		Mat gainAdjusted = zeros();
		image.convertTo(gainAdjusted, image.type(), alpha, beta);
		return gainAdjusted;
	}

	/**
	 * Detect edges in the image using Sobel method.
	 */
	private Mat detectEdges(Mat image) {

		Mat grad = zeros();
		Mat grad_x = zeros(), grad_y = zeros();
		Mat abs_grad_x = zeros(), abs_grad_y = zeros();

		int scale = 1;
		int delta = 0;
		int ddepth = CvType.CV_16UC1; // use 16 bit so we don't overflow

		/// Gradient X
		Imgproc.Sobel(image, grad_x, ddepth, 1, 0, 3, scale, delta, Core.BORDER_DEFAULT);
		Core.convertScaleAbs(grad_x, abs_grad_x);

		/// Gradient Y
		Imgproc.Sobel(image, grad_y, ddepth, 0, 1, 3, scale, delta, Core.BORDER_DEFAULT);
		Core.convertScaleAbs(grad_y, abs_grad_y);

		/// Total Gradient (approximate)
		Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);

		return grad;

	}

	/**
	 * Remove horizontal and vertical lines.
	 */
	private Mat removeLines(Mat image) {

		Mat adjusted = zeros();

		Mat horizontalElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 3));
		Mat verticalElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 1));

		Imgproc.erode(image, adjusted, horizontalElement);
		Imgproc.dilate(adjusted, adjusted, horizontalElement);

		Imgproc.erode(adjusted, adjusted, verticalElement);
		Imgproc.dilate(adjusted, adjusted, verticalElement);

		return adjusted;

	}

	/**
	 * Dilate the image so related features become connected.
	 */
	private Mat blur(Mat image) {
		Mat adjusted = zeros();
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 8));
		Imgproc.dilate(image, adjusted, element);
		return adjusted;
	}

	/**
	 * Convert an image to binary using a threshold.
	 * 
	 * @return a binary image
	 */
	private Mat threshold(Mat image) {
		Mat adjusted = zeros();
		Imgproc.threshold(image, adjusted, 0, 255, Imgproc.THRESH_BINARY);
		return adjusted;
	}

	/**
	 * Detects connected components (objects) by tracing contours.
	 */
	private Mat drawContours(Mat adjusted, Mat image) {

		Mat overlayed = image.clone();

		/*
		 * Find the contours. We use RETR_EXTERNAL so that we don't get spurious results from inside
		 * objects. For nested objects, use RETR_LIST (or RETR_TREE to get the actual structure).
		 */
		Mat hierarchy = zeros();
		List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
		Imgproc.findContours(adjusted, contours, hierarchy, Imgproc.RETR_EXTERNAL,
		        Imgproc.CHAIN_APPROX_SIMPLE);

		/* Draw the contours. */
		Random rand = new Random();
		for (int i = 0; i < contours.size(); i++) {
			Scalar color = new Scalar(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
			Imgproc.drawContours(adjusted, contours, i, color);
		}

		/* Draw a bounding box around each contour. */
		for (MatOfPoint contour : contours) {
			Rect box = Imgproc.boundingRect(contour);
			Scalar color = new Scalar(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
			if (box.size().area() > 200) {
				Imgproc.rectangle(overlayed, box.tl(), box.br(), color, 3);
				Mat objImg = new Mat(this.image, new Rect(box.x, box.y, box.width, box.height));
				PageObject pageObject = pageObjectFactory.makePageObject(objImg, box.x, box.y,
				        box.width, box.height);
				this.pageObjects.add(pageObject);
			}
		}

		return overlayed;

	}

	/**
	 * @return An empty image with dimensions and type of the original.
	 */
	private Mat zeros() {
		return Mat.zeros(image.size(), CvType.CV_8UC1);
	}

}

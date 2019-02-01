package com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects;

import org.opencv.core.Mat;

public class AveragePageObjectFactory implements IPageObjectFactory {

	public PageObject makePageObject(Mat image, int x, int y, int width, int height) {
		return new PageObjectAverage(image, x, y, width, height);
	}

}

package com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects;

import org.opencv.core.Mat;

public interface IPageObjectFactory {
	PageObject makePageObject(Mat image, int x, int y, int width, int height);
}

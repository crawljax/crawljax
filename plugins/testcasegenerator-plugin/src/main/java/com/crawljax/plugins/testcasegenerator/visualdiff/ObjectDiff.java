package com.crawljax.plugins.testcasegenerator.visualdiff;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.Page;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.PageObject;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.PageObject.ChangeType;

/**
 * Compares objects detected on two web pages.
 */
public class ObjectDiff {

	private boolean annotateUnchanged;

	private Page oldPage;
	private Page newPage;

	public ObjectDiff(Page oldPage, Page newPage) {
		this(oldPage, newPage, false);
	}

	/**
	 * @param oldPage
	 *            The screenshot of the old page.
	 * @param newPage
	 *            The screenshot of the new page.
	 * @param annotateUnchanged
	 *            Set to {@code true} to also annotate unchanged objects.
	 */
	public ObjectDiff(Page oldPage, Page newPage, boolean annotateUnchanged) {
		this.oldPage = oldPage;
		this.newPage = newPage;
		this.annotateUnchanged = annotateUnchanged;
	}

	/**
	 * Annotate each of the {@code PageObject}s with a change label. Currently uses a naive method
	 * of matching objects based on their image (MD5) hash, size, and location. A better algorithm
	 * can certainly be developed which uses: (1) Perceptual hashing, for finding approximate image
	 * matches (2) Graph diff, for finding blocks of elements that have moved (3) Text diff, for
	 * identifying where text has changed
	 */
	public void diff() {

		/* Label PageObjects which we can find matches for. */
		newPage.filterExactMatches(oldPage);
		newPage.filterGeographicMatches(oldPage);
		newPage.filterHashMatches(oldPage);

		/* Everything left over was either inserted or deleted. */
		for (PageObject pageObject : oldPage.getUnclassifiedObjects())
			pageObject.setChangeType(ChangeType.REMOVED);
		for (PageObject pageObject : newPage.getUnclassifiedObjects())
			pageObject.setChangeType(ChangeType.INSERTED);

	}

	/**
	 * @return the old page with change labels.
	 */
	public Mat annotateOldPage() {
		return annotatePage(oldPage);
	}

	/**
	 * @return the new page with change labels.
	 */
	public Mat annotateNewPage() {
		return annotatePage(newPage);
	}

	/**
	 * @return {@code true} iff the transformation contains one or more edit operations.
	 */
	public boolean hasChanges() {
		for (PageObject po : oldPage.getPageObjects())
			if (po.getChangeType() != ChangeType.UNCHANGED)
				return true;
		for (PageObject po : newPage.getPageObjects())
			if (po.getChangeType() != ChangeType.UNCHANGED)
				return true;
		return false;
	}

	/**
	 * Annotate the images with change labels.
	 */
	private Mat annotatePage(Page page) {

		Mat overlayed = page.getImage().clone();

		/*
		 * Draw a bounding box around each page objects, coloured according to the page object's
		 * change type.
		 */
		for (PageObject pageObject : page.getPageObjects()) {
			Point tl = new Point(pageObject.getX(), pageObject.getY());
			Point br = new Point(pageObject.getX() + pageObject.getWidth(),
			        pageObject.getY() + pageObject.getHeight());
			if (annotateUnchanged || pageObject.getChangeType() != ChangeType.UNCHANGED)
				Imgproc.rectangle(overlayed, tl, br, pageObject.getChangeColor(), 3);
		}

		return overlayed;

	}

}

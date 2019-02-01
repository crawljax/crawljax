package com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Mat;

import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.PageObject.ChangeType;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.PageObject.ExactMatchComparator;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.PageObject.HashMatchComparator;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;

/**
 * Stores and compares objects which were detected on a page.
 */
public class Page {

	/** The image of the page. **/
	private Mat image;

	/** All objects on the page. **/
	private List<PageObject> pageObjects;

	/** The set of objects which have not been classified. */
	private Set<PageObject> unclassified;

	public Page(Mat image, List<PageObject> pageObjects) {
		this.image = image;
		this.pageObjects = pageObjects;
		this.unclassified = new HashSet<PageObject>(pageObjects);
	}

	public List<PageObject> getPageObjects() {
		return this.pageObjects;
	}

	public Set<PageObject> getUnclassifiedObjects() {
		return this.unclassified;
	}

	public Mat getImage() {
		return this.image;
	}

	/**
	 * Classifies and filters exact {@code PageObject} matches as {@code
	 * UNCHANGED}.
	 * 
	 * @param otherPage
	 *            The other version of the page.
	 */
	public void filterExactMatches(Page otherPage) {

		/* Build the hash set for detecting matches. */
		Map<ExactMatchComparator, PageObject> otherObjects =
		        new HashMap<ExactMatchComparator, PageObject>();
		for (PageObject pageObject : otherPage.getUnclassifiedObjects()) {
			otherObjects.put(pageObject.getComparatorExactMatch(), pageObject);
		}

		/* Find exact matches. */
		List<PageObject> toRemove = new LinkedList<PageObject>();
		for (PageObject pageObject : this.getUnclassifiedObjects()) {
			PageObject otherObject = otherObjects.get(pageObject.getComparatorExactMatch());
			if (otherObject == null)
				continue;
			otherObject.setChangeType(ChangeType.UNCHANGED);
			pageObject.setChangeType(ChangeType.UNCHANGED);
			otherPage.getUnclassifiedObjects().remove(otherObject);
			toRemove.add(pageObject);
		}
		this.getUnclassifiedObjects().removeAll(toRemove);

	}

	/**
	 * Classifies and filters hash {@code PageObject} matches as {@code
	 * UNCHANGED}.
	 * 
	 * @param otherPage
	 *            The other version of the page.
	 */
	public void filterHashMatches(Page otherPage) {

		/* Build the hash set for detecting matches. */
		Map<HashMatchComparator, PageObject> otherObjects =
		        new HashMap<HashMatchComparator, PageObject>();
		for (PageObject pageObject : otherPage.getUnclassifiedObjects()) {
			otherObjects.put(pageObject.getComparatorHash(), pageObject);
		}

		/* Find exact matches. */
		List<PageObject> toRemove = new LinkedList<PageObject>();
		for (PageObject pageObject : this.getUnclassifiedObjects()) {
			PageObject otherObject = otherObjects.get(pageObject.getComparatorHash());
			if (otherObject == null || otherObject.changeType != ChangeType.UNKNOWN)
				continue;
			otherObject.setChangeType(ChangeType.MOVED);
			pageObject.setChangeType(ChangeType.MOVED);
			otherPage.getUnclassifiedObjects().remove(otherObject);
			toRemove.add(pageObject);
		}
		this.getUnclassifiedObjects().removeAll(toRemove);

	}

	/**
	 * Classifies and filters geographic {@code PageObject} matches as {@code
	 * UPDATED}.
	 * 
	 * @param otherPage
	 */
	public void filterGeographicMatches(Page otherPage) {

		/* Build the RTree for detecting matches. */
		RTree<PageObject, Rectangle> otherObjects = RTree.star().create();
		for (PageObject pageObject : otherPage.getUnclassifiedObjects())
			otherObjects = otherObjects.add(pageObject, pageObject.getRectangle());

		/* Find geographic matches. */
		List<PageObject> toRemove = new LinkedList<PageObject>();
		for (PageObject pageObject : this.getUnclassifiedObjects()) {
			List<Entry<PageObject, Rectangle>> results = otherObjects
			        .search(pageObject.getRectangle(), 10).toList().toBlocking().single();
			for (Entry<PageObject, Rectangle> entry : results) {
				PageObject otherObject = entry.value();
				if (pageObject.getComparatorGeographic()
				        .equals(otherObject.getComparatorGeographic())) {
					otherObject.setChangeType(ChangeType.UPDATED);
					pageObject.setChangeType(ChangeType.UPDATED);
					otherPage.getUnclassifiedObjects().remove(otherObject);
					toRemove.add(pageObject);
				}
			}
		}
		this.getUnclassifiedObjects().removeAll(toRemove);

	}

}

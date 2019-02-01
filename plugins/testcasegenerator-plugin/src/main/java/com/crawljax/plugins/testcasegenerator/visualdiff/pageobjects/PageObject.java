package com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects;

import java.util.Arrays;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;

/**
 * Stores the information for an object which was detected on the page.
 */
public abstract class PageObject {

	/* Details about the image. */
	protected Mat image;
	protected byte[] imageHash;
	protected String imageText;

	/* Details about the position and size. */
	protected int x;
	protected int y;
	protected int width;
	protected int height;

	/* How the object has changed. */
	protected ChangeType changeType;

	/**
	 * @param image
	 *            The image inside the object's bounding box.
	 * @param x
	 *            The x co-ordinate of the top left of the object's bounding box.
	 * @param y
	 *            The y co-ordinate of the top left of the object's bounding box.
	 * @param width
	 *            The width of the object's bounding box.
	 * @param height
	 *            The height of the object's bounding box.
	 * @return a new instance of the {@code PageObject}.
	 */
	PageObject(Mat image, int x, int y, int width, int height) {
		this.image = image;
		this.imageHash = createImageHash();
		this.imageText = createImageText();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.changeType = ChangeType.UNKNOWN;
	}

	/**
	 * @return the image pixels inside the object's bounding box.
	 */
	public Mat getImage() {
		return image;
	}

	/**
	 * @return the x co-ordinate of the top left of the object's bounding box.
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y co-ordinate of the top left of the object's bounding box.
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the width of the object's bounding box.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height of the object's bounding box.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param changeType
	 *            The change operation for this object in the diff.
	 */
	public void setChangeType(ChangeType changeType) {
		this.changeType = changeType;
	}

	/**
	 * @return the change operation for this object in the diff.
	 */
	public ChangeType getChangeType() {
		return changeType;
	}

	/**
	 * @return The bounding rectangle for the RTree.
	 */
	public Rectangle getRectangle() {
		return Geometries.rectangle(x, y, x + width, y + height);
	}

	/**
	 * @return Returns true if the images are equal under some function.
	 */
	public boolean imageEquals(PageObject po) {
		return Arrays.equals(this.getImageHash(), po.getImageHash());
	}

	/**
	 * @return the hash value of the object image.
	 */
	public byte[] getImageHash() {
		return this.imageHash;
	}

	/**
	 * @return the hash value of the object image.
	 */
	protected abstract byte[] createImageHash();

	/**
	 * @return the text within the object image.
	 */
	public String getImageText() {
		return this.imageText;
	}

	/**
	 * @return the text within the object image.
	 */
	protected abstract String createImageText();

	/**
	 * @return a comparator for finding exact matches on the page.
	 */
	public ExactMatchComparator getComparatorExactMatch() {
		return new ExactMatchComparator(this);
	}

	/**
	 * @return a comparator for finding hash matches on the page.
	 */
	public HashMatchComparator getComparatorHash() {
		return new HashMatchComparator(this);
	}

	/**
	 * @return a comparator for finding geographic matches on the page.
	 */
	public GeographicMatchComparator getComparatorGeographic() {
		return new GeographicMatchComparator(this);
	}

	/**
	 * @return {@code true} if the difference between {@code a} and {@code b} is * within
	 *         {@code tolerance}.
	 */
	public static boolean approx(int a, int b, int tolerance) {
		return Math.abs(a - b) < tolerance;
	}

	public Scalar getChangeColor() {
		// NOTE: Scalar uses (b,g,r), while color pickers use (r,g,b)
		switch (changeType) {
			case UNCHANGED:
				return new Scalar(135, 135, 135);
			case MOVED:
				return new Scalar(12, 158, 255);
			case UPDATED:
				return new Scalar(252, 204, 32);
			case REMOVED:
				return new Scalar(32, 32, 252); // BGR vs RGB
			case INSERTED:
				return new Scalar(0, 214, 17);
			case UNKNOWN:
			default:
				return new Scalar(0, 0, 0);
		}
	}

	public enum ChangeType {
		UNKNOWN,
		UNCHANGED,
		INSERTED,
		REMOVED,
		MOVED,
		UPDATED
	}

	/**
	 * Compares PageObject based on its position, size and image hash.
	 */
	public class ExactMatchComparator {

		private PageObject pageObject;

		private ExactMatchComparator(PageObject pageObject) {
			this.pageObject = pageObject;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(imageHash);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ExactMatchComparator))
				return false;
			PageObject other = ((ExactMatchComparator) o).pageObject;
			return Arrays.equals(pageObject.getImageHash(), other.imageHash)
			        && approx(pageObject.getX(), other.getX(), 5)
			        && approx(pageObject.getY(), other.getY(), 5)
			        && approx(pageObject.getWidth(), other.getWidth(), 5)
			        && approx(pageObject.getHeight(), other.getHeight(), 5);
		}

	}

	/**
	 * Compares PageObject based on its image hash.
	 */
	public class HashMatchComparator {

		private PageObject pageObject;

		private HashMatchComparator(PageObject pageObject) {
			this.pageObject = pageObject;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(imageHash);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HashMatchComparator))
				return false;
			PageObject other = ((HashMatchComparator) o).pageObject;
			return Arrays.equals(pageObject.getImageHash(), other.imageHash)
			        && approx(pageObject.getWidth(), other.getWidth(), 5)
			        && approx(pageObject.getHeight(), other.getHeight(), 5);
		}

	}

	/**
	 * Compares PageObject based on its position and size.
	 */
	public class GeographicMatchComparator {

		private PageObject pageObject;

		private GeographicMatchComparator(PageObject pageObject) {
			this.pageObject = pageObject;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof GeographicMatchComparator))
				return false;
			PageObject other = ((GeographicMatchComparator) o).pageObject;
			return approx(pageObject.getX(), other.getX(), 10)
			        && approx(pageObject.getY(), other.getY(), 10)
			        && approx(pageObject.getWidth(), other.getWidth(), 5)
			        && approx(pageObject.getHeight(), other.getHeight(), 5);
		}

	}

}
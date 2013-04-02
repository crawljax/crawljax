package com.crawljax.plugins.crawloverview.model;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import com.crawljax.core.CandidateElement;

/**
 * Position of a candidate element of a state. This type is used to build the overlays of screenshot
 * to show where the {@link CandidateElement}s were located.
 */
@Immutable
public class CandidateElementPosition {

	private final int top;
	private final int left;
	private final String xpath;
	private final int width;
	private final int height;

	/**
	 * @param xpath
	 * @param location
	 *            The element's offset.
	 * @param size
	 *            The size of the element.
	 */
	public CandidateElementPosition(String xpath, Point location, Dimension size) {
		this.top = location.y;
		this.left = location.x;
		this.xpath = xpath;
		this.width = size.width;
		this.height = size.height;
	}

	/**
	 * @return The offset to the top of the document.
	 */
	public int getTop() {
		return top;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * @return The offset to the left of the document.
	 */
	public int getLeft() {
		return left;
	}

	public String getXpath() {
		return xpath;
	}

	@Override
	public String toString() {
		return "CandidateElementPosition [top=" + top + ", left=" + left + ", xpath=" + xpath
		        + ", width=" + width + ", height=" + height + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(height, left, top, width, xpath);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CandidateElementPosition other = (CandidateElementPosition) obj;
		return Objects.equals(height, other.height)
		        && Objects.equals(left, other.left)
		        && Objects.equals(top, other.top)
		        && Objects.equals(width, other.width)
		        && Objects.equals(xpath, other.xpath);
	}
}

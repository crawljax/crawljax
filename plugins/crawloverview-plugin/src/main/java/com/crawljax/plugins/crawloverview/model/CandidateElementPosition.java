package com.crawljax.plugins.crawloverview.model;

import javax.annotation.concurrent.Immutable;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import com.crawljax.core.CandidateElement;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Position of a candidate element of a state. This type is used to build the overlays of screenshot
 * to show where the {@link CandidateElement}s were located.
 */
@Immutable
public class CandidateElementPosition {

	private final int top;
	private final int left;
	private String xpath;
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

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

	@JsonCreator
	public CandidateElementPosition(@JsonProperty("top") int top,
	        @JsonProperty("left") int left, @JsonProperty("xpath") String xpath,
	        @JsonProperty("width") int width, @JsonProperty("height") int height) {
		this.top = top;
		this.left = left;
		this.xpath = xpath;
		this.width = width;
		this.height = height;
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
		return MoreObjects.toStringHelper(this)
		        .add("top", top)
		        .add("left", left)
		        .add("xpath", xpath)
		        .add("width", width)
		        .add("height", height)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(top, left, xpath, width, height);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof CandidateElementPosition) {
			CandidateElementPosition that = (CandidateElementPosition) object;
			return Objects.equal(this.top, that.top)
			        && Objects.equal(this.left, that.left)
			        && Objects.equal(this.xpath, that.xpath)
			        && Objects.equal(this.width, that.width)
			        && Objects.equal(this.height, that.height);
		}
		return false;
	}

}

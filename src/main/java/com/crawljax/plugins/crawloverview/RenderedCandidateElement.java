package com.crawljax.plugins.crawloverview;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.w3c.dom.Element;

import com.crawljax.core.CandidateElement;

public class RenderedCandidateElement extends CandidateElement {

	private final Point location;
	private final Dimension size;

	protected RenderedCandidateElement(Element element, String xpath, Point location,
	        Dimension size) {
		super(element, xpath);
		this.location = location;
		this.size = size;
	}

	/**
	 * @return the location
	 */
	protected Point getLocation() {
		return location;
	}

	/**
	 * @return the size
	 */
	protected Dimension getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "RenderedCandidateElement [location=" + location + ", size=" + size + "]";
	}

}

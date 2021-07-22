package com.crawljax.stateabstractions.hybrid;

import java.util.Objects;

import org.openqa.selenium.Rectangle;

public class DOMElementWithVisualInfo {
	
	private String xpath;
	private String visualHash;
	private Rectangle boundingBox;
	
	public DOMElementWithVisualInfo(String xpath, Rectangle boundingBox) {
		this(xpath, boundingBox, null);
	}
	
	public DOMElementWithVisualInfo(String xpath, Rectangle boundingBox, String visualHash) {
		this.xpath = xpath;
		this.boundingBox = boundingBox;
		this.visualHash = visualHash;
	}
	
	public String getXpath() {
		return xpath;
	}
	
	public String getVisualHash() {
		return visualHash;
	}
	
	public Rectangle getBoundingBox() {
		return boundingBox;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("XPath: ").append(xpath).append(System.lineSeparator());
		stringBuilder.append("Position:");
		stringBuilder.append(getBoundingBoxString());
		if (this.visualHash != null) {
			stringBuilder.append(System.lineSeparator()).append("Visual Hash: ").append(visualHash);
		}
		return stringBuilder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(xpath, visualHash, boundingBox);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DOMElementWithVisualInfo other = (DOMElementWithVisualInfo) obj;
		if (boundingBox == null) {
			if (other.boundingBox != null)
				return false;
		} else if (!boundingBox.equals(other.boundingBox))
			return false;
		if (visualHash == null) {
			if (other.visualHash != null)
				return false;
		} else if (!visualHash.equals(other.visualHash))
			return false;
		if (xpath == null) {
			if (other.xpath != null)
				return false;
		} else if (!xpath.equals(other.xpath))
			return false;
		return true;
	}
	
	private String getBoundingBoxString() {
		return getBoundingBoxDimensionsString(boundingBox);
	}
	
	public static String getBoundingBoxDimensionsString(Rectangle boundingBox) {
		String toReturn = "<x=%x, y=%s, width= %s, height= %s>";
		return String.format(toReturn, boundingBox.getX(), 
									boundingBox.getY(), 
									boundingBox.getWidth(),
									boundingBox.getHeight());

	}
	
}

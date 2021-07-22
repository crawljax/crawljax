package com.crawljax.plugins.testcasegenerator.fragDiff;

import java.awt.Rectangle;

public class ImageAnnotation {
	Rectangle rectangle;
	AnnotationType type;
	boolean fill;
	
	public enum AnnotationType{
		ADDED, CHANGED, DYNAMIC, NONE
	}
	
	public ImageAnnotation(AnnotationType type, Rectangle rect, boolean fill) {
		this.type = type;
		this.rectangle = rect;
		this.fill = fill;
	}
	public Rectangle getRectangle() {
		return rectangle;
	}
	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
	public AnnotationType getType() {
		return type;
	}
	public void setType(AnnotationType type) {
		this.type = type;
	}
	public boolean isFill() {
		return fill;
	}
	public void setFill(boolean fill) {
		this.fill = fill;
	}
	
}	

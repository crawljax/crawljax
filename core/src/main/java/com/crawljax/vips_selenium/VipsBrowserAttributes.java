package com.crawljax.vips_selenium;

import java.awt.Rectangle;

public class VipsBrowserAttributes {
	String bgcolor;
	int fontsize;
	int fontweight;
	Rectangle rectangle;
	boolean isDisplayed;
	String eventListeners;
	
	public String getEventListeners() {
		return eventListeners;
	}
	public void setEventListeners(String eventListeners) {
		this.eventListeners = eventListeners;
	}
	public boolean isDisplayed() {
		return isDisplayed;
	}
	public void setDisplayed(boolean isDisplayed) {
		this.isDisplayed = isDisplayed;
	}
	public VipsBrowserAttributes(Rectangle rect, int fontSize, int fontWeight, String bgColor, boolean isDisplayed2, String eventListeners) {
		this.bgcolor = bgColor;
		this.rectangle= rect;
		this.fontsize= fontSize;
		this.fontweight  =fontWeight;
		this.isDisplayed = isDisplayed2;
		this.eventListeners = eventListeners;
	}
	public String getBgcolor() {
		return bgcolor;
	}
	public void setBgcolor(String bgcolor) {
		this.bgcolor = bgcolor;
	}
	public int getFontsize() {
		return fontsize;
	}
	public void setFontsize(int fontsize) {
		this.fontsize = fontsize;
	}
	public int getFontweight() {
		return fontweight;
	}
	public void setFontweight(int fontweight) {
		this.fontweight = fontweight;
	}
	public Rectangle getRectangle() {
		return rectangle;
	}
	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
	
}

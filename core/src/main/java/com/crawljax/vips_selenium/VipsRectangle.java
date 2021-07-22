package com.crawljax.vips_selenium;

import java.awt.Rectangle;
import java.util.List;

import org.w3c.dom.Node;

public class VipsRectangle {
	transient List<Node> nestedBlocks;
	
	public List<Node> getNestedBlocks() {
		return nestedBlocks;
	}



	public void setNestedBlocks(List<Node> nestedBlocks) {
		this.nestedBlocks = nestedBlocks;
	}

	String xpath;
	int id;
	int parentId;
	Rectangle rect;
	
	public VipsRectangle(List<Node> list, int id, int parentId, String xpath, Rectangle rect2) {
		this.nestedBlocks = list;
		this.id = id;
		this.parentId = parentId;
		this.xpath = xpath ;
		this.rect = rect2;
	}

	

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public Rectangle getRect() {
		return rect;
	}

	public void setRect(Rectangle rect) {
		this.rect = rect;
	}
	
	
}

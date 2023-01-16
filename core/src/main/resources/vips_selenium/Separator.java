/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Separator.java
 */

package com.crawljax.vips_selenium;

import java.awt.Point;

/**
 * Class that represents visual separator.
 * @author Tomas Popela
 *
 */
public class Separator implements Comparable<Separator> {
	public int startPoint = 0;
	public int endPoint = 0;
	public int weight = 3;
	public int normalizedWeight = 0;

	// for horizontal separators it means
	public Point leftUp;
	public Point rightDown;

	public Separator(int start, int end) {
		this.startPoint = start;
		this.endPoint = end;
	}

	public Separator(int start, int end, int weight) {
		this.startPoint = start;
		this.endPoint = end;
		this.weight = weight;
	}

	public Separator(int leftUpX, int leftUpY, int rightDownX, int rightDownY)
	{
		this.leftUp = new Point(leftUpX, leftUpY);
		this.rightDown = new Point(rightDownX, rightDownY);
		this.startPoint = leftUpX;
		this.endPoint = rightDownY;
	}

	public void setLeftUp(int leftUpX, int leftUpY)
	{
		this.leftUp = new Point(leftUpX, leftUpY);
	}

	public void setRightDown(int rightDownX, int rightDownY)
	{
		this.rightDown = new Point(rightDownX, rightDownY);
	}

	@Override
	public int compareTo(Separator otherSeparator)
	{
		return this.weight - otherSeparator.weight;
	}
}

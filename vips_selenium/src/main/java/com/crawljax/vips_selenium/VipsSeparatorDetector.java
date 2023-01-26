/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorDetector.java
 */

package com.crawljax.vips_selenium;

import java.util.List;
import org.w3c.dom.Node;

/**
 * Common interface for separators detectors.
 *
 * @author Tomas Popela
 */
public interface VipsSeparatorDetector {

  public void fillPool();

  public Node getVipsBlock();

  public void setVipsBlock(Node vipsBlock);

  public List<Node> getVisualBlocks();

  public void setVisualBlocks(List<Node> visualBlocks);

  public void detectHorizontalSeparators();

  public void detectVerticalSeparators();

  public List<Separator> getHorizontalSeparators();

  public void setHorizontalSeparators(List<Separator> separators);

  public List<Separator> getVerticalSeparators();

  public void setVerticalSeparators(List<Separator> separators);

  public void setCleanUpSeparators(int treshold);

  public boolean isCleanUpEnabled();

}

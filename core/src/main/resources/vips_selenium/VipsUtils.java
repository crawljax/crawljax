package com.crawljax.vips_selenium;

//import org.openqa.selenium.Rectangle;

import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.gson.Gson;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VipsUtils {

  private static final Logger LOG = LoggerFactory.getLogger(VipsUtils.class);
  private static final String RECTANGLE = "rectangle";
  private static final String ISVISUALBLOCK = "isvisualblock";
  private static final String ISDISPLAYED = "isdisplayed";
  private static final String ALREADYDIVIDED = "alreadydivided";
  private static final String PCOUNT = "pcount";
  private static final String CONTAINSTABLE = "containstable";
  private static final String VIPSLEVEL = "vipslevel";
  private static final String DOC = "doc";
  private static final String ISDIVIDABLE = "isdividable";
  private static final String IMAGECOUNT = "imagecount";
  private static final String LINKTEXTLENGTH = "linktextlength";
  private static final String BGCOLOR = "bgcolor";
  private static final String FONTWEIGHT = "fontweight";
  private static final String FONTSIZE = "fontsize";
  private static final String BACKGROUNDCOLOR = "background-color";
  private static final String FRAGPARENT = "fragparent";
  private static final String DYNAMIC_FRAGMENT = "dynamicfragment";
  private static final String POPULATED = "populated";
  private static final String DIRACCESS = "diraccess";
  private static final String INDIRACCESS = "indiraccess";
  private static final String JSOBSERVER = "jsobserver";
  private static final String ANYACCESS = "anyaccess";
  private static final String EVLIST = "evlist";
  private static final String EVLISTVAL = "evlistval";
  public static boolean USE_CDP = false;
  static String COMPUTEDSTYLESHEET_ALL = "return Array.from(%s).map(element => {return {xpath: element, attributes: getVipsAttributes(element)}});";
  static String CDP_COMPUTEDSTYLESHEET_ALL = "Array.from(%s).map(element => {return {xpath: element, attributes: getVipsAttributes(element)}});";
  static String CONTENT_RECTANGLE_JAVASCRIPT_FUNCTION =
      "function getInt(str, digits){\n" +
          "	var value = parseInt(str, digits);\n" +
          "	if(Number.isNaN(value)){\n" +
          "		return 0;\n" +
          "	}\n" +
          "	return value;\n" +
          "}\n" +
          "\n" +
          "function computeContentRect(a){\n" +
          "var map1 = a.computedStyleMap();\n" +
          "\n" +
          "var padding_left = getInt(map1.get('padding-left'), 10);\n" +
          "var padding_right = getInt(map1.get('padding-right'), 10);\n" +
          "var border_left_width = getInt(map1.get('border-left-width'), 10);\n" +
          "var border_right_width = getInt(map1.get('border-right-width'), 10);\n" +
          "\n" +
          "var padding_top = getInt(map1.get('padding-top'), 10);\n" +
          "var padding_bottom = getInt(map1.get('padding-bottom'), 10) ;\n" +
          "var border_top_width = getInt(map1.get('border-top-width'), 10);\n" +
          "var border_bottom_width = getInt(map1.get('border-bottom-width'), 10) ;\n" +
          "\n" +
          "var font_size = getInt(map1.get('font-size'), 10);\n" +
          "\n" +
          "\n" +
          "var width = a.getBoundingClientRect().width -  (padding_left + padding_right + border_left_width + border_right_width );\n"
          +
          "var height = a.getBoundingClientRect().height - (padding_top + padding_bottom  + border_top_width + border_bottom_width);\n"
          +
          "var x = scrollX + a.getBoundingClientRect().x + (padding_left +  border_left_width);\n" +
          "var y = scrollY + a.getBoundingClientRect().y + (padding_top + border_top_width);\n" +
          "var returnString = Math.round(x) +  \" : \" + Math.round(y) + \" : \" + Math.round(width) + \" : \" + Math.round(height) + ':' + Math.round(font_size);\n"
          +
          "console.log(returnString);\n" +
          "return returnString;\n" +
          "}";
  static String CONTENT_RECTANGLE_RETURN = "return computeContentRect(arguments[0]);";
  private static boolean useScript = true;

  public static String[] getVipsAttributes() {
    String[] returnArray = {RECTANGLE, ISDISPLAYED, ISDIVIDABLE, ISVISUALBLOCK, ALREADYDIVIDED,
        PCOUNT,
        CONTAINSTABLE, VIPSLEVEL, DOC, IMAGECOUNT, LINKTEXTLENGTH,
        BGCOLOR, FONTSIZE, FONTWEIGHT, BACKGROUNDCOLOR, FRAGPARENT, POPULATED, DYNAMIC_FRAGMENT,
        DIRACCESS, INDIRACCESS};
    return returnArray;
  }

  //	public static Document dom = null;
//	public static VipsSelenium vips = null;
//
  public static void cleanDom(Document dom, boolean offline) {
    Node root = dom.getDocumentElement();
    cleanNode(root, offline);
    root.normalize();
  }

  private static void removeOfflineAttributes(Node node) {
    if (!node.hasAttributes()) {
      return;
    }
    if (node.getAttributes().getNamedItem(FRAGPARENT) != null) {
      node.getAttributes().removeNamedItem(FRAGPARENT);
    }
  }

  private static void cleanNode(Node root, boolean offline) {
    if (offline) {
      // Remove fragment id attribute so that it can be created again
      if (root.hasAttributes()) {
        removeOfflineAttributes(root);
      }
    }
    NodeList childNodes = root.getChildNodes();
    ArrayList<Node> toRemove = new ArrayList<Node>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if (childNode.getAttributes() == null) {
        if (childNode.getNodeName().equalsIgnoreCase("#text")) {
          if (!childNode.getTextContent().trim().isEmpty()) {
            continue;
          }
        }
        toRemove.add(childNode);
      } else {
        cleanNode(childNode, offline);
      }
    }
    for (Node toRemoveNode : toRemove) {
      toRemoveNode.getParentNode().removeChild(toRemoveNode);
    }
//
//		if(root.getChildNodes().getLength()>1) {
//			childNodes = root.getChildNodes();
//			List<Node> textNodes = new ArrayList<Node>();
//			for(int i = 0; i<childNodes.getLength(); i++) {
//				Node childNode = childNodes.item(i);
//				if(childNode.getAttributes() == null) {
//					if(childNode.getNodeName().equalsIgnoreCase("#text")) {
//						if(!childNode.getTextContent().trim().isEmpty()) {
//							textNodes.add(childNode);
//						}
//					}
//				}
//			}
//			for(Node textNode: textNodes) {
//				Node dummyTextNode = root.getOwnerDocument().createElement("text");
//				dummyTextNode.setTextContent(textNode.getTextContent());
//				textNode.getParentNode().replaceChild(dummyTextNode, textNode);
//			}
//		}

  }

  public static ArrayList<Node> getChildren(Node vipsBlock) {
    if (vipsBlock == null || vipsBlock.getNodeName().equalsIgnoreCase("#text")
        || vipsBlock.getChildNodes() == null) {
      return new ArrayList<>();
    }
    NodeList childNodes = vipsBlock.getChildNodes();
    ArrayList<Node> children = new ArrayList<Node>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node child = childNodes.item(i);
      if (child == null) {
        continue;
      }
      if (child.getNodeName().equalsIgnoreCase("#text")) {
        if (child.getTextContent().trim().isEmpty()) {
          continue;
        }
      }
      children.add(child);
    }
    return children;
  }

  public static void setDoC(Node vipsBlock, int i) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(DOC);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(DOC).setNodeValue(Integer.toString(i));
  }

  public static int getDoC(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return getDoC(vipsBlock.getParentNode());
    }
    if (vipsBlock.getAttributes().getNamedItem(DOC) == null) {
      return 11;
    }
    String value = vipsBlock.getAttributes().getNamedItem(DOC).getNodeValue();
    return Integer.parseInt(value);
  }

  public static void setIsVisualBlock(Node vipsBlock, boolean b) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(ISVISUALBLOCK);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(ISVISUALBLOCK).setNodeValue(b ? "true" : "false");
    checkProperties(vipsBlock);
  }

  public static void checkProperties(Node vipsBlock) {
    checkIsImg(vipsBlock);
    checkContainImg(vipsBlock);
    checkContainTable(vipsBlock);
    checkContainP(vipsBlock);
    countLinkTextLength(vipsBlock);
    setSourceIndex(vipsBlock);
  }

  public static void setSourceIndex(Node vipsBlock) {
    // TODO Auto-generated method stub

  }

  public static void countLinkTextLength(Node vipsBlock) {
    int ltl = 0;
    if (vipsBlock.getNodeName().equalsIgnoreCase("a")) {
      ltl += vipsBlock.getTextContent().trim().length();
    }
    NodeList childNodes = vipsBlock.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childVipsBlock = childNodes.item(i);
      if (childVipsBlock.getNodeName().equalsIgnoreCase("a")) {
        ltl += childVipsBlock.getTextContent().trim().length();
      }
    }
    setLinkTextLength(vipsBlock, ltl);
  }

  public static void checkContainP(Node vipsBlock) {
    int containP = 0;
    if (vipsBlock.getNodeName().equalsIgnoreCase("p")) {
      containP++;
    }
    NodeList childNodes = vipsBlock.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childVipsBlock = childNodes.item(i);
      if (childVipsBlock.getNodeName().equalsIgnoreCase("p")) {
        containP++;
      }
    }
    setPCount(vipsBlock, containP);
  }

  public static void checkContainTable(Node vipsBlock) {
    boolean containTable = false;
    if (vipsBlock.getNodeName().equalsIgnoreCase("table")) {
      containTable = true;
    }

    NodeList childNodes = vipsBlock.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childVipsBlock = childNodes.item(i);
      if (childVipsBlock.getNodeName().equalsIgnoreCase("table")) {
        containTable = true;
      }
    }
    if (containTable) {
      setContainsTable(vipsBlock);
    }
  }

  public static void checkContainImg(Node vipsBlock) {
    int containImg = 0;
    if (vipsBlock.getNodeName().equalsIgnoreCase("img")) {
      containImg++;
    }
    NodeList childNodes = vipsBlock.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childVipsBlock = childNodes.item(i);
      if (checkIsImg(childVipsBlock)) {
        containImg++;
      }
    }
    setImageCount(vipsBlock, containImg);
  }

  public static boolean checkIsImg(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("img")) {
      return true;
    } else {
      return false;
    }
  }

  public static void setAlreadyDivided(Node vipsBlock) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(ALREADYDIVIDED);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
  }

  public static boolean isAlreadyDivided(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return isAlreadyDivided(vipsBlock.getParentNode());
    }
    return vipsBlock.getAttributes().getNamedItem(ALREADYDIVIDED) != null;
  }

  public static void setContainsTable(Node vipsBlock) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(CONTAINSTABLE);
    vipsBlock.getAttributes().setNamedItem(newAttribute);

  }

  public static boolean containsTable(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return containsTable(vipsBlock.getParentNode());
    }
    return vipsBlock.getAttributes().getNamedItem(CONTAINSTABLE) != null;
  }

  public static void setRectangle(Node vipsBlock, Rectangle rect) {
    Gson gson = new Gson();
    String rectString = gson.toJson(rect);
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(RECTANGLE);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(RECTANGLE).setNodeValue(rectString);
  }

  public static boolean isDisplayed(Node vipsBlock, WebDriver driver) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return isDisplayed(vipsBlock.getParentNode(), driver);
    }

    if (vipsBlock.getAttributes().getNamedItem(ISDISPLAYED) != null) {
      String str = vipsBlock.getAttributes().getNamedItem(ISDISPLAYED).getNodeValue();
      return str.equalsIgnoreCase("true");
    }

    if (driver == null) {
      setDisplayedUsingRectangle(vipsBlock);
      LOG.debug("Cannot find isDisplayed for {}. Using rectangle", vipsBlock.getNodeName());
      return isDisplayed(vipsBlock, driver);
    }

    boolean isDisplayed = false;
    try {
      String xpath = XPathHelper.getXPathExpression(vipsBlock);
      WebElement element = driver.findElement(By.xpath(xpath));
      isDisplayed = element.isDisplayed();
    } catch (Exception ex) {

    }

    setIsDisplayed(vipsBlock, isDisplayed ? "true" : "false");
    return isDisplayed;
  }

  private static void setDisplayedUsingRectangle(Node vipsBlock) {
    Rectangle rectangle = getRectangle(vipsBlock, null);
    boolean isDisplayed = rectangle != null &&
        rectangle.height > 0 && rectangle.width > 0;
    setIsDisplayed(vipsBlock, isDisplayed ? "true" : "false");
  }

  public static void setIsDisplayed(Node vipsBlock, String value) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(ISDISPLAYED);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(ISDISPLAYED).setNodeValue(value);
  }

  public static void setIsDividable(Node vipsBlock, boolean b) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(ISDIVIDABLE);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(ISDIVIDABLE).setNodeValue(b ? "true" : "false");
  }

  static boolean isDividable(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return isDividable(vipsBlock.getParentNode());
    }
    if (vipsBlock.getAttributes().getNamedItem(ISDIVIDABLE) != null) {
      String value = vipsBlock.getAttributes().getNamedItem(ISDIVIDABLE).getNodeValue();
      return value.trim().equalsIgnoreCase("true");
    }
    return vipsBlock.getAttributes().getNamedItem(ISDIVIDABLE) != null;
  }

  // TOP RIGHT BOTTOM LEFT
  // border-width
  // margin
  // padding

  public static boolean isTextBox(Node vipsBlockChild) {
    return vipsBlockChild.getNodeName().equalsIgnoreCase("#text");
  }

  public static boolean isVisualBlock(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return isVisualBlock(vipsBlock.getParentNode());
    }
    if (vipsBlock.getAttributes().getNamedItem(ISVISUALBLOCK) != null) {
      String value = vipsBlock.getAttributes().getNamedItem(ISVISUALBLOCK).getNodeValue();
      return (value.trim().equalsIgnoreCase("true"));

    }

    return vipsBlock.getAttributes().getNamedItem(ISVISUALBLOCK) != null;
  }

//	static String COMPUTEDSTYLESHEET_ALL = "return Array.from(document.querySelectorAll('*')).map(element => {return {webElement: element, computedStyle: getComputedStyle(element)}});";

  public static Rectangle peelRectangleLayer(Rectangle rect, String toRemove) {
    String[] settings = toRemove.split(" ");

    int top = getNumerals(settings[0]);
    int right = getNumerals(settings[0]);
    int bottom = getNumerals(settings[0]);
    int left = getNumerals(settings[0]);

    if (settings.length == 2) {
      right = getNumerals(settings[1]);
      left = getNumerals(settings[1]);
    }

    if (settings.length == 3) {
      right = getNumerals(settings[1]);
      bottom = getNumerals(settings[2]);
      left = getNumerals(settings[1]);
    }
    if (settings.length == 4) {
      right = getNumerals(settings[1]);
      bottom = getNumerals(settings[2]);
      left = getNumerals(settings[3]);
    }

    Rectangle returnRect = new Rectangle(rect.x + left, rect.y + top, rect.width - (left + right),
        rect.height - (top + bottom));

    return returnRect;
  }

  private static int getNumerals(String string) {
    return Integer.parseInt(string.replaceAll("[^0-9]", ""));
  }

  public static Rectangle getContentRectangle(Node node, WebDriver driver) {
    String xpath = XPathHelper.getXPathExpression(node);
    WebElement vipsBlock = driver.findElement(By.xpath(xpath));

    //		((JavascriptExecutor)vips.driver).executeScript(VipsUtils.CONTENT_RECTANGLE_JAVASCRIPT_FUNCTION);

    String javascriptReturn = ((JavascriptExecutor) driver).executeScript(
        CONTENT_RECTANGLE_JAVASCRIPT_FUNCTION + CONTENT_RECTANGLE_RETURN, vipsBlock).toString();
    LOG.debug(javascriptReturn);

    String[] split = javascriptReturn.split(":");
    if (split.length == 5) {
      Rectangle rect = new Rectangle(Integer.parseInt(split[0].trim()),
          Integer.parseInt(split[1].trim()),
          Integer.parseInt(split[2].trim()), Integer.parseInt(split[3].trim()));

      int fontSize = Integer.parseInt(split[4].trim());
      setFontSize(node, fontSize);
      return rect;
    }

    org.openqa.selenium.Rectangle outer_return = vipsBlock.getRect();

    Rectangle outer = new Rectangle(outer_return.x, outer_return.y, outer_return.width,
        outer_return.height);
    LOG.debug("Outer : " + outer);

    String toRemove = vipsBlock.getCssValue("margin");
    outer = peelRectangleLayer(outer, toRemove);
    LOG.debug("peeling margin : " + outer);

    toRemove = vipsBlock.getCssValue("border-width");
    outer = peelRectangleLayer(outer, toRemove);
    LOG.debug("peeling border : " + outer);

    toRemove = vipsBlock.getCssValue("padding");
    outer = peelRectangleLayer(outer, toRemove);
    LOG.debug("peeling padding : " + outer);

    return outer;


  }

  public static Rectangle getRectangle(Node vipsBlock, WebDriver driver) {
    if (vipsBlock == null) {
      return null;
    }
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return getRectangle(vipsBlock.getParentNode(), driver);
    }
    if (vipsBlock.getAttributes().getNamedItem(RECTANGLE) != null) {
      Gson gson = new Gson();
      String rectString = vipsBlock.getAttributes().getNamedItem(RECTANGLE).getNodeValue();
      Rectangle rect = gson.fromJson(rectString, Rectangle.class);
//			LOG.debug(rect);
      return rect;
    }

    if (vipsBlock.getNodeName().equalsIgnoreCase("text")) {
      Rectangle rect = getRectangle(vipsBlock.getParentNode(), driver);
      List<Rectangle> siblingRects = new ArrayList<>();
      List<Node> siblings = getChildren(vipsBlock.getParentNode());
      int index = siblings.indexOf(vipsBlock);
      Rectangle beforeRect = null;
      Rectangle afterRect = null;
      if (index > 0) {
        if (!siblings.get(index - 1).getNodeName().equalsIgnoreCase("text")) {
          beforeRect = getRectangle(siblings.get(index - 1), driver);
        }
      }
      if (index < siblings.size() - 1) {
        if (!siblings.get(index + 1).getNodeName().equalsIgnoreCase("text")) {
          afterRect = getRectangle(siblings.get(index + 1), driver);
        }
      }
      Rectangle returnRect = getInBetweenRectangle(beforeRect, afterRect, rect);
      return returnRect;

    }
    String xpath = XPathHelper.getXPathExpression(vipsBlock);
    try {
      LOG.debug(vipsBlock.getNodeName());
//			WebElement element = vips.driver.findElement(By.xpath(xpath));
//			LOG.debug("padding : " + );\

//			org.openqa.selenium.Rectangle rect1 = element.getRect();
//			Rectangle rect = new Rectangle(rect1.x, rect1.y, rect1.width, rect1.height);
      Rectangle rect = getContentRectangle(vipsBlock, driver);
      setRectangle(vipsBlock, rect);
      return rect;
    } catch (Exception ex) {
//			ex.printStackTrace();
      Rectangle rect = new Rectangle(-1, -1, -1, -1);
      setRectangle(vipsBlock, rect);
      LOG.debug("Problem getting rectangle for element with xpath : " + xpath);
      return rect;
    }
  }

  private static Rectangle getInBetweenRectangle(Rectangle beforeRect, Rectangle afterRect,
      Rectangle rect) {
    if (beforeRect == null || afterRect == null) {
      return rect;
    }
//		if(beforeRect ==null) {
//			return getBeforeRect(rect, afterRect);
//		}
//		if(afterRect == null) {
//			return getAfterRect(rect, beforeRect);
//		}

    Rectangle returnRect = new Rectangle(-1, -1, -1, -1);
    if (beforeRect.x + beforeRect.width < afterRect.x) {
      returnRect.setLocation((beforeRect.x + beforeRect.width), (beforeRect.y));

      returnRect.setSize((afterRect.x - returnRect.x), (beforeRect.height));
    }

    if (beforeRect.y + beforeRect.height < afterRect.y) {
      returnRect.setLocation((beforeRect.x), (beforeRect.y + beforeRect.height));

      returnRect.setSize((beforeRect.width), (afterRect.y - returnRect.y));
    }
    return returnRect;
  }

  private static void setCssProperty(Node vipsBlock, String property, String propertyValue) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(property);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(property).setNodeValue(propertyValue);
  }

  public static void setFontSize(Node vipsBlock, int fontSize) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(FONTSIZE);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(FONTSIZE).setNodeValue(Integer.toString(fontSize));
  }

  public static int getFontSize(Node vipsBlock, WebDriver driver) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text") || vipsBlock.getNodeName()
        .equalsIgnoreCase("text")) {
      return getFontSize(vipsBlock.getParentNode(), driver);
    }
    if (vipsBlock.getAttributes().getNamedItem(FONTSIZE) != null) {

      String propertyValue = vipsBlock.getAttributes().getNamedItem(FONTSIZE).getNodeValue();

      return Integer.parseInt(propertyValue);
    }

    String xpath = XPathHelper.getXPathExpression(vipsBlock);
    WebElement element = driver.findElement(By.xpath(xpath));
    String propertyValue = element.getCssValue("fontSize");
    propertyValue = propertyValue.replaceAll("\\D+", "");
    int fontSize = Integer.parseInt(propertyValue);
    setFontSize(vipsBlock, fontSize);
    return fontSize;
  }

  private static String getCssProperty(Node vipsBlock, String property, WebDriver driver) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text") || vipsBlock.getNodeName()
        .equalsIgnoreCase("text")) {
      return getCssProperty(vipsBlock.getParentNode(), property, driver);
    }
    if (vipsBlock.getAttributes().getNamedItem(property.toLowerCase()) != null) {

      String propertyValue = vipsBlock.getAttributes().getNamedItem(property.toLowerCase())
          .getNodeValue();

      return propertyValue;
    }

    if (driver == null) {
      return "";
    }
    String xpath = XPathHelper.getXPathExpression(vipsBlock);
    WebElement element = driver.findElement(By.xpath(xpath));
    String propertyValue = element.getCssValue(property);
    setCssProperty(vipsBlock, property.toLowerCase(), propertyValue);
    return propertyValue;
  }

  public static void setImageCount(Node vipsBlock, int containImg) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(IMAGECOUNT);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(IMAGECOUNT).setNodeValue(Integer.toString(containImg));
  }

  public static int getImageCount(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return getImageCount(vipsBlock.getParentNode());
    }
    String value = vipsBlock.getAttributes().getNamedItem(IMAGECOUNT).getNodeValue();
    return Integer.parseInt(value);
  }

  public static void setPCount(Node vipsBlock, int containP) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(PCOUNT);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(PCOUNT).setNodeValue(Integer.toString(containP));
  }

  public static int getPCount(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return getPCount(vipsBlock.getParentNode());
    }
    String value = vipsBlock.getAttributes().getNamedItem(PCOUNT).getNodeValue();
    return Integer.parseInt(value);
  }

  public static int getTextLength(Node vipsBlock) {
    String text = vipsBlock.getTextContent().trim();
    return text.length();
  }

  public static void setLinkTextLength(Node vipsBlock, int linkTextLength) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(LINKTEXTLENGTH);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(LINKTEXTLENGTH)
        .setNodeValue(Integer.toString(linkTextLength));
  }

  public static int getLinkTextLength(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return getLinkTextLength(vipsBlock.getParentNode());
    }
    String value = vipsBlock.getAttributes().getNamedItem(LINKTEXTLENGTH).getNodeValue();
    return Integer.parseInt(value);
  }

  public static void setVipsLevel(Node vipsBlock, int level) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(VIPSLEVEL);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(VIPSLEVEL).setNodeValue(Integer.toString(level));
  }

  public static int getVipsLevel(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return getVipsLevel(vipsBlock.getParentNode());
    }
    if (isVipsBlock(vipsBlock)) {
      String value = vipsBlock.getAttributes().getNamedItem(VIPSLEVEL).getNodeValue();
      return Integer.parseInt(value);
    }
    return -1;
  }

  private static boolean isVipsBlock(Node vipsBlock) {
    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return isVipsBlock(vipsBlock.getParentNode());
    }

    if (vipsBlock.getAttributes().getNamedItem(VIPSLEVEL) != null) {
      return true;
    }
    return false;
  }

  public static Node getParentBox(List<Node> nestedBlocks) {
//		for(Node nestedBlock: nestedBlocks) {
//			LOG.debug(XPathHelper.getSkeletonXpath(nestedBlock));
//		}
//
    if (nestedBlocks == null) {
      return null;
    }
    if (nestedBlocks.isEmpty()) {
      return null;
    }

    Node tempBlock = nestedBlocks.get(0);
    while (tempBlock != tempBlock.getOwnerDocument().getDocumentElement()) {
      LOG.debug("Trying : " + XPathHelper.getSkeletonXpath(tempBlock));
      boolean allContains = true;
      for (Node block : nestedBlocks) {
        if ((tempBlock.compareDocumentPosition(block) & Document.DOCUMENT_POSITION_CONTAINED_BY)
            == 0) {
          if (!block.isSameNode(tempBlock)) {
            LOG.debug(XPathHelper.getSkeletonXpath(block));
            allContains = false;
            break;
          }
        }
      }

      if (allContains) {
        return tempBlock;
      }
      tempBlock = tempBlock.getParentNode();
    }

    return null;
  }

  public static Rectangle getIntersectionRectangle(Rectangle r1, Rectangle r2) {
    if (!isValidRectangle(r1) || !isValidRectangle(r2)) {
      return new Rectangle(0, 0, 0, 0);
    }
    ;

    Rectangle intersectionRect = null;

    int leftX = (int) Math.max(r1.getX(), r2.getX());
    int rightX = (int) Math.min(r1.getX() + r1.getWidth(), r2.getX() + r2.getWidth());
    int topY = (int) Math.max(r1.getY(), r2.getY());
    int bottomY = (int) Math.min(r1.getY() + r1.getHeight(), r2.getY() + r2.getHeight());

    if (leftX < rightX && topY < bottomY) {
      intersectionRect = new Rectangle(leftX, topY, rightX - leftX, bottomY - topY);
    } else {
      // Rectangles do not overlap, or overlap has an area of zero (edge/corner overlap)
      intersectionRect = new Rectangle(0, 0, 0, 0);
    }

    return intersectionRect;
  }

  public static Rectangle getUnionRectangle(Rectangle r1, Rectangle r2) {

    if (!isValidRectangle(r1) || !isValidRectangle(r2)) {
      return new Rectangle(0, 0, 0, 0);
    }

    Rectangle unionRect = null;

    int leftX = (int) Math.min(r1.getX(), r2.getX());
    int rightX = (int) Math.max(r1.getX() + r1.getWidth(), r2.getX() + r2.getWidth());
    int topY = (int) Math.min(r1.getY(), r2.getY());
    int bottomY = (int) Math.max(r1.getY() + r1.getHeight(), r2.getY() + r2.getHeight());

    if (leftX < rightX && topY < bottomY) {
      unionRect = new Rectangle(leftX, topY, rightX - leftX, bottomY - topY);
    } else {
      // Rectangles do not overlap, or overlap has an area of zero (edge/corner overlap)
      unionRect = new Rectangle(0, 0, 0, 0);
    }

    return unionRect;
  }

  public static boolean isValidRectangle(Rectangle rect) {
    if (rect.x < 0 || rect.y < 0 || rect.width <= 0 || rect.height <= 0) {
      return false;
    }

    return true;
  }

  public static void exportFragment(BufferedImage pageViewPort, File target, Rectangle rect) {
    try {
      BufferedImage subImage = pageViewPort.getSubimage(rect.x, rect.y, rect.width, rect.height);

      saveToImage(subImage, target);
    } catch (Exception ex) {
      LOG.error("Error exporting rectangle to image " + rect);
      LOG.debug(ex.getStackTrace().toString());
    }
  }

  public static void exportFragments(BufferedImage pageViewport, File target,
      List<VipsRectangle> vipsRectangles) {
    File targetFolder = target.getParentFile();
    if (targetFolder.isDirectory()) {
      File fragFolder = new File(targetFolder, FilenameUtils.getBaseName(target.getName()));
      fragFolder.mkdir();
      for (VipsRectangle rect : vipsRectangles) {
        File subImageTarget = new File(fragFolder, "" + rect.getId());
        exportFragment(pageViewport, subImageTarget, rect.getRect());
      }
    }
  }

  public static void saveToImage(BufferedImage image, File target) {
//		filename = System.getProperty("user.dir") + "/" + filename + ".png";
    try {
      ImageIO.write(image, "PNG", target);
//			ImageIO.write(image, "png", new File(filename));
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void setFontWeight(Node vipsBlock, int i) {
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(FONTWEIGHT);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(FONTWEIGHT).setNodeValue(Integer.toString(i));
  }

  public static String getFontWeight(Node vipsBlock, WebDriver driver) {
    return getCssProperty(vipsBlock, FONTWEIGHT, driver);
  }

  public static String getBgColor(Node vipsBlock, WebDriver driver) {
    return getCssProperty(vipsBlock, BGCOLOR, driver);
  }

  public static String getBackgroundColor(Node vipsBlock, WebDriver driver) {
    return getCssProperty(vipsBlock, BACKGROUNDCOLOR, driver);
  }

//	public static void setBgColor(Node vipsBlock, String bgColor) {
//		Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(BGCOLOR);
//		vipsBlock.getAttributes().setNamedItem(newAttribute);
//		vipsBlock.getAttributes().getNamedItem(BGCOLOR).setNodeValue(bgColor);
//	}

  /**
   * return xpaths of all children
   *
   * @param node
   * @return
   */
  public static List<String> getXpathList(Node node) {
    List<String> returnList = new ArrayList<String>();
    if (node.getNodeName().equalsIgnoreCase("#text")) {
      return returnList;
    }
    returnList.add(XPathHelper.getXPathExpression(node));
    List<Node> children = getChildren(node);
    for (Node child : children) {
      if (child.getNodeName().equalsIgnoreCase("#text")) {
        continue;
      }
      returnList.addAll(getXpathList(child));
    }
    return returnList;
  }

  public static boolean isPopulated(Document dom) {
    if (dom == null || dom.getDocumentElement() == null) {
      return false;
    }
    return dom.getDocumentElement().hasAttribute(POPULATED);
  }

  public static void setPopulated(Document dom) {
    if (dom == null) {
      return;
    }
    Node vipsBlock = dom.getDocumentElement();
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(POPULATED);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
  }

  public static void populateStyle(Document dom, WebDriver driver, boolean USE_CDP) {
    if (!useScript || isPopulated(dom)) {
      LOG.info("SKipping populate dom because already populated {}", isPopulated(dom));
      return;
    }
		/*File scriptFile = null;
		if(USE_CDP) {
			Path cdp = Paths.get("src", "main", "resources", "cdpScript.js");
			scriptFile = new File(cdp.toString());
		}
		else {
			Path vipsScript = Paths.get("src", "main", "resources", "vipsScript.js");
			scriptFile = new File(vipsScript.toString());
		}
		if(!scriptFile.exists()) {
			LOG.error("Could not find Vips Script at {}", scriptFile.getAbsolutePath());
			return;
		}
		String script="";
		try {
			script =  FileUtils.readFileToString(scriptFile);

		} catch (FileNotFoundException e) {
			LOG.error("Could not find Vips Script at {}", scriptFile.getAbsolutePath());
			LOG.error(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

    String script = null;

    if (USE_CDP) {
      script = Scripts.CDP_SCRIPT;
    } else {
      script = Scripts.VIPS_SCIRPT;
    }

    List<String> xpaths = getXpathList(dom.getElementsByTagName("body").item(0));
    LOG.info("Sending {} xpaths", xpaths.size());
    LOG.info("{}", xpaths);
    Object attributeString = null;
    if (USE_CDP) {
      Gson gson = new Gson();
      String xpathString = gson.toJson(xpaths);
      String executeScript = script + String.format(CDP_COMPUTEDSTYLESHEET_ALL, xpathString);
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("expression", executeScript);
      parameters.put("includeCommandLineAPI", Boolean.TRUE);
      parameters.put("returnByValue", Boolean.TRUE);
      attributeString = ((ChromeDriver) driver).executeCdpCommand("Runtime.evaluate", parameters);
      if (attributeString instanceof Map) {
        attributeString = ((Map) attributeString).get("result");
        if (attributeString instanceof Map) {
          attributeString = ((Map) attributeString).get("value");
        }
      }
      LOG.info("{}", attributeString);
    } else {
      Gson gson = new Gson();
      String xpathString = gson.toJson(xpaths);
      String executeScript = script + String.format(COMPUTEDSTYLESHEET_ALL, xpathString);
      attributeString = ((JavascriptExecutor) driver).executeScript(executeScript);
      LOG.info(attributeString.toString());
    }

//		Gson gson = new Gson();
//		List<Map<String, VipsBrowserAttributes>> attributes = gson.fromJson( attributeString, new TypeToken<List<Map<String, VipsBrowserAttributes>>>(){}.getType());
//		LOG.debug(attributes);

    Map<String, VipsBrowserAttributes> attributeMap = new HashMap<String, VipsBrowserAttributes>();

    if (attributeString instanceof Collection) {
      LOG.info("Found {} attribute objects", ((Collection) attributeString).size());
      for (Object elementSheet : (Collection) attributeString) {
        if (elementSheet instanceof Map) {
          String xpath = (String) ((Map) elementSheet).get("xpath");
          LOG.debug(xpath);
//					if(webElement instanceof WebElement) {
//						String bgColor = ((WebElement)webElement).getCssValue(BACKGROUNDCOLOR);
//						LOG.debug(bgColor);
//					}
          Object attributes = ((Map) elementSheet).get("attributes");
          LOG.debug("attributes{}", attributes);
          if (attributes instanceof Map) {
            if (((Map) attributes).isEmpty()) {
              LOG.debug("Empty attributes found for {}", xpath);
              continue;
            }
            Object rectangle = ((Map) attributes).get(RECTANGLE);
            Rectangle rect = null;
            if (rectangle instanceof Map) {
              rect = new Rectangle(
                  (int) (long) ((Map) rectangle).get("x"),
                  (int) (long) ((Map) rectangle).get("y"),
                  (int) (long) ((Map) rectangle).get("width"),
                  (int) (long) ((Map) rectangle).get("height")
              );
              LOG.debug(rect.toString());
            } else {
              rect = new Rectangle(-1, -1, -1, -1);
            }

            int fontSize = (int) (long) ((Map) attributes).get(FONTSIZE);
            int fontWeight = (int) (long) ((Map) attributes).get(FONTWEIGHT);
            String bgColor = (String) ((Map) attributes).get(BGCOLOR);
            boolean isDisplayed = (boolean) ((Map) attributes).get(ISDISPLAYED);
            String eventListeners = (String) ((Map) attributes).get("eventListeners");
            LOG.debug("rectangle {}", rectangle);
            LOG.debug("font size {}", fontSize);
            LOG.debug("font weight {}", fontWeight);
            LOG.debug("bg color {}", bgColor);
            attributeMap.put(xpath,
                new VipsBrowserAttributes(rect, fontSize, fontWeight, bgColor, isDisplayed,
                    eventListeners));
          }
        }
        LOG.debug(elementSheet.toString());
      }
    }

    for (String xpath : xpaths) {
      try {
        NodeList nodes = XPathHelper.evaluateXpathExpression(dom, xpath);
        if (nodes.getLength() == 1) {
          Node vipsBlock = nodes.item(0);
          // Apply attributes
          if (attributeMap.containsKey(xpath)) {
            setBrowserAttributes(vipsBlock, attributeMap.get(xpath));
          }
        }
      } catch (XPathExpressionException e) {
        LOG.error("Error while setting browser attribtues to document");
        LOG.error(e.getMessage());
      }
    }

    setPopulated(dom);
  }

  private static void setBrowserAttributes(Node vipsBlock,
      VipsBrowserAttributes browserAttributes) {
    setFontSize(vipsBlock, browserAttributes.getFontsize());
    setFontWeight(vipsBlock, browserAttributes.getFontweight());
    setCssProperty(vipsBlock, BGCOLOR, browserAttributes.getBgcolor());
    setCssProperty(vipsBlock, BACKGROUNDCOLOR, browserAttributes.bgcolor);
    boolean isDisplayed = browserAttributes.isDisplayed() && browserAttributes.rectangle != null &&
        browserAttributes.rectangle.height > 0 && browserAttributes.rectangle.width > 0;
    setIsDisplayed(vipsBlock, isDisplayed ? "true" : "false");
    setRectangle(vipsBlock, browserAttributes.rectangle);
    setEventListeners(vipsBlock, browserAttributes.eventListeners);
  }

  /**
   * Currently only has support for "click" events. JavaScript code for fetching eventhandlers is
   * available in {@see #com.crawljax.core.vips_selenium.Scripts}
   *
   * @param vipsBlock
   * @param hasEvent
   * @param event
   */
  public static void setEventListenerAttributes(Node vipsBlock, boolean hasEvent, String event) {
    if (!hasEvent) {
      Node newAttr = vipsBlock.getOwnerDocument().createAttribute(EVLIST);
      newAttr.setNodeValue("false");
      vipsBlock.getAttributes().setNamedItem(newAttr);
      return;
    }
    Node newAttr = vipsBlock.getOwnerDocument().createAttribute(EVLIST);
    newAttr.setNodeValue("true");
    vipsBlock.getAttributes().setNamedItem(newAttr);
    newAttr = vipsBlock.getOwnerDocument().createAttribute(EVLISTVAL);
		/*if(event!=null) {
			event = event.replace("\n", "");
			event = event.replace("\r", "");
			event = event.replace("\"", "'");
		}
		System.out.println(event);*/
    newAttr.setNodeValue(event);
    vipsBlock.getAttributes().setNamedItem(newAttr);
  }

  public static boolean hasEventListener(Node vipsBlock) {
    String evList = getVipsAttributeValue(vipsBlock, EVLIST);
    if (evList.equalsIgnoreCase("true")) {
      return true;
    } else {
      return false;
    }
  }

  public static String getEventListenerVal(Node vipsBlock) {
    return getVipsAttributeValue(vipsBlock, EVLISTVAL);
  }

  public static void setEventListeners(Node vipsBlock, String eventListeners) {
    if (eventListeners == null || eventListeners.trim().isEmpty()) {
      setEventListenerAttributes(vipsBlock, false, null);
    } else {
      setEventListenerAttributes(vipsBlock, true, eventListeners);
    }
  }

  public static boolean setFragParent(Node vipsBlock, int fragId) {
    if (vipsBlock == null || !vipsBlock.hasAttributes()) {
      return false;
    }

    if (vipsBlock.getAttributes().getNamedItem(FRAGPARENT) != null) {
      LOG.warn("Trying to reset the parent node for fragment {}", fragId);
      return false;
    }
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(FRAGPARENT);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(FRAGPARENT).setNodeValue(Integer.toString(fragId));
    return true;
  }

  public static int getFragParent(Node vipsBlock) {
    if (vipsBlock == null || !vipsBlock.hasAttributes()
        || vipsBlock.getAttributes().getNamedItem(FRAGPARENT) == null) {
//			LOG.info("Not a parent for any fragment");
      return -1;
    }

    if (vipsBlock.getAttributes().getNamedItem(FRAGPARENT) != null) {
      String propertyValue = vipsBlock.getAttributes().getNamedItem(FRAGPARENT).getNodeValue();
      return Integer.parseInt(propertyValue);
    }

    return -1;
  }

  public static boolean setDynamic(Node vipsBlock) {
    if (vipsBlock == null) {
      return false;
    }

    if (!vipsBlock.hasAttributes()) {
      if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
        return setDynamic(vipsBlock.getParentNode());
      }
    }

    if (vipsBlock.getAttributes().getNamedItem(DYNAMIC_FRAGMENT) != null) {
      // Already set
      return true;
    }

//		if(vipsBlock.getAttributes().getNamedItem(FRAGPARENT) == null) {
//			LOG.warn("Trying to set non parent as dynamic {}", vipsBlock);
//			return false;
//		}

    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(DYNAMIC_FRAGMENT);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    return true;
  }

  public static boolean isDynamic(Node vipsBlock) {
    return vipsBlock.getAttributes().getNamedItem(DYNAMIC_FRAGMENT) != null;
  }

  public static void removeVipsAttributes(Node node) {
    if (node == null || !node.hasAttributes() || node.getAttributes() == null) {
      // Skip attribute removal
    } else {
      for (String attribute : getVipsAttributes()) {
        try {
          node.getAttributes().removeNamedItem(attribute);
        } catch (Exception ex) {
          // Attribute doesn't exist
          LOG.debug("No such attribute {}", attribute);
        }
      }
    }

    if (node != null && node.getChildNodes().getLength() > 0) {
      for (Node child : getChildren(node)) {
        removeVipsAttributes(child);
      }
    }

  }

  public static String removeVipsAttributes(String dom) {
    Document doc = null;
    try {
      doc = DomUtils.asDocument(dom);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    if (doc == null) {
      return null;
    }
    removeVipsAttributes(doc.getDocumentElement());
    return DomUtils.getDocumentToString(doc);
  }

  public static void setCoverage(Node vipsBlock, AccessType access, Coverage coverage) {
    switch (access) {
      case direct:
        setCoverage(vipsBlock, coverage, DIRACCESS);
        break;
      case equivalent:
        setCoverage(vipsBlock, coverage, INDIRACCESS);
      case any:
        setCoverage(vipsBlock, coverage, ANYACCESS);
        break;
      case js:
        setCoverage(vipsBlock, coverage, JSOBSERVER);
        break;
      case none:
        break;
      default:
        break;
    }
  }

  public static AccessType getAccessType(Node vipsBlock) {

    if (getCoverage(vipsBlock, DIRACCESS) == Coverage.none) {
      if (getCoverage(vipsBlock, INDIRACCESS) == Coverage.none) {
        if (getCoverage(vipsBlock, JSOBSERVER) == Coverage.none) {
          return AccessType.none;
        } else {
          return AccessType.js;
        }
      } else {
        return AccessType.equivalent;
      }
    } else {
      return AccessType.direct;
    }
  }

  /*
   * Coverage computation
   */

  public static Coverage getCoverage(Node vipsBlock, AccessType access) {
    switch (access) {
      case direct:
        return getCoverage(vipsBlock, DIRACCESS);
      case equivalent:
        return getCoverage(vipsBlock, INDIRACCESS);
      case js:
        return getCoverage(vipsBlock, JSOBSERVER);
      case any:
        if (getCoverage(vipsBlock, DIRACCESS) == Coverage.none) {
          if (getCoverage(vipsBlock, INDIRACCESS) == Coverage.none) {
//						System.out.println(XPathHelper.getXPathExpression(vipsBlock) + "" + getCoverage());
            return getCoverage(vipsBlock, JSOBSERVER);
          } else {
            return getCoverage(vipsBlock, INDIRACCESS);
          }
        } else {
          return getCoverage(vipsBlock, DIRACCESS);
        }
      default:
        return Coverage.none;
    }
//		return getCoverage(vipsBlock, DIRACCESS);
//		return Coverage.none;
  }

  private static void setCoverage(Node vipsBlock, Coverage coverage, String access) {
    if (hasVipsAttribute(vipsBlock, access)) {
      switch (getCoverage(vipsBlock, access)) {
        case action:
          // best scenario already
          return;
        default:
          // set current coverage
          break;
      }
    }
    Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(access);
    vipsBlock.getAttributes().setNamedItem(newAttribute);
    vipsBlock.getAttributes().getNamedItem(access).setNodeValue(coverage.name());
  }

  private static Coverage getCoverage(Node vipsBlock, String access) {
    if (!hasVipsAttribute(vipsBlock, access)) {
      setCoverage(vipsBlock, Coverage.none, access);
      return Coverage.none;
    }
    String vipsAttr = getVipsAttributeValue(vipsBlock, access);

    return getCoverageTypeFromString(vipsAttr);

  }

  public static Coverage getCoverageTypeFromString(String vipsAttr) {
    if (vipsAttr == null) {
      return Coverage.none;
    }

    switch (vipsAttr) {
      case "action":
        return Coverage.action;
      case "assertion":
        return Coverage.assertion;
      case "find":
        return Coverage.find;
      case "implicit":
        return Coverage.implicit;
      case "characterData":
        return Coverage.characterData;
      case "subtree":
        return Coverage.subtree;
      case "childList":
        return Coverage.childList;
      case "attributes":
        return Coverage.attributes;
      default:
        return Coverage.none;
    }
  }

  public static boolean hasVipsAttribute(Node vipsBlock, String attr) {
    if (vipsBlock == null) {
      return false;
    }

    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return hasVipsAttribute(vipsBlock.getParentNode(), attr);
    }

    if (vipsBlock.getAttributes() != null) {
      return vipsBlock.getAttributes().getNamedItem(attr) != null;
    } else {
      return false;
    }
  }

  public static String getVipsAttributeValue(Node vipsBlock, String attr) {
    if (vipsBlock == null) {
      return null;
    }

    if (vipsBlock.getNodeName().equalsIgnoreCase("#text")) {
      return getVipsAttributeValue(vipsBlock.getParentNode(), attr);
    }

    if (vipsBlock.getAttributes() != null && vipsBlock.getAttributes().getNamedItem(attr) != null) {
      return vipsBlock.getAttributes().getNamedItem(attr).getNodeValue();
    } else {
      return null;
    }

  }

  public static enum Coverage {
    action, assertion, find, implicit, none, characterData, attributes, subtree, childList
  }

  public static enum AccessType {
    direct, equivalent, none, any, js
  }

//	public static void setAccessType(Node vipsBlock, AccessType accessType) {
//		if(hasVipsAttribute(vipsBlock, INDIRACCESS)) {
//			switch(getAccessType(vipsBlock)) {
//				case "direct":
//					// a direct access registered already
//					return;
//				default:
//					// set access
//					break;
//			}
//		}
//		Node newAttribute = vipsBlock.getOwnerDocument().createAttribute(INDIRACCESS);
//		vipsBlock.getAttributes().setNamedItem(newAttribute);
//		vipsBlock.getAttributes().getNamedItem(INDIRACCESS).setNodeValue(accessType.name());
//	}
//	
//	public static String getAccessType(Node vipsBlock) {
//		if(!hasVipsAttribute(vipsBlock, INDIRACCESS)) {
//			setAccessType(vipsBlock, AccessType.none);
//			return AccessType.none.name();
//		}
//		return getVipsAttributeValue(vipsBlock, INDIRACCESS);
//	}
}

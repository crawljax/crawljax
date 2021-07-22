package com.crawljax.stateabstractions.hybrid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.xerces.dom.TextImpl;
import org.openqa.selenium.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.Crawler;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.stateabstractions.visual.imagehashes.DHash;
import com.crawljax.util.DomUtils;
import com.crawljax.util.FSUtils;
import com.crawljax.util.XPathHelper;
import com.google.common.collect.Lists;

public class StructuralVisualStateVertexFactory extends StateVertexFactory {
	
	private static final Logger LOGGER = 
			LoggerFactory.getLogger(StructuralVisualStateVertexFactory.class.getName());
	
	private BufferedImage screenshot;	
	private DHash visualHashCalculator;
	
	private static final int THUMBNAIL_WIDTH = 200;
	private static final int THUMBNAIL_HEIGHT = 200;

	@Override
	public StateVertex newStateVertex(int id, String url, String name,  String dom, String strippedDom, EmbeddedBrowser browser) {
		File screenshotFile = new File(Crawler.outputDir.getAbsoluteFile() + "/screenshots/" + name + ".png");
		return newStateVertex(id, url, name, dom, strippedDom, browser, screenshotFile);
	}
	
	public StateVertexForElementsWithVisualInfo newStateVertex(int id, String url, String name,
			String dom, String strippedDom, EmbeddedBrowser browser, File screenShotFile) {
		return newStateVertex(id, url, name, dom, strippedDom, browser, screenShotFile, new DHash());
	}
	
	public StateVertexForElementsWithVisualInfo newStateVertex(int id, String url, String name,
			String dom, String strippedDom, EmbeddedBrowser browser, File screenShotFile, DHash visualHashCalculator) {
		this.visualHashCalculator = visualHashCalculator;
		this.screenshot = browser.getScreenShotAsBufferedImage(1000);
		saveImage(screenshot, screenShotFile, true);
		List<DOMElementWithVisualInfo> elementsVisualInfo = getElementsVisualInfo(browser, strippedDom);
		return new StateVertexForElementsWithVisualInfo(id, url, name, dom, strippedDom, elementsVisualInfo);
	}

	private List<DOMElementWithVisualInfo> getElementsVisualInfo(EmbeddedBrowser browser, String strippedDom) {
		List<DOMElementWithVisualInfo> domElements = Lists.newArrayList();
		try {
			Document document = DomUtils.asDocument(strippedDom);
			// I'm only looking at body elements, cause these will result to visual stuff
			populateDOMNodesWithVisualInfo(browser, domElements, document.getDocumentElement().getElementsByTagName("body").item(0));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return domElements;
	}

	private void populateDOMNodesWithVisualInfo(EmbeddedBrowser browser, List<DOMElementWithVisualInfo> domElements, Node node) {
		String xpath = XPathHelper.getXPathExpression(node);
		Rectangle boundingBox;
		DOMElementWithVisualInfo domElementWithWisualInfo;
		if (shouldDoVisualComaprison(node)) {
			String visualHash = null;
			if (node instanceof TextImpl) {
				// Text nodes are treated differently, their Visual Hash is their text
				// If their style changes, the parent node will catch it
				TextImpl textNode = (TextImpl) node;
				boundingBox = getTextNodeRectangle(browser, textNode);
				visualHash = textNode.getTextContent();
			} else {
				boundingBox = getNodeRectangle(browser, xpath);
				if (boundingBox.getWidth() == 0 || boundingBox.getHeight() == 0) {
					LOGGER.warn("Node {} is not visible. Take extra care about it. Dimensions are {}", 
							xpath, DOMElementWithVisualInfo.getBoundingBoxDimensionsString(boundingBox));
				} else {
					LOGGER.info("Taking screenshot from {}", xpath);
					BufferedImage elementScreenshot = getElementScreenshot(boundingBox);
					LOGGER.info("Took screenshot, now computing Visual Hash for {}", xpath);
					visualHash = visualHashCalculator.getDHash(elementScreenshot);
					LOGGER.info("Computed Visual Hash for {}", xpath);
					//File screenshotFile = new File(this.screenshotFile.getParentFile().getAbsolutePath() + "/" + xpath.replaceAll("[:/\\\\]", "_"));
					//LOGGER.info("Saving element's screensthot to {}", screenshotFile.getAbsolutePath());
					//saveImage(elementScreenshot, screenshotFile, false);
					//LOGGER.info("Saved screenshot");
				}
			}
			domElementWithWisualInfo = new DOMElementWithVisualInfo(xpath, boundingBox, visualHash);
		} else {
			boundingBox = getNodeRectangle(browser, xpath);
			domElementWithWisualInfo = new DOMElementWithVisualInfo(xpath, boundingBox);
		}
		domElements.add(domElementWithWisualInfo);

		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			populateDOMNodesWithVisualInfo(browser, domElements, childNodes.item(i));
		}
	}
	
	private boolean shouldDoVisualComaprison(Node node) {
		if (node.getChildNodes().getLength() == 0 ) { // Leaf node
			return true;
		} else {
			// Not leaf node, but there is a text node directly in it
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i) instanceof TextImpl) {
					return true;
				}
			}
		}
		return false;
	}

	private BufferedImage getElementScreenshot(Rectangle boundingBox) {
		BufferedImage screenshotImage = screenshot.getSubimage(boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight());
		return screenshotImage; 
	}
	
	private Rectangle getTextNodeRectangle(EmbeddedBrowser browser, TextImpl textNode) {
		// TODO This is not correct, should be fixed later. 
		// The bounding box of a text node is calculated in a completely different way
		return getNodeRectangle(browser, XPathHelper.getXPathExpression(textNode.getParentNode()));
	}

	private Rectangle getNodeRectangle(EmbeddedBrowser browser, String xpath) {
		/*
		 * It is necessary to get the bounding box from JS.
		 * The WebElement#getRect method has not been implemented in the 
		 * web drivers that I used:
		 * webDriver.findElement(By.xpath(PathHelper.getXPathExpression(node))).getRect(); // WON'T WORK
		 * The scrolling should be taken into account:
		 * https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect 
		 */
		Object executedJavaScript;
		String javascript = "return window.scrollX";
		executedJavaScript = browser.executeJavaScript(javascript);
		int windowScrollX = getIntValue(executedJavaScript);
		javascript = "return window.scrollY";
		executedJavaScript = browser.executeJavaScript(javascript);
		int windowScrollY = getIntValue(executedJavaScript);
		javascript = "return document.evaluate(\"%s\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.getBoundingClientRect();";
		javascript = String.format(javascript, xpath);
		executedJavaScript = browser.executeJavaScript(javascript);
		if (null != executedJavaScript && executedJavaScript instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) executedJavaScript;
			int x = getIntValue(map.get("left")) + windowScrollX;
			int y = getIntValue(map.get("top")) + windowScrollY;
			int width = getIntValue(map.get("width"));
			int height = getIntValue(map.get("height"));
			return new Rectangle(x, y, height, width);
		}
		return null;
	}

	private int getIntValue(Object object) {
		if (object instanceof Number) {
			Number n = (Number) object;
			return n.intValue();
		}
		return 0;
	}

	private static void saveImage(BufferedImage image, File screenshotFileToSave, boolean saveThumbnail) {
		LOGGER.debug("Saving screenshot for the current state to {}", screenshotFileToSave);
		try {
			String parentFolderPath = screenshotFileToSave.getParentFile().getAbsolutePath();
			FSUtils.directoryCheck(parentFolderPath);
			ImageIO.write(image, "PNG", screenshotFileToSave);
			if (saveThumbnail) {
				writeThumbNail(new File(parentFolderPath + "/" + screenshotFileToSave.getName() + "_small.jpg"), image);
			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private static void writeThumbNail(File target, BufferedImage screenshot) throws IOException {
		BufferedImage resizedImage =
		        new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(screenshot, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Color.WHITE, null);
		g.dispose();
		ImageIO.write(resizedImage, "JPEG", target);
	}

}

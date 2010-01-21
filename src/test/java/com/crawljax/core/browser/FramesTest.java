package com.crawljax.core.browser;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;

public class FramesTest {

	WebDriver driver = new FirefoxDriver();

	public void testFrames() throws SAXException, IOException {
		File index = new File("src/test/site/frame/index.html");

		driver.navigate().to("file://" + index.getAbsolutePath());

		driver.switchTo().window(driver.getWindowHandle());
		System.out.println("driver source: " + driver.getPageSource());

		Document doc = Helper.getDocument(driver.getPageSource());

		NodeList frameNodes = doc.getElementsByTagName("FRAME");

		for (int i = 0; i < frameNodes.getLength(); i++) {
			System.out.println("frame index: " + i);
			Element frame = (Element) frameNodes.item(i);
			String xpath = XPathHelper.getXpathExpression(frame);
			String frameStr = driver.switchTo().frame(i).getPageSource();

			System.out.println("frameStr before reload: " + frameStr);

			/*
			 * Element toAppend = (Element) (browser.frame(IEBrowser.symbols .get("xpath"),
			 * xpath).element()); toAppend = (Element) document.importNode(toAppend, true);
			 * frame.appendChild(toAppend); appendFrameContent(toAppend, document);
			 */
		}

		driver.close();

	}

	public void testIFrame() throws SAXException, IOException {
		File index = new File("src/test/site/iframe/index.html");

		driver.navigate().to("file://" + index.getAbsolutePath());

		// System.out.println("driver source1: " + driver.getPageSource());

		/*
		 * driver.switchTo().window(driver.getWindowHandle()); System.out.println("driver source2: "
		 * + driver.getPageSource());
		 */

		Document doc = Helper.getDocument(driver.getPageSource());

		NodeList frameNodes = doc.getElementsByTagName("IFRAME");

		for (int i = 0; i < frameNodes.getLength(); i++) {
			System.out.println("frame index: " + i);
			Element frame = (Element) frameNodes.item(i);
			String xpath = XPathHelper.getXpathExpression(frame);
			String frameStr = driver.switchTo().frame(i).getPageSource();

			System.out.println("frameStr before: " + frameStr);

			driver.navigate().refresh();
			System.out.println("frameStr after reload: " + frameStr);

			/*
			 * Element toAppend = (Element) (browser.frame(IEBrowser.symbols .get("xpath"),
			 * xpath).element()); toAppend = (Element) document.importNode(toAppend, true);
			 * frame.appendChild(toAppend); appendFrameContent(toAppend, document);
			 */
		}

		driver.close();

	}

	@Test
	public void testIFrameTemp() throws SAXException, IOException {
		File index = new File("src/test/site/iframe/index.html");

		driver.navigate().to("file://" + index.getAbsolutePath());

		// System.out.println("driver source1: " + driver.getPageSource());

		/*
		 * driver.switchTo().window(driver.getWindowHandle()); System.out.println("driver source2: "
		 * + driver.getPageSource());
		 */

		Document doc = Helper.getDocument(driver.getPageSource());

		driver = driver.switchTo().frame(0);
		driver.switchTo().window(driver.getWindowHandle());
		String frameStr = driver.getPageSource();
		System.out.println("frameStr from Index: " + frameStr);

		driver = driver.switchTo().frame(0);
		driver.switchTo().window(driver.getWindowHandle());
		frameStr = driver.getPageSource();
		System.out.println("frameStr from frame2: " + frameStr);

		driver.close();

	}

}

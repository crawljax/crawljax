package com.crawljax.clickabledetection;

import static com.crawljax.vips_selenium.VipsUtils.getXpathList;
import static com.crawljax.vips_selenium.VipsUtils.isPopulated;
import static com.crawljax.vips_selenium.VipsUtils.setEventListeners;
import static com.crawljax.vips_selenium.VipsUtils.setPopulated;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;
import com.crawljax.vips_selenium.XPathHelper;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Requirements: Chrome Browser - [BrowserConfiguration.BrowserType] Chrome Developer Tools enabled
 * -  Default BrowserProvider : [BrowserOptions.setUSE_CDP(true)] -  Custom BrowserProvider :
 * [WebDriverBackedEmbeddedBrowser.setUSE_CDP(true)]
 * <p>
 * For example usage, see ClickableDetectorExample in examples module
 */
public class ClickableDetectorPlugin implements OnNewStatePlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ClickableDetectorPlugin.class);
    public static final String CDP_SCRIPT = "function getEventHandlers(xpath){\n" + "	// a= $x(xpath)[0];\n"
            + "	result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);\n"
            + "	a = result.singleNodeValue;\n"
            + "	var returnMap = {};\n"
            + "	if(a==null){\n"
            + "		return returnMap;\n"
            + "	}\n"
            + "	if(getEventListeners(a)['click']){\n"
            + "		returnMap['eventListeners'] =  getEventListeners(a)['click'][0].listener.toString()\n"
            + "	} \n"
            + "\n"
            + "	return returnMap;\n"
            + "}";
    static final String CDP_COMPUTEDSTYLESHEET_ALL =
            "Array.from(%s).map(element => {return {xpath: element, attributes: getEventHandlers(element)}});";

    @Override
    public void onNewState(CrawlerContext context, StateVertex newState) {
        boolean isChrome = context.getConfig().getBrowserConfig().getBrowserType() == EmbeddedBrowser.BrowserType.CHROME
                || context.getConfig().getBrowserConfig().getBrowserType()
                        == EmbeddedBrowser.BrowserType.CHROME_HEADLESS;

        if (isChrome) {
            try {
                findClickables(context.getBrowser(), newState);
            } catch (IOException e) {
                LOG.error("Error extracting clickables from {}", newState.getName());
                LOG.debug(e.getMessage());
            }
        } else {
            throw new IllegalStateException("Cannot call ClickableDetector if the browser is not CHROME with CDP.\n"
                    + "Make sure BrowserType is CHROME and Please use BrowserOptions.setUSE_CDP(true) to enable CDP");
        }
    }

    public void findClickables(EmbeddedBrowser browser, StateVertex newState) throws IOException {
        Document dom = newState.getDocument();
        if (isPopulated(dom)) {
            LOG.info("Already populated dom. No need to run clickable detection again");
            return;
        }

        WebDriver driver = browser.getWebDriver();
        String script = CDP_SCRIPT;

        List<String> xpaths = getXpathList(dom.getElementsByTagName("body").item(0));
        LOG.info("Sending {} xpaths", xpaths.size());
        LOG.info("{}", xpaths);
        Object attributeString = null;

        Gson gson = new Gson();
        String xpathString = gson.toJson(xpaths);
        String executeScript = script + String.format(CDP_COMPUTEDSTYLESHEET_ALL, xpathString);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("expression", executeScript);
        parameters.put("includeCommandLineAPI", Boolean.TRUE);
        parameters.put("returnByValue", Boolean.TRUE);
        attributeString = ((ChromeDriver) driver).executeCdpCommand("Runtime.evaluate", parameters);
        if (attributeString instanceof Map) {
            attributeString = ((Map<?, ?>) attributeString).get("result");
            if (attributeString instanceof Map) {
                attributeString = ((Map<?, ?>) attributeString).get("value");
            }
        }
        LOG.info("{}", attributeString);

        Map<String, String> attributeMap = new HashMap<>();

        if (attributeString instanceof Collection) {
            LOG.info("Found {} attribute objects", ((Collection<?>) attributeString).size());
            for (Object elementSheet : (Collection) attributeString) {
                if (elementSheet instanceof Map) {
                    String xpath = (String) ((Map<?, ?>) elementSheet).get("xpath");
                    LOG.debug(xpath);

                    Object attributes = ((Map<?, ?>) elementSheet).get("attributes");
                    LOG.debug("attributes{}", attributes);
                    if (attributes instanceof Map) {
                        if (((Map<?, ?>) attributes).isEmpty()) {
                            LOG.debug("Empty attributes found for {}", xpath);
                            continue;
                        }

                        String eventListeners = (String) ((Map<?, ?>) attributes).get("eventListeners");

                        attributeMap.put(xpath, eventListeners);
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
                        setEventListeners(vipsBlock, attributeMap.get(xpath));
                    }
                }
            } catch (XPathExpressionException e) {
                LOG.error("Error while setting browser attributes to document");
                LOG.error(e.getMessage());
            }
        }

        setPopulated(dom);
        newState.setDocument(dom);
    }
}

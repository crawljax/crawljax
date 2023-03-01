package com.crawljax.stateabstractions.hybrid;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.fragmentation.FragmentationPlugin;
import com.crawljax.vips_selenium.VipsUtils;
import java.awt.image.BufferedImage;

/**
 * The default factory that creates State vertexes with a {@link Object#hashCode()} and
 * {@link Object#equals(Object)} function based on the Stripped dom.
 */
public class FragGenStateVertexFactory extends StateVertexFactory {

    private static double threshold = 0.0;
    private boolean visualData = false;

    public FragGenStateVertexFactory(double threshold, CrawljaxConfigurationBuilder builder, boolean visualData) {
        builder.addPlugin(new FragmentationPlugin());
        FragGenStateVertexFactory.threshold = threshold;
        this.visualData = visualData;
    }

    @Override
    public StateVertex newStateVertex(
            int id, String url, String name, String dom, String strippedDom, EmbeddedBrowser browser) {
        HybridStateVertexImpl newVertex =
                new HybridStateVertexImpl(id, url, name, dom, strippedDom, threshold, visualData);
        if (visualData && browser != null) {
            BufferedImage screenshot = browser.getScreenShotAsBufferedImage(500);
            newVertex.setImage(screenshot);
            VipsUtils.populateStyle(
                    newVertex.getDocument(),
                    browser.getWebDriver(),
                    ((WebDriverBackedEmbeddedBrowser) browser).isUSE_CDP());
        }
        return newVertex;
    }

    @Override
    public String toString() {
        return "Hybrid" + threshold;
    }
}

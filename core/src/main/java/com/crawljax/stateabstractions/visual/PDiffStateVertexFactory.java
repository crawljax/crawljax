package com.crawljax.stateabstractions.visual;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default factory that creates State vertexes with a {@link Object#hashCode()} and
 * {@link Object#equals(Object)} function based on the Stripped dom.
 */
public class PDiffStateVertexFactory extends StateVertexFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PDiffStateVertexFactory.class.getName());
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;
    private static double threshold = 0.0;

    static {
        OpenCVLoad.load();
    }

    public PDiffStateVertexFactory(double treshold) {
        threshold = treshold;
    }

    @Override
    public StateVertex newStateVertex(
            int id, String url, String name, String dom, String strippedDom, EmbeddedBrowser browser) {

        BufferedImage image = browser.getScreenShotAsBufferedImage(1000);

        return new PDiffStateVertexImpl(id, url, name, dom, strippedDom, image, threshold);
    }

    @Override
    public String toString() {
        return "VISUAL_PDiff_" + threshold;
    }
}

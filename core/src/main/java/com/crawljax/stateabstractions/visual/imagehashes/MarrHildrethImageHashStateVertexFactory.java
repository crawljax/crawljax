package com.crawljax.stateabstractions.visual.imagehashes;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.stateabstractions.visual.OpenCVLoad;
import java.awt.image.BufferedImage;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default factory that creates State vertexes with a {@link Object#hashCode()} and
 * {@link Object#equals(Object)} function based on the visual hash of the web page's screenshot.
 */
public class MarrHildrethImageHashStateVertexFactory extends StateVertexFactory {

    private static final Logger LOG = LoggerFactory.getLogger(MarrHildrethImageHashStateVertexFactory.class.getName());
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    static {
        OpenCVLoad.load();
    }

    private final MarrHildrethImageHash visHash = new MarrHildrethImageHash();

    @Override
    public StateVertex newStateVertex(
            int id, String url, String name, String dom, String strippedDom, EmbeddedBrowser browser) {

        BufferedImage image = browser.getScreenShotAsBufferedImage(1000);
        Mat hashMat = visHash.getHash(image);

        return new MarrHildrethImageHashStateVertexImpl(id, url, name, dom, strippedDom, visHash, hashMat);
    }

    @Override
    public String toString() {
        return this.visHash.getHashName();
    }

    public double getMarrHildrethImageHashMaxRaw() {
        return this.visHash.maxRaw;
    }

    public MarrHildrethImageHash getMarrHildrethImageHash() {
        return this.visHash;
    }
}

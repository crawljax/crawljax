package com.crawljax.stateabstractions.visual.imagehashes;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.stateabstractions.visual.OpenCVLoad;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default factory that creates State vertexes with a {@link Object#hashCode()} and
 * {@link Object#equals(Object)} function based on the Stripped dom.
 */
public class DHashStateVertexFactory extends StateVertexFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DHashStateVertexFactory.class.getName());
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    static {
        OpenCVLoad.load();
    }

    @Override
    public StateVertex newStateVertex(
            int id, String url, String name, String dom, String strippedDom, EmbeddedBrowser browser) {

        BufferedImage image = browser.getScreenShotAsBufferedImage(1000);

        DHash visualDHash = new DHash();
        String dHash = null;
        try {
            dHash = visualDHash.getDHash(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new DHashStateVertexImpl(id, url, name, dom, strippedDom, dHash);
    }
}

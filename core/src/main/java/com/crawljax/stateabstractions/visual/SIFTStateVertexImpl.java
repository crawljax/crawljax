package com.crawljax.stateabstractions.visual;

import com.crawljax.core.state.StateVertexImpl;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class SIFTStateVertexImpl extends StateVertexImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SIFTStateVertexImpl.class);

    private static final long serialVersionUID = 123400017983489L;
    double threshold = 95.0;
    final BufferedImage image;
    //	Mat image;

    /**
     * Creates a current state without an url and the stripped dom equals the dom.
     *
     * @param name the name of the state
     * @param dom  the current DOM tree of the browser
     */
    @VisibleForTesting
    SIFTStateVertexImpl(int id, String name, String dom, BufferedImage image) {
        this(id, null, name, dom, dom, image, -1);
    }

    /**
     * Defines a State.
     *
     * @param url         the current url of the state
     * @param name        the name of the state
     * @param dom         the current DOM tree of the browser
     * @param strippedDom the stripped dom by the OracleComparators
     * @param threshold
     */
    public SIFTStateVertexImpl(
            int id, String url, String name, String dom, String strippedDom, BufferedImage image, double threshold) {
        super(id, url, name, dom, strippedDom);
        this.image = image;
        if (threshold != -1) {
            this.threshold = threshold;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(image);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SIFTStateVertexImpl) {
            SIFTStateVertexImpl that = (SIFTStateVertexImpl) object;
            System.out.println(this.getId() + " : " + that.getId());
            if (this.getId() == that.getId()) {
                return true;
            }
            try {
                return SIFTComparator.computeDistance(this.image, that.getPage()) >= threshold;
            } catch (IOException e) {
                LOGGER.debug(e.getMessage());
                LOGGER.error("Error computing distance between {} and {}", getName(), that.getName());
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", super.getId())
                .add("name", super.getName())
                .add("Hist", image)
                .toString();
    }

    public BufferedImage getPage() {
        return image;
    }
}

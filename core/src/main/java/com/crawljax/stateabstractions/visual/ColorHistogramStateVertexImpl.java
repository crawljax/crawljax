package com.crawljax.stateabstractions.visual;

import com.crawljax.core.state.StateVertexImpl;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.opencv.core.Mat;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class ColorHistogramStateVertexImpl extends StateVertexImpl {

    private static final long serialVersionUID = 123400017983489L;

    final Mat hist;

    private double threshold = 0.0;

    /**
     * Creates a current state without an url and the stripped dom equals the dom.
     *
     * @param name the name of the state
     * @param dom  the current DOM tree of the browser
     */
    @VisibleForTesting
    ColorHistogramStateVertexImpl(int id, String name, String dom, Mat hist) {
        this(id, null, name, dom, dom, hist, -1);
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
    public ColorHistogramStateVertexImpl(
            int id, String url, String name, String dom, String strippedDom, Mat hist, double threshold) {
        super(id, url, name, dom, strippedDom);
        this.hist = hist;
        if (threshold != -1) {
            this.threshold = threshold;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hist);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ColorHistogramStateVertexImpl) {
            ColorHistogramStateVertexImpl that = (ColorHistogramStateVertexImpl) object;
            double distance = ColorHistogram.compare(this.hist, that.getColorHistogram());
            return ((distance >= 0) && (distance <= this.threshold));
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", super.getId())
                .add("name", super.getName())
                .add("Hist", hist)
                .toString();
    }

    public Mat getColorHistogram() {
        return hist;
    }
}

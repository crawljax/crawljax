package com.crawljax.plugins.crawloverview;

import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Factory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

class CandidateElementMatcher extends CustomMatcher<CandidateElementPosition> {

    private CandidateElementPosition actual;

    public CandidateElementMatcher(CandidateElementPosition actual) {
        super("A " + CandidateElementPosition.class.getName() + " with coordinates");
        this.actual = actual;
    }

    @Factory
    public static CandidateElementMatcher element(Point point, Dimension size) {
        CandidateElementPosition position = new CandidateElementPosition(null, point, size);
        return new CandidateElementMatcher(position);
    }

    @Override
    public boolean matches(Object item) {
        if (item instanceof CandidateElementPosition) {
            CandidateElementPosition element = (CandidateElementPosition) item;
            return element.getLeft() == actual.getLeft()
                    && element.getTop() == actual.getTop()
                    && element.getWidth() == actual.getWidth()
                    && element.getHeight() == actual.getHeight();
        } else {
            return false;
        }
    }
}

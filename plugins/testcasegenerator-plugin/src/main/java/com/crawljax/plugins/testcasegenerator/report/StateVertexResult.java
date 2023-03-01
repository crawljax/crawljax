package com.crawljax.plugins.testcasegenerator.report;

import com.crawljax.condition.invariant.Invariant;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.testcasegenerator.report.MethodResult.WarnLevel;
import java.util.List;

public class StateVertexResult {

    private final int id;
    private final String url;
    private String name;

    private boolean success;
    private boolean identical;
    private String compResult;
    private List<Invariant> failedInvariants;
    private WarnLevel warnLevel;
    private boolean locatorWarning;
    private int traceState;
    private double distance = -1.0;

    public StateVertexResult(StateVertex state) {
        this.id = state.getId();
        this.url = state.getUrl();
        this.setName(state.getName());
        this.success = true;
        this.identical = true;
        this.compResult = "DUPLICATE";
        this.warnLevel = WarnLevel.LEVEL0;
        this.locatorWarning = false;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTraceState() {
        return traceState;
    }

    public void setTraceState(int traceState) {
        this.traceState = traceState;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setFailedInvariants(List<Invariant> failedInvariants) {
        this.failedInvariants = failedInvariants;
    }

    public String getCompResult() {
        return compResult;
    }

    public void setCompResult(String compResult) {
        this.compResult = compResult;
    }

    public boolean isIdentical() {
        return identical;
    }

    public void setIdentical(boolean identical) {
        this.identical = identical;

        if (!identical) {
            this.compResult = "DIFFERENT";
        }
    }

    public int getStateVertex() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WarnLevel getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(WarnLevel level) {
        this.warnLevel = level;
    }

    public boolean getLocatorWarning() {
        return locatorWarning;
    }

    public void setLocatorWarning(boolean broken) {
        this.locatorWarning = broken;
    }
}

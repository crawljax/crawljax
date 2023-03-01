package com.crawljax.core.state;

import com.crawljax.core.CandidateElement;
import com.crawljax.fragmentation.Fragment;
import com.crawljax.util.DomUtils;
import com.crawljax.vips_selenium.VipsRectangle;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class StateVertexImpl implements StateVertex {

    protected static final Logger LOG = LoggerFactory.getLogger(StateVertexImpl.class);

    private static final long serialVersionUID = 123400017983488L;

    private final int id;
    private final String dom;
    private final String strippedDom;
    private final String url;
    private transient Document document;
    private final String name;

    private final transient HashMap<Node, List<CandidateElement>> nodeCandidateMapping = new HashMap<>();

    private transient ImmutableList<CandidateElement> candidateElements;

    private boolean isNearDuplicate;

    private int nearestState = -1;

    private double distToNearestState;

    private int cluster;

    private boolean unexploredActions = true;
    private boolean onURL;

    /**
     * Creates a current state without an url and the stripped dom equals the dom.
     *
     * @param name the name of the state
     * @param dom  the current DOM tree of the browser
     */
    @VisibleForTesting
    StateVertexImpl(int id, String name, String dom) {
        this(id, null, name, dom, dom);
    }

    /**
     * Defines a State.
     *
     * @param url         the current url of the state
     * @param name        the name of the state
     * @param dom         the current DOM tree of the browser
     * @param strippedDom the stripped dom by the OracleComparators
     */
    public StateVertexImpl(int id, String url, String name, String dom, String strippedDom) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.dom = dom;
        this.strippedDom = strippedDom;
        this.distToNearestState = -1;
        this.cluster = id;
        this.document = null;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDom() {
        return dom;
    }

    @Override
    public String getStrippedDom() {
        return strippedDom;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(strippedDom);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StateVertex) {
            StateVertex that = (StateVertex) object;
            return Objects.equal(this.strippedDom, that.getStrippedDom());
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Document getDocument() throws IOException {
        if (document != null) {
            return this.document;
        }
        return DomUtils.asDocument(this.strippedDom);
    }

    @Override
    public void setDocument(Document dom) {
        this.document = dom;
    }

    @Override
    public void setElementsFound(LinkedList<CandidateElement> elements) {
        this.candidateElements = ImmutableList.copyOf(elements);
        createNodeCandidateMapping();
    }

    private void createNodeCandidateMapping() {
        for (CandidateElement candidate : candidateElements) {
            if (!nodeCandidateMapping.containsKey(candidate.getElement())) {
                nodeCandidateMapping.put(candidate.getElement(), new ArrayList<>());
            }
            nodeCandidateMapping.get(candidate.getElement()).add(candidate);
        }
    }

    public List<CandidateElement> getCandidateElement(Node equivalentNode) {
        if (equivalentNode == null) {
            return null;
        }
        return nodeCandidateMapping.get(equivalentNode);
    }

    @Override
    public ImmutableList<CandidateElement> getCandidateElements() {
        return candidateElements;
    }

    @Override
    public boolean hasNearDuplicate() {
        return isNearDuplicate;
    }

    @Override
    public void setHasNearDuplicate(boolean b) {
        this.isNearDuplicate = b;
    }

    @Override
    public int getNearestState() {
        return this.nearestState;
    }

    @Override
    public void setNearestState(int vertex) {
        this.nearestState = vertex;
    }

    @Override
    public boolean inThreshold(StateVertex vertexOfGraph) {
        // Only implemented when there is a threshold for near duplicates
        return false;
    }

    @Override
    public double getDistToNearestState() {
        return distToNearestState;
    }

    @Override
    public void setDistToNearestState(double distToNearestState) {
        this.distToNearestState = distToNearestState;
    }

    @Override
    public double getDist(StateVertex vertexOfGraph) {
        // Return proper value when implemented
        return -1;
    }

    @Override
    public ArrayList<Fragment> getFragments() {
        return null;
    }

    @Override
    public void setFragments(ArrayList<Fragment> fragments) {}

    @Override
    public void addFragments(List<VipsRectangle> rectangles, WebDriver driver) {}

    public boolean isNearDuplicate() {
        return isNearDuplicate;
    }

    @Override
    public Fragment getRootFragment() {
        return null;
    }

    @Override
    public Fragment getClosestFragment(Node node) {
        return null;
    }

    @Override
    public Fragment getClosestFragment(CandidateElement element) {
        return null;
    }

    @Override
    public boolean hasUnexploredActions() {
        if (!this.unexploredActions) {
            return false;
        }
        for (CandidateElement element : candidateElements) {
            if (!element.wasExplored()) {
                return true;
            }
        }

        this.unexploredActions = false;
        return false;
    }

    @Override
    public CandidateElement getCandidateElement(Eventable event) {
        Identification id = event.getIdentification();
        Node equiv;
        try {
            equiv = DomUtils.getElementByXpath(getDocument(), id.getValue());
        } catch (XPathExpressionException | IOException e) {
            LOG.info("State {} has no Equivalent element for {}", this.getId(), id.getValue());
            return null;
        }

        if (getCandidateElement(equiv) != null && !getCandidateElement(equiv).isEmpty()) {
            return getCandidateElement(equiv).get(0);
        }

        return null;
    }

    @Override
    public int getCluster() {
        return cluster;
    }

    @Override
    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    @Override
    public Fragment getClosestDomFragment(CandidateElement element) {
        return null;
    }

    @Override
    public void setDirectAccess(CandidateElement element) {
        for (CandidateElement candidate : nodeCandidateMapping.get(element.getElement())) {
            candidate.setDirectAccess(true);
        }
    }

    @Override
    public boolean isOnURL() {
        return this.onURL;
    }

    @Override
    public void setOnURL(boolean onURL) {
        this.onURL = onURL;
    }
}

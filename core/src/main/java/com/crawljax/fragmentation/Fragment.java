package com.crawljax.fragmentation;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.state.StateVertex;
import com.crawljax.stateabstractions.dom.apted.costmodel.StringUnitCostModel;
import com.crawljax.stateabstractions.dom.apted.distance.APTED;
import com.crawljax.stateabstractions.dom.apted.node.AptedNode;
import com.crawljax.stateabstractions.dom.apted.node.StringNodeData;
import com.crawljax.stateabstractions.dom.apted.util.AptedUtils;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.crawljax.stateabstractions.visual.ColorHistogram;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.crawljax.vips_selenium.VipsUtils;
import com.crawljax.vips_selenium.VipsUtils.AccessType;
import com.crawljax.vips_selenium.VipsUtils.Coverage;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.opencv.core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(Fragment.class);
    // ID is local to the state this fragment belongs to.
    private int id;
    private Node fragmentParentNode = null;

    // Highest differentiator node. Makes sure no DOM nodes are excluded in fragment hierarchy.
    private Node hdn = null;
    private Rectangle rect;
    private final StateVertex referenceState;
    private final ArrayList<Fragment> equivalentFragments;
    private final ArrayList<Fragment> duplicateFragments;
    private final ArrayList<Fragment> nd2Fragments;
    private Boolean isUseful = null;
    private boolean accessTransferred = false;
    private int size = -1; // -2 if getting size is not possible the first time
    private boolean isGlobal;
    private final ArrayList<CandidateElement> candidates;
    private Mat chist = null;
    private Fragment parent;
    private Fragment domParent;
    private final ArrayList<Fragment> children;
    private List<Node> nestedBlocks;
    private boolean isDynamic;
    private Double candidateInfluence = null;
    private final List<Fragment> domChildren = new ArrayList<>();

    public Fragment(int id, List<Node> nestedBlocks, Rectangle rect, StateVertex referenceState) {
        this.id = id;
        this.nestedBlocks = nestedBlocks;
        this.rect = rect;
        this.referenceState = referenceState;
        this.equivalentFragments = new ArrayList<>();
        this.duplicateFragments = new ArrayList<>();
        this.nd2Fragments = new ArrayList<>();

        this.isGlobal = false;
        this.parent = null;
        this.children = new ArrayList<>();
        this.candidates = new ArrayList<>();
        this.isDynamic = false;
        this.domParent = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id2) {
        this.id = id2;
    }

    public int getSize() {
        if (size == -2) {
            return size;
        }
        if (size == -1) {
            try {
                size = getAllNodes().size();
            } catch (XPathExpressionException e) {
                LOG.error("Cannot find size of fragment {} - {}", referenceState.getName(), id);
                size = -2;
            }
        }
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isAccessTransferred() {
        return accessTransferred;
    }

    public void setAccessTransferred(boolean accessTransferred) {
        this.accessTransferred = accessTransferred;
    }

    public Boolean isUseful() {
        return isUseful;
    }

    public void setUseful(boolean isUseful) {
        this.isUseful = isUseful;
    }

    public ArrayList<Fragment> getNd2Fragments() {
        return nd2Fragments;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean isDynamic) {
        if (isDynamic && fragmentParentNode != null) {
            VipsUtils.setDynamic(fragmentParentNode);
        }
        this.isDynamic = isDynamic;
    }

    public Double getCandidateInfluence() {
        return candidateInfluence;
    }

    public void setCandidateInfluence(Double candidateInfluence) {
        this.candidateInfluence = candidateInfluence;
    }

    public List<Node> getNestedBlocks() {
        return nestedBlocks;
    }

    public void setNestedBlocks(List<Node> nestedBlocks) {
        this.nestedBlocks = nestedBlocks;
    }

    public Fragment getParent() {
        return parent;
    }

    public void setParent(Fragment parent) {
        this.parent = parent;
    }

    public void addChild(Fragment child) {
        if (!children.contains(child)) {
            this.children.add(child);
        }
    }

    public ArrayList<Fragment> getChildren() {
        return children;
    }

    public boolean compareImage(Fragment other) {
        double comp = ColorHistogram.compare(this.getChist(), other.getChist());
        return comp == 0.0;
    }

    public Mat getChist() {
        if (this.chist == null) {
            ColorHistogram colorHistogram = new ColorHistogram();
            BufferedImage thisImage = ((HybridStateVertexImpl) referenceState)
                    .getImage()
                    .getSubimage(this.rect.x, this.rect.y, this.rect.width, this.rect.height);
            this.chist = colorHistogram.getHistogram(thisImage);
        }
        return chist;
    }

    public void setChist(Mat chist) {
        this.chist = chist;
    }

    /**
     * Makes use of DOM tree size instead of invoking APTED differencing
     *
     * @param other
     * @return
     */
    public FragmentComparision compareFast(Fragment other) {

        if (this.getSize() < 0 || other.getSize() < 0) {
            LOG.debug("Cant do fast compare: unable to get fragment size {} {}", this, other);
        } else if (this.getSize() != other.getSize()) {
            return FragmentComparision.DIFFERENT;
        }
        return compare(other);
    }

    /**
     * Uses APTED for DOM Tree comparison and Histogram for Visual Comparison Does not check if the
     * other fragment being compared is Near-duplicate of category ND_struct Returns EQUAL if both DOM
     * and Histogram are exactly the same Return EQUIVALENT if DOM is the same but Histogram is not.
     * Returns DIFFERENT if DOM is different
     *
     * @param other
     * @return
     */
    public FragmentComparision compare(Fragment other) {
        boolean visualData = ((HybridStateVertexImpl) this.referenceState).isVisualData()
                && ((HybridStateVertexImpl) other.referenceState).isVisualData();
        AptedNode thisTree = AptedUtils.getAptedTree(this, visualData);
        AptedNode thatTree = AptedUtils.getAptedTree(other, visualData);
        APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());

        double structuralDistance = apted.computeEditDistance(thisTree, thatTree);
        if (structuralDistance != 0) {
            return FragmentComparision.DIFFERENT;
        }
        try {
            if (compareImage(other)) {
                return FragmentComparision.EQUAL;
            }
        } catch (Exception ex) {
            LOG.error(
                    "Error comparing Fragment images {} {} : {} {}",
                    this.id,
                    this.referenceState.getName(),
                    other.getId(),
                    other.getReferenceState().getName());
        }
        return FragmentComparision.EQUIVALENT;
    }

    public Node getFragmentParentNode() {
        return fragmentParentNode;
    }

    public void setFragmentParentNode(Node fragmentParentNode) {
        if (fragmentParentNode == null) {
            return;
        }
        boolean set = VipsUtils.setFragParent(fragmentParentNode, this.getId());
        if (!set) {
            LOG.error(
                    "node{} already a parent for fragment {}",
                    fragmentParentNode,
                    VipsUtils.getFragParent(fragmentParentNode));
        }
        this.fragmentParentNode = fragmentParentNode;
    }

    public Rectangle getRect() {
        return rect;
    }

    public void setRect(Rectangle rect) {
        this.rect = rect;
    }

    public StateVertex getReferenceState() {
        return referenceState;
    }

    public ArrayList<CandidateElement> getCandidates() {
        return candidates;
    }

    public void setCandidates(ArrayList<CandidateElement> candidates) {
        for (CandidateElement candidate : candidates) {
            addCandidateElement(candidate);
        }
    }

    public void addDuplicateFragment(Fragment fragment) {
        if (!this.duplicateFragments.contains(fragment)) {
            this.duplicateFragments.add(fragment);
        }
    }

    public void addEquivalentFragment(Fragment fragment) {
        if (!this.equivalentFragments.contains(fragment)) {
            this.equivalentFragments.add(fragment);
        }
    }

    public void addND2Fragment(Fragment fragment) {
        if (!this.nd2Fragments.contains(fragment)) {
            this.nd2Fragments.add(fragment);
        }
    }

    public void setIsGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    private boolean nestedBlockIncludesNode(Element element) {
        for (Node nestedBlock : nestedBlocks) {
            if (DomUtils.contains(nestedBlock, element)) {
                return true;
            }
        }
        return false;
    }

    public void addCandidateElement(CandidateElement element) {
        if (!this.candidates.contains(element)) {
            this.candidates.add(element);
        }
    }

    public boolean containsCandidate(CandidateElement element) {
        return this.candidates.contains(element);
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public ArrayList<Fragment> getDuplicateFragments() {
        return duplicateFragments;
    }

    public ArrayList<Fragment> getEquivalentFragments() {
        return equivalentFragments;
    }

    public List<CandidateElement> recordDuplicateCandidateAccess(
            CandidateElement element, Fragment duplicateFragment, List<CandidateElement> coveredCandidates) {
        List<CandidateElement> returnList = new ArrayList<>();
        for (CandidateElement coveredCandidate : coveredCandidates) {
            if (this.candidates.contains(coveredCandidate)) {
                returnList.add(coveredCandidate);
            }
        }
        try {
            List<CandidateElement> duplicates = getEquivalentCandidate(element, duplicateFragment);

            if (duplicates != null) {
                for (CandidateElement duplicate : duplicates) {
                    if (returnList.contains(duplicate)) {
                        continue;
                    }

                    duplicate.incrementDuplicateAccess();
                    returnList.add(duplicate);
                }
            }
        } catch (Exception ex) {
            LOG.error("Could not record duplicate access in :" + referenceState);
        }

        return returnList;
    }

    public List<CandidateElement> recordEquivalentCandidateAccess(
            CandidateElement element, Fragment duplicateFragment, List<CandidateElement> coveredCandidates) {
        List<CandidateElement> returnList = new ArrayList<>();
        for (CandidateElement coveredCandidate : coveredCandidates) {
            if (this.candidates.contains(coveredCandidate)) {
                returnList.add(coveredCandidate);
            }
        }

        try {
            List<CandidateElement> equivalents = getEquivalentCandidate(element, duplicateFragment);
            if (equivalents != null) {
                for (CandidateElement equivalent : equivalents) {
                    if (returnList.contains(equivalent)) {
                        continue;
                    }
                    equivalent.incrementEquivalentAccess();
                    returnList.add(equivalent);
                }
            }
            return returnList;
        } catch (Exception ex) {
            LOG.error("Could not record equivalent access " + referenceState);
        }
        return returnList;
    }

    private List<CandidateElement> getEquivalentCandidate(CandidateElement element, Fragment duplicateFragment) {
        try {
            if (duplicateFragment.fragmentParentNode == null) {
                Node tempDuplicateParent = VipsUtils.getParentBox(duplicateFragment.nestedBlocks);
                Node tempParent = VipsUtils.getParentBox(nestedBlocks);
                String relativeXpath =
                        XPathHelper.getXPathFromSpecificParent(element.getElement(), tempDuplicateParent);
                if (relativeXpath == null) {
                    if (DomUtils.contains(element.getElement(), tempDuplicateParent)) {
                        int hops = 0;
                        while (!tempDuplicateParent.equals(element.getElement())) {
                            hops = hops + 1;
                            tempDuplicateParent = tempDuplicateParent.getParentNode();
                        }
                        Node equivalentNode = tempParent;
                        for (int i = 0; i < hops; i++) {
                            equivalentNode = tempParent.getParentNode();
                        }
                        return referenceState.getCandidateElement(equivalentNode);
                    }
                    return null;
                } else {
                    Node equivalentNode = XPathHelper.getNodeFromSpecificParent(tempParent, relativeXpath);
                    return referenceState.getCandidateElement(equivalentNode);
                }
            }
            String relativeXpath = XPathHelper.getXPathFromSpecificParent(
                    element.getElement(), duplicateFragment.getFragmentParentNode());
            Node equivalentNode = XPathHelper.getNodeFromSpecificParent(fragmentParentNode, relativeXpath);
            return this.referenceState.getCandidateElement(equivalentNode);
        } catch (Exception ex) {
            LOG.error("Could not find equivalent candidate for : " + element + " in " + this.getReferenceState());
            LOG.debug(ex.getMessage());
        }
        return null;
    }

    public void transferEquivalentAccess(Fragment oldFragment) {
        for (CandidateElement element : oldFragment.getCandidates()) {
            List<CandidateElement> newElements = getEquivalentCandidate(element, oldFragment);
            if (newElements != null) {
                for (CandidateElement newElement : newElements) {
                    if (newElement.getEquivalentAccess() < element.getEquivalentAccess()) {
                        newElement.setEquivalentAccess(element.getEquivalentAccess());
                    }
                }
            }
        }
    }

    /**
     * For new fragments that have not been access transferred Called for every other duplicate
     * fragment ( to avoid candidate element not being all fragment problem)
     *
     * @param oldFragment
     */
    public void transferDuplicateAccess(Fragment oldFragment) {
        for (CandidateElement element : oldFragment.getCandidates()) {
            List<CandidateElement> newElements = getEquivalentCandidate(element, oldFragment);
            if (newElements != null) {
                for (CandidateElement newElement : newElements) {

                    if (newElement.getEquivalentAccess() < element.getEquivalentAccess()) {
                        newElement.setEquivalentAccess(element.getEquivalentAccess());
                    }

                    if (newElement.getDuplicateAccess() < element.getDuplicateAccess()) {
                        newElement.setDuplicateAccess(element.getDuplicateAccess());
                    }
                }
            }
        }
    }

    public boolean containsNode(Node node) {
        if (fragmentParentNode != null) {
            return DomUtils.contains(fragmentParentNode, node) || DomUtils.contains(hdn, node);
        } else {

            Rectangle rect = VipsUtils.getRectangle(node, null);
            if (rect.x < 0 || rect.y < 0 || rect.width <= 0 || rect.height <= 0) {
                // Invalid Rectangle so use nested blocks
                return nestedBlockIncludesNode((Element) node);
            }

            return containsRectangle(rect);
        }
    }

    private boolean containsRectangle(Rectangle rect2) {
        int x2 = rect2.x + rect2.width;
        int y2 = rect2.y + rect2.height;

        int oldx2 = rect.x + rect.width;
        int oldy2 = rect.y + rect.height;

        return (rect2.x >= rect.x) && (rect2.y >= rect.y) && (x2 <= oldx2) && (y2 <= oldy2);
    }

    public Fragment getDomParent() {
        return domParent;
    }

    public void setDomParent(Fragment domParent) {
        this.domParent = domParent;
    }

    public void addDomChildren(Fragment fragment) {
        if (!domChildren.contains(fragment)) {
            domChildren.add(fragment);
        }
    }

    public List<Fragment> getDomChildren() {
        return domChildren;
    }

    public void setEquivalentCoverage(
            Node equivalentNode, Fragment equivalentFrag, AccessType indirect, Coverage coverage) {
        Node node = getEquivalentNode(equivalentNode, equivalentFrag);
        //		VipsUtils.setAccessType(node, indirect);
        setCoverage(node, indirect, coverage);
    }

    /**
     * Uses relative Xpath to fetch node that is similar in the current fragment to the one provided
     * as argument
     *
     * @param element
     * @param duplicateFragment
     * @return
     */
    public Node getEquivalentNode(Node element, Fragment duplicateFragment) {
        try {
            if (duplicateFragment.fragmentParentNode == null) {
                Node tempDuplicateParent = VipsUtils.getParentBox(duplicateFragment.nestedBlocks);
                Node tempParent = VipsUtils.getParentBox(nestedBlocks);
                String relativeXpath = XPathHelper.getXPathFromSpecificParent(element, tempDuplicateParent);
                if (relativeXpath == null) {
                    if (DomUtils.contains(element, tempDuplicateParent)) {
                        int hops = 0;
                        while (!tempDuplicateParent.equals(element)) {
                            hops = hops + 1;
                            tempDuplicateParent = tempDuplicateParent.getParentNode();
                        }
                        Node equivalentNode = tempParent;
                        for (int i = 0; i < hops; i++) {
                            equivalentNode = tempParent.getParentNode();
                        }
                        return equivalentNode;
                    }
                    return null;
                } else {
                    Node equivalentNode = XPathHelper.getNodeFromSpecificParent(tempParent, relativeXpath);
                    return equivalentNode;
                }
            }
            String relativeXpath =
                    XPathHelper.getXPathFromSpecificParent(element, duplicateFragment.getFragmentParentNode());
            Node equivalentNode = XPathHelper.getNodeFromSpecificParent(fragmentParentNode, relativeXpath);
            return equivalentNode;
        } catch (Exception ex) {
            LOG.error("Could not find equivalent node for : " + XPathHelper.getSkeletonXpath(element) + " in "
                    + this.getReferenceState());
            LOG.debug(ex.getMessage());
        }
        return null;
    }

    public void setCoverage(Node node, AccessType access, Coverage coverage) {

        VipsUtils.setCoverage(node, access, coverage);
    }

    public void transferCoverage(Fragment oldFragment) {
        try {
            for (Node node : oldFragment.getAllNodes()) {
                setEquivalentCoverage(
                        node, oldFragment, AccessType.equivalent, VipsUtils.getCoverage(node, AccessType.direct));
                setEquivalentCoverage(
                        node, oldFragment, AccessType.equivalent, VipsUtils.getCoverage(node, AccessType.equivalent));
            }
        } catch (Exception ex) {
            LOG.error(
                    "Error transferring coverage for {}-{} from {}-{} ",
                    this.referenceState.getId(),
                    this.getId(),
                    oldFragment.getReferenceState().getId(),
                    oldFragment.getId());
            LOG.error("{}", ex.getMessage());
        }
    }

    private List<Node> getAllNodes() throws XPathExpressionException {
        List<Node> returnList = new ArrayList<>();
        if (fragmentParentNode != null) {
            NodeList subtreeNodes = DomUtils.getAllSubtreeNodes(fragmentParentNode);
            for (int i = 0; i < subtreeNodes.getLength(); i++) {
                returnList.add(subtreeNodes.item(i));
            }
        } else {
            for (Node parent : nestedBlocks) {
                NodeList subtreeNodes = DomUtils.getAllSubtreeNodes(parent);
                for (int i = 0; i < subtreeNodes.getLength(); i++) {
                    returnList.add(subtreeNodes.item(i));
                }
            }
        }
        return returnList;
    }

    public Node getHdn() {
        return hdn;
    }

    public void setHdn(Node hdn) {
        this.hdn = hdn;
    }

    public enum FragmentComparision {
        EQUAL,
        EQUIVALENT,
        DIFFERENT,
        ND2
    }
}

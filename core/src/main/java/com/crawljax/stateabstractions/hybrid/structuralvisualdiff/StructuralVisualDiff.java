package com.crawljax.stateabstractions.hybrid.structuralvisualdiff;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.openqa.selenium.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.crawljax.stateabstractions.hybrid.DOMElementWithVisualInfo;
import com.crawljax.stateabstractions.hybrid.StateVertexForElementsWithVisualInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import com.crawljax.stateabstractions.dom.apted.costmodel.StringUnitCostModel;
import com.crawljax.stateabstractions.dom.apted.distance.APTED;
import com.crawljax.stateabstractions.dom.apted.node.AptedNode;
import com.crawljax.stateabstractions.dom.apted.node.StringNodeData;

public class StructuralVisualDiff {
	
	/* Edit distance between the two DOMs. Is calculated based on the 
	 * diff operation costs in the APTED algorithm
	 * (currently StringUnitCostModel is used) */
	private float structuralDistance;
	
	/* Time taken for the APTED algorithm to calculate the diff */
	private long computationTime;
	
	/* Nodes mapped from the old document to the new one */
	private final Map<String, String> nodeMappings = Maps.newLinkedHashMap();
	
	/* Added nodes to the new document */
	private final Set<String> addedNodes = Sets.newHashSet();
	
	/* Removed nodes from the old document */
	private final Set<String> removedNodes = Sets.newHashSet();
	
	/* Moved nodes from the old document */
	private final Set<String> movedNodes = Sets.newHashSet();
	
	/* Modified elements (visually or textually) from the old document */
	private final Set<String> modifiedNodes = Sets.newHashSet();
	
	private final StateVertexForElementsWithVisualInfo oldVertex;
	private final StateVertexForElementsWithVisualInfo newVertex;
	
	private transient Document oldDocument;
	private transient Document newDocument;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StructuralVisualDiff.class);
	
	/**
	 * Returns a new instance of StructuralVisualDiff which includes the changes across the two given states
	 * @param oldVertex Old state. Should be of type {@link com.crawljax.visual.StateVertexForElementsWithVisualInfo} 
	 * since it relies on the visual information recorded in the previous run of the crawler
	 * @param newVertex New state to which we are comparing the old state
	 * @return
	 */
	public static StructuralVisualDiff calculate(StateVertexForElementsWithVisualInfo oldVertex, StateVertexForElementsWithVisualInfo newVertex) {
		return new StructuralVisualDiff(oldVertex, newVertex);
	}
	
	private StructuralVisualDiff(StateVertexForElementsWithVisualInfo oldVertex, StateVertexForElementsWithVisualInfo newVertex) {
		this.oldVertex = oldVertex;
		this.newVertex = newVertex;
		
		try {
			this.oldDocument = DomUtils.asDocument(oldVertex.getStrippedDom());
			this.newDocument = DomUtils.asDocument(newVertex.getStrippedDom());
			computeDiff();
		} catch (IOException e) {
			// IOException might happen from DomUtils#asDocument
			e.printStackTrace();
		}
	}
	
	public void computeDiff() {
		
		List<Node> postOrderOld = Lists.newArrayList();
		populatePostorder(postOrderOld, oldDocument);
		List<Node> postOrderNew = Lists.newArrayList();
		populatePostorder(postOrderNew, newDocument);
		
	    AptedNode<StringNodeData> t1 = getAPTEDTreeFromDocument(oldDocument);
 	    AptedNode<StringNodeData> t2 = getAPTEDTreeFromDocument(newDocument);
	    
	    APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
	    
	    LOGGER.debug("Started computing edit distance");
	    long startTime = System.nanoTime();
	    
	    structuralDistance = apted.computeEditDistance(t1, t2);
	    
	    long endTime = System.nanoTime();
	    computationTime = endTime - startTime;
	    LOGGER.debug("Done computing edit distance in {}", computationTime);
	    
	    /* Mapping is a list of 2-dimension arrays. 
	     * Each array corresponds to a mapping between the DOM nodes. 
	     * See the documentation for APTED#computeEditMapping()
	     * (I'm not sure whether this also takes time and should be 
	     * counted when we measure time)
	     */
	    LinkedList<int[]> mappings = (LinkedList<int[]>) apted.computeEditMapping();
	    for (int[] mapping : mappings) {
	    		if (mapping[0] == 0) {
	    			addedNodes.add(XPathHelper.getXPathExpression(postOrderNew.get(mapping[1] - 1)));
	    		} else if (mapping[1] == 0) {
	    			removedNodes.add(XPathHelper.getXPathExpression(postOrderOld.get(mapping[0] - 1)));
	    		} else {
	    			nodeMappings.put(XPathHelper.getXPathExpression(postOrderOld.get(mapping[0] - 1)), 
	    					XPathHelper.getXPathExpression(postOrderNew.get(mapping[1] - 1)));
	    		}
	    }
	    
	    detectNodeModifications();
	    
	}
	
	private void detectNodeModifications() {
		/*
		 * First, look at the mapped elements
		 * If the rectangles are not the same, the elements have been moved / resized.
		 * If the Visual Hashes are different then there could be a visual change.
		 * This relies on the assumption that the mappings are correct in the first place
		 * (which is not true necessarily)
		 */
		nodeMappings.forEach((oldNodeXPath, newNodeXPath) -> {
			DOMElementWithVisualInfo oldElement = getOldElementWithVisualInfo(oldNodeXPath);
			DOMElementWithVisualInfo newElement = getNewElementWithVisualInfo(newNodeXPath);
			if (null != oldElement && null != newElement) {
				Rectangle boundingBoxOld = oldElement.getBoundingBox();
				Rectangle boundingBoxNew = newElement.getBoundingBox();
				
				if (//The elements to be removed as moved should be visible before and after:
					boundingBoxOld.getWidth() != 0 &&
					boundingBoxOld.getHeight() != 0 &&
					boundingBoxNew.getWidth() != 0 &&
					boundingBoxNew.getHeight() != 0 &&
					// If the X's or Y's are different in this case, then the element has been moved:
					(
						boundingBoxOld.getX() != boundingBoxNew.getX() ||
						boundingBoxOld.getY() != boundingBoxNew.getY()
					)
				) {
					movedNodes.add(oldNodeXPath);
				}
				if (
					// If there are hashes for both mapped elements, yet the hashes are different
					(oldElement.getVisualHash() != null && newElement.getVisualHash() != null 
					 	&&!oldElement.getVisualHash().equals(newElement.getVisualHash())) ||
					// If the element did not have a visual hash but now it has:
					(oldElement.getVisualHash() == null && newElement.getVisualHash() != null) || 
					// If the element now has a visual hash but it didn't have before (e.g., it has become disappeared) :
					(oldElement.getVisualHash() != null && newElement.getVisualHash() == null) 
					
				) {
					modifiedNodes.add(oldNodeXPath);
				}
				// The visual hash changes in case of resized nodes. This can make a lot of FPs, which is not handled yet
			}
			
		});
		
		/*
		 * Look at the added / removed nodes
		 * If the elements look similar then maybe they have been moved
		 * For now we report them as moved
		 * P.S. I know, I could use the iterator to remove stuff as well.
		 */
		for (String addedNodeXPath : Lists.newArrayList(addedNodes)) {
			DOMElementWithVisualInfo newElementWithVisualInfo = getNewElementWithVisualInfo(addedNodeXPath);
			if (null != newElementWithVisualInfo && newElementWithVisualInfo.getVisualHash() != null) {
				String addedNodeVisualHash = newElementWithVisualInfo.getVisualHash();
				for (String removedNodeXPath : Lists.newArrayList(removedNodes)) {
					// We don't want to map a node two times
					if (movedNodes.contains(removedNodeXPath)) {
						continue;
					}
					DOMElementWithVisualInfo oldElementWithVisualInfo = getOldElementWithVisualInfo(removedNodeXPath);
					if (null != oldElementWithVisualInfo && oldElementWithVisualInfo.getVisualHash() != null) {
						String removedNodeVisualHash = oldElementWithVisualInfo.getVisualHash();
						if(addedNodeVisualHash.equals(removedNodeVisualHash)) {
							nodeMappings.put(addedNodeXPath, removedNodeXPath);
							movedNodes.add(removedNodeXPath);
							addedNodes.remove(addedNodeXPath);
							removedNodes.remove(removedNodeXPath);
						}
					}
				}
			}
		}
		
		/*
		 * Then, look at all the moved nodes, and remove all the child nodes which are reported to be moved 
		 * (This implementation is not the best)
		 * TODO: Maybe look at the remaining nodes, and see if they are all moved by the same amount (x and y wise)
		 * 		 Then report a region that contains all these elements
		 */
		for (String movedNodeXPath : Lists.newArrayList(movedNodes)) {
			if (movedNodes.contains(movedNodeXPath)) { // Necessary since we might have removed a node already!
				try {
					if (!movedNodeXPath.contains("#text")) { // Text nodes are not important, since they MUST have a parent that has moved.
						Element movedElement = DomUtils.getElementByXpath(oldDocument, movedNodeXPath);
						List<Node> childNodes = Lists.newArrayList();
						populatePostorder(childNodes, movedElement);
						childNodes.remove(childNodes.size() - 1); // Remove the last node from the postorder since it is the parent node!
						List<String> childNodesXPaths = Lists.newArrayList();
						boolean allChildsMoved = true;
						for (Node childNode : childNodes) {
							String childNodeXPath = XPathHelper.getXPathExpression(childNode);
							/*
							 * We want to only look at the elements that are shown.
							 * This skips, for instance, BR tags.
							 * We could hard-code BRs to be skipped,
							 * but I'm not sure whether there are other elements as well.
							 */
							 DOMElementWithVisualInfo oldElement = getOldElementWithVisualInfo(childNodeXPath);
							 if (null != oldElement && 
									 (oldElement.getBoundingBox().getWidth() > 0 &&
									  oldElement.getBoundingBox().getHeight() > 0)) {
								childNodesXPaths.add(childNodeXPath);
								if (!movedNodes.contains(childNodeXPath)) {
									/*
									 * If only one of the the children is not reported as moved, 
									 * We should keep the node and all the children
									 * (trying to see how this scenario can happen indeed)
									 */
									allChildsMoved = false;
									break;
								}
							 }
						}
						if (allChildsMoved) {
							// Keep the parent
							movedNodes.removeAll(childNodesXPaths);
						}
					}
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		/*
		 * TODO: I'm not sure how we should deal with nested modified elements.
		 */
	}

	private AptedNode<StringNodeData> getAPTEDTreeFromDocument(Node node) {
		AptedNode<StringNodeData> root = new AptedNode<StringNodeData>(new StringNodeData(getNodeStringRepresentation(node)));
		
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			root.addChild(getAPTEDTreeFromDocument(item));
		}
		return root;
	}
	
	private String getNodeStringRepresentation(Node node) {
		/*
		 * The APTED algorithm works on labeled trees.
		 * The question is, what is a good label for a node?
		 * For the moment, we just keep the node name,
		 * and assume that the algorithm is robust enough.
		 * This can be changed later is we find the assumption incorrect
		 */
		/*if (node instanceof TextImpl) {
			TextImpl textImpl = (TextImpl) node;
			return textImpl.getWholeText().replace("{", " <$< ").replace("}", " >$> ");
		} else {*/
			return node.getNodeName();
		/*}*/
	}
	
	private void populatePostorder(List<Node> postorderList, Node node) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			populatePostorder(postorderList, item);
		}
		postorderList.add(node);
	}
	
	/**
	 * @return The distance between the two DOM states
	 * based on the APTED algorithm and string cost model
	 */
	public float getStructuralDistance() {
		return this.structuralDistance;
	}
	
	/**
	 * @return Nodes mapped across the two DOMs
	 */
	public Map<String, String> getMappedNodes() {
		return Maps.newLinkedHashMap(nodeMappings);
	}
	
	/**
	 * @return Nodes that have been added to the new DOM
	 */
	public Set<String> getAddedNodes() {
		return Sets.newHashSet(addedNodes);
	}
	
	/**
	 * @return Nodes that have been removed from the old DOM
	 */
	public Set<String> getRemovedNodes() {
		return Sets.newHashSet(removedNodes);
	}
	
	/**
	 * @return Nodes (from the old state) that are mapped 
	 * across the two states but are moved
	 */
	public Set<String> getMovedNodes() {
		return Sets.newHashSet(movedNodes);
	}
	
	/**
	 * @return Nodes that are modified (visually or textually) from the old state
	 */
	public Set<String> getModifiedNodes() {
		return Sets.newHashSet(modifiedNodes);
	}
	
	/**
	 * @return Time took to compute the edit distance in the APTED algorithm
	 */
	public long getComputationTime() {
		return this.computationTime;
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("Added nodes: ").append(getAddedNodes()).append(System.lineSeparator());
		toReturn.append("Removed nodes: ").append(getRemovedNodes()).append(System.lineSeparator());
		toReturn.append("Modified nodes: ").append(modifiedNodes).append(System.lineSeparator());
		toReturn.append("Moved nodes: ").append(getMovedNodes()).append(System.lineSeparator());
		toReturn.append("Mappings: ").append(getMappedNodes());
		return toReturn.toString();
	}

	/**
	 * Tells whether two states are identical after performing the analysis.
	 * Three things are checked: (1) there is no modified node (visually or textually),
	 * (2) there is no moved node, and (3) the distance between the two DOMs' structures is zero
	 * (i.e., there is no added/removed nodes and all nodes are mapped without any differences in tag names) 
	 * @return True if the states are identical
	 */
	public boolean statesAreIdentical() {
		return getModifiedNodes().size() == 0 &&
				getMovedNodes().size() == 0 &&
				getStructuralDistance() == 0;
	}
	
	/**
	 * Given an XPath for a node, this method returns {@code DOMElementWithVisualInfo} of the node in the old document.
	 * The XPath should be from the old document.
	 * @param XPath of the node
	 * @return The node of type {@code DOMElementWithVisualInfo}. {@code null} if the node does not exist.
	 */
	public DOMElementWithVisualInfo getOldElementWithVisualInfo(String xpath) {
		return oldVertex.getElementWithVisualInfo(xpath);
	}
	
	/**
	 * Given an XPath for a node, this method returns {@code DOMElementWithVisualInfo} of the node in the new document.
	 * The XPath should be from the new document.
	 * @param XPath of the node
	 * @return The node of type {@code DOMElementWithVisualInfo}. {@code null} if the node does not exist.
	 */
	public DOMElementWithVisualInfo getNewElementWithVisualInfo(String xpath) {
		return newVertex.getElementWithVisualInfo(xpath);
	}
	
	public void serializeDiff(String pathToJSONFile) throws IOException {
		FileOutputStream file = new FileOutputStream(pathToJSONFile);
		file.write((new Gson().toJson(this).getBytes()));
		file.close();
	}

}

package com.crawljax.stateabstractions.hybrid;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.openqa.selenium.WebDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.fragmentation.Fragment;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.fragmentation.Fragment.FragmentComparision;
import com.crawljax.stateabstractions.dom.apted.costmodel.StringUnitCostModel;
import com.crawljax.stateabstractions.dom.apted.distance.APTED;
import com.crawljax.stateabstractions.dom.apted.node.AptedNode;
import com.crawljax.stateabstractions.dom.apted.node.StringNodeData;
import com.crawljax.stateabstractions.dom.apted.util.AptedUtils;
import com.crawljax.stateabstractions.visual.OpenCVLoad;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.crawljax.vips_selenium.VipsRectangle;
import com.crawljax.vips_selenium.VipsSelenium;
import com.crawljax.vips_selenium.VipsUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * The state vertex class which represents a state in the browser. When iterating over the possible
 * candidate elements every time a candidate is returned its removed from the list so it is a one
 * time only access to the candidates.
 */
public class HybridStateVertexImpl extends StateVertexImpl{
	static {
		OpenCVLoad.load();
	}
	private static final long serialVersionUID = 123400017983489L;

	public static boolean FAST_COMPARE = false;

	private double threshold = 0.0;
	transient private Document fragmentedDom = null;
	
	transient private BufferedImage image = null;
	
	private boolean fragmented = false;
	
	private boolean visualData = false;

	private int size = -1;

	public boolean isVisualData() {
		return visualData;
	}

	public void setVisualData(boolean visualData) {
		this.visualData = visualData;
	}

	/**
	 * Defines a State.
	 *
	 * @param id          id of the state in the SFG
	 * @param url         the current url of the state
	 * @param name        the name of the state
	 * @param dom         the current DOM tree of the browser
	 * @param strippedDom the stripped dom by the OracleComparators
	 * @param threshold   the threshold to be used
	 * @param visualData 
	 */
	public HybridStateVertexImpl(int id, String url, String name, String dom, String strippedDom,
			double threshold, boolean visualData) {
		super(id, url, name, dom, strippedDom);
		long start = System.currentTimeMillis();
		this.threshold = threshold;
		this.visualData = visualData;
		try {
			this.fragmentedDom = DomUtils.asDocument(strippedDom);
			boolean offline = false;
			VipsUtils.cleanDom(fragmentedDom, offline);
		} catch (IOException e) {
			System.out.println("Error creating document : state " +  id);
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		LOG.info("Took {} ms to parse DOM", end-start);
	}

	public boolean isFragmented() {
		return fragmented;
	}
	
	public Document loadFragmentDom(Document dom, BufferedImage screenshot) {
		this.fragmentedDom = dom;
		boolean offline = true;
		VipsUtils.cleanDom(fragmentedDom, offline);
		VipsSelenium vips = new VipsSelenium(null, this.fragmentedDom, screenshot, 10, null, this.getName(), false);
//		VipsSeleniumParser parser = new VipsSeleniumParser(vips);
		List<VipsRectangle> rectangles = vips.startSegmentation();
		fragmented = true;
		this.image = screenshot;
		this.addFragments(rectangles, null);
		return fragmentedDom;
	}
	
	public Document fragmentDom(WebDriver driver, BufferedImage screenshot, File screenshotFile) {
		if(!fragmented) {
			VipsSelenium vips = new VipsSelenium(driver,this.fragmentedDom, screenshot, 10, screenshotFile, this.getName(), true);
//			VipsSeleniumParser parser = new VipsSeleniumParser(vips);
			List<VipsRectangle> rectangles = vips.startSegmentation();
			fragmented = true;
			this.image = screenshot;
			this.addFragments(rectangles, driver);
		}
		
		return fragmentedDom;
	}
	
	@Override
	public Document getDocument() {
		return fragmentedDom;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.getStrippedDom());
	}

	public static boolean computeDistanceUsingChangedNodes(Document doc1, Document doc2, boolean visualData) {
		List<List<Node>> changedNodes  = getChangedNodes(doc1, doc2, visualData);
		List<Node> doc1Changed = changedNodes.get(0);
		List<Node> doc2Changed = changedNodes.get(1);
		boolean allHidden = true;
		for(Node changed : doc1Changed) {
			if(VipsUtils.isDisplayed(changed, null)) {
				allHidden = false;
				break;
			}
		}
		
		for(Node changed : doc2Changed) {
			if(VipsUtils.isDisplayed(changed, null)) {
				allHidden = false;
				break;
			}
		}
		
		return allHidden;
	}
	
	public static double computeDistance(Document doc1, Document doc2, boolean visualData) {
		AptedNode<StringNodeData> aptedDoc1 = AptedUtils.getAptedTree(doc1, visualData);
//		System.out.println(aptedDoc1);
		AptedNode<StringNodeData> aptedDoc2 = AptedUtils.getAptedTree(doc2, visualData);
//		System.out.println(aptedDoc2);
		APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
		 
		double structuralDistance = apted.computeEditDistance(aptedDoc1, aptedDoc2);
//		System.out.println(getChangedNodes(doc1, doc2));
		return structuralDistance; 
	}
	
	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	// Doc1 is the new state
	// Doc2 is the old state
	public static List<List<Node>> getChangedNodes(Document doc1, Document doc2, boolean visualData){
		
		List<Node> postOrder1 = Lists.newArrayList();
		populatePostorder(postOrder1, doc1.getElementsByTagName("body").item(0));
		
		List<Node> postOrder2 = Lists.newArrayList();
		populatePostorder(postOrder2, doc2.getElementsByTagName("body").item(0));
		
		List<Node> doc1Nodes = new LinkedList<>();
		List<Node> doc2Nodes = new LinkedList<>();
		Map<Node, Node> nodeMappings = Maps.newLinkedHashMap();
		
		
		AptedNode<StringNodeData> aptedDoc1 = AptedUtils.getAptedTree(doc1, visualData);
		
		AptedNode<StringNodeData> aptedDoc2 = AptedUtils.getAptedTree(doc2, visualData);
		APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
		double structuralDistance = apted.computeEditDistance(aptedDoc1, aptedDoc2);
		
		
		LinkedList<int[]> mappings = (LinkedList<int[]>) apted.computeEditMapping();
	    for (int[] mapping : mappings) {
	    		if (mapping[1] == 0) {
	    			
					doc1Nodes.add(postOrder1.get(mapping[0] - 1));
	    		} else if (mapping[0] == 0) {
	    			doc2Nodes.add(postOrder2.get(mapping[1] - 1));
	    		} else {
	    			Node oldNode = postOrder2.get(mapping[1] - 1);
	    			Node newNode = postOrder1.get(mapping[0] - 1);
	    			String oldS = AptedUtils.getNodeStringRepresentation(oldNode, visualData);
	    			String newS = AptedUtils.getNodeStringRepresentation(newNode, visualData);
	    			if(!oldS.equalsIgnoreCase(newS)){
	    				nodeMappings.put(oldNode, newNode);
	    				doc2Nodes.add(oldNode);
	    				doc1Nodes.add(newNode);
	    			}
	    		}
	    }
//	    String[] a = {"",""};
	    
	    List<List<Node>> changedNodes = new ArrayList<List<Node>>();
	    changedNodes.add(doc1Nodes);
	    changedNodes.add(doc2Nodes);

	    return changedNodes;
	}
	
	private static void populatePostorder(List<Node> postorderList, Node node) {
		if(node == null) {
			return;
		}
		ArrayList<Node> children = VipsUtils.getChildren(node);

		if(node.getNodeName().equalsIgnoreCase("select")) {
			if(children.size()>0) {
				postorderList.add(children.get(0));
			}
			postorderList.add(node);
			return;
		}
		
		for (Node item: children) {
			populatePostorder(postorderList, item);
		}
		postorderList.add(node);
	}
	
	public List<List<Node>> getDifference(StateVertex other){
		try {
			return getChangedNodes(this.fragmentedDom, other.getDocument(), visualData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Use nearduplicate state to find dynamic fragments. 
	 * Can be used during state revisit as well.
	 * @param ndState
	 * @return
	 */
	public List<Fragment> assignDynamicFragments(StateVertex ndState) {
		if(!(ndState instanceof HybridStateVertexImpl)){	
			return new ArrayList<>();
		}
		List<Fragment> dynamicFragments = new ArrayList<Fragment>();
		try {
			List<Node> diffNodes = getDiffNodes(this.getDocument(), ((HybridStateVertexImpl)ndState).getDocument(), visualData);
			//		List<Node> diffNodes = FragmentManager.getDomDiffNodes(this, (HybridStateVertexImpl)ndState);
			LOG.debug("No of diff nodes found {}", diffNodes.size());
			for(Node node: diffNodes) {
				if(node==null)
					continue;
				LOG.debug("Diff node {}", XPathHelper.getXPathExpression(node));
				
				VipsUtils.setDynamic(node);
				Fragment fragment = getClosestFragment(node);
				LOG.debug("Closest fragment {} with xpath {}", fragment.getId(), XPathHelper.getXPathExpression(fragment.getFragmentParentNode()));
				if(fragment!=null) {
					if(!dynamicFragments.contains(fragment)) {
						dynamicFragments.add(fragment);
						fragment.setDynamic(true);
					}
				}
			}
			LOG.debug("No of dynamic fragments found {}", dynamicFragments.size());
		}catch(Exception ex) {
			LOG.error("Error assigning dynamic fragments {}", ex.getMessage());
		}
		return dynamicFragments;
	}

	/**
	 * Returns nodes of doc1 which are mapped to doc2 but have different tag or text value
	 * @param doc1
	 * @param doc2
	 * @param visualData 
	 * @return
	 */
	public static List<Node> getDiffNodes(Document doc1, Document doc2, boolean visualData) {		
		List<Node> postOrder1 = Lists.newArrayList();
		populatePostorder(postOrder1, doc1.getElementsByTagName("body").item(0));
		
		List<Node> postOrder2 = Lists.newArrayList();
		populatePostorder(postOrder2, doc2.getElementsByTagName("body").item(0));
		
		List<Node> doc1Nodes = new LinkedList<>();
		List<Node> doc2Nodes = new LinkedList<>();
		Map<Node, Node> nodeMappings = Maps.newLinkedHashMap();
		
		
		AptedNode<StringNodeData> aptedDoc1 = AptedUtils.getAptedTree(doc1, visualData);
		
		AptedNode<StringNodeData> aptedDoc2 = AptedUtils.getAptedTree(doc2, visualData);
		APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
		double structuralDistance = apted.computeEditDistance(aptedDoc1, aptedDoc2);
		
		
		LinkedList<int[]> mappings = (LinkedList<int[]>) apted.computeEditMapping();
	    for (int[] mapping : mappings) {
	    		if (mapping[1] == 0) {
	    			
//					doc1Nodes.add(postOrder1.get(mapping[0] - 1));
	    		} else if (mapping[0] == 0) {
//	    			doc2Nodes.add(postOrder2.get(mapping[1] - 1));
	    		} else {
	    			Node doc2Node = postOrder2.get(mapping[1] - 1);
	    			Node doc1Node = postOrder1.get(mapping[0] - 1);
	    			String doc2Tag = AptedUtils.getNodeStringRepresentation(doc2Node, visualData);
	    			String doc1Tag = AptedUtils.getNodeStringRepresentation(doc1Node, visualData);
	    			if(!doc2Tag.equalsIgnoreCase(doc1Tag)){
	    				doc2Nodes.add(doc2Node);
	    				doc1Nodes.add(doc1Node);
	    			}
	    			else {
	    				if(doc1Tag.equalsIgnoreCase("#text") && doc2Tag.equalsIgnoreCase("#text")) {
	    					if(!doc1Node.getTextContent().trim().equalsIgnoreCase(doc2Node.getTextContent().trim())) {
	    						doc1Nodes.add(doc1Node);
	    						doc2Nodes.add(doc2Node);
	    					}
	    				}
	    			}
	    			
    				nodeMappings.put(doc2Node, doc1Node);
	    		}
	    }
	    return doc1Nodes;
	}
	
	

	private int getSize() {
		if(size == -2) {
			return size;
		}
		if(size == -1) {
			try {
				size = DomUtils.getAllSubtreeNodes(getDocument().getElementsByTagName("body").item(0)).getLength();
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				size = -2;
			}
		}
		return size;
	}

	


	@Override
	public boolean equals(Object object) {
		HybridStateVertexImpl that = (HybridStateVertexImpl) object;
		if(this.getId() == that.getId()) {
			return true;
		}
		if(FAST_COMPARE) {
			if(this.getSize() < 0 || that.getSize() < 0) {
				LOG.error("Cant do fast compare: unable to get state size {} {}", this, that);
			}
			else if(this.getSize() != that.getSize()) {
				return false;
			}
		}
		try {
//			if(visualData) {
//				return computeDistanceUsingChangedNodes(this.getDocument(), that.getDocument(), visualData);
//			}
			double distance = computeDistance(this.getDocument(), that.getDocument(), visualData);
//			LOG.info("Distance  between {} {} is {}", this.getName(), that.getName(), distance);
			return distance <= threshold;
		}catch(Exception ex) {
			LOG.error("Error calculating distance between {} and {}", this.getName(), that.getName());
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", super.getId())
				.add("name", super.getName()).toString();
	}

	@Override
	public double getDist(StateVertex vertexOfGraph) {
		if (vertexOfGraph instanceof HybridStateVertexImpl) {
			HybridStateVertexImpl vertex = (HybridStateVertexImpl) vertexOfGraph;
			return computeDistance(this.getDocument(), vertex.getDocument(), visualData);
		}
		return -1;
	}

	@Override
	public void setDocument(Document dom) {
		this.fragmentedDom = dom;
	}

	public static double computeDistance_Oracle(Document doc1, Document doc2, boolean visualData) {
		AptedNode<StringNodeData> aptedDoc1 = AptedUtils.getAptedTree(doc1, visualData);
//		System.out.println(aptedDoc1);
		AptedNode<StringNodeData> aptedDoc2 = AptedUtils.getAptedTree(doc2, visualData);
//		System.out.println(aptedDoc2);
		APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
		 
		double structuralDistance = apted.computeEditDistance(aptedDoc1, aptedDoc2);
//		System.out.println(getChangedNodes(doc1, doc2));
		double toRemove = 0;
		List<List<Node>> changedNodes = getChangedNodes(doc1, doc2, visualData);
		List<Node> doc1Nodes = changedNodes.get(0);
		List<Node> doc2Nodes = changedNodes.get(1);
		
		List<Node> allNodes = new ArrayList<>();
		allNodes.addAll(doc1Nodes);
		allNodes.addAll(doc2Nodes);
		for(Node node: allNodes) {
			if(!VipsUtils.isDisplayed(node, null)) {
				toRemove += 1;
			}
		}
		return structuralDistance - toRemove; 
	}
	
}

package com.crawljax.core.state;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.crawljax.core.CandidateElement;
import com.crawljax.fragmentation.Fragment;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.crawljax.vips_selenium.VipsRectangle;
import com.crawljax.vips_selenium.VipsUtils;
import com.crawljax.vips_selenium.VipsUtils.AccessType;
import com.crawljax.vips_selenium.VipsUtils.Coverage;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

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
	private String name;

	private transient HashMap<Node, List<CandidateElement>> nodeCandidateMapping = new HashMap<>();
	
	private transient ImmutableList<CandidateElement> candidateElements;

	private boolean isNearDuplicate;

	private int nearestState = -1;

	private double distToNearestState;
	
	private int cluster;
	

	transient private ArrayList<Fragment> fragments;
	
	transient private Fragment rootFragment;
	
	transient private HashMap<Integer, Fragment> fragmentMap;


	private boolean unexploredActions = true;


	/**
	 * Creates a current state without an url and the stripped dom equals the dom.
	 *
	 * @param name the name of the state
	 * @param dom  the current DOM tree of the browser
	 */
	@VisibleForTesting StateVertexImpl(int id, String name, String dom) {
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
		this.fragments =null;
		this.cluster = id;
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
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.toString();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Document getDocument() throws IOException {
		return DomUtils.asDocument(this.strippedDom);
	}

	@Override
	public void setElementsFound(LinkedList<CandidateElement> elements) {
		this.candidateElements = ImmutableList.copyOf(elements);
		createNodeCandidateMapping();
		if(fragments!=null) {
			addCandidatesToFragments();
		}
			
	}

	private void createNodeCandidateMapping() {
		for(CandidateElement candidate: candidateElements) {
			if(!nodeCandidateMapping.containsKey(candidate.getElement())){
				nodeCandidateMapping.put(candidate.getElement(), new ArrayList<>());
			}			
			nodeCandidateMapping.get(candidate.getElement()).add(candidate);
		}
	}
	
	public List<CandidateElement> getCandidateElement(Node equivalentNode) {
		if(equivalentNode == null) {
			return null;
		}
		return nodeCandidateMapping.get(equivalentNode);
	}

	private void addCandidatesToFragments() {
		
		for(CandidateElement element: candidateElements) {
		
			Fragment closest = getClosestFragment(element.getElement());
			Fragment closestDom = null;
			if(closest!=null) {
				if(closest.getFragmentParentNode()==null) {
					// Separate dom fragment hierarchy
					closestDom = getClosestDomFragment(element.getElement());
				}
				else {
					closestDom = closest;
				}
				
				// Add to normal hierarchy
				element.setClosestFragment(closest);
				Fragment fragment = closest;
				while(fragment!=null) {
					fragment.addCandidateElement(element);
					fragment = fragment.getParent();
				}
				
				// Add to DOM hierarchy
				element.setClosestDomFragment(closestDom);
				fragment = closestDom;
				while(fragment!=null) {
					fragment.addCandidateElement(element);
					fragment = fragment.getDomParent();
				}
			}
			else {
				LOG.error("Could not find closest fragment for {}", XPathHelper.getSkeletonXpath(element.getElement()));
			}
			
//			boolean added = false;
//			
//			for(Fragment fragment: fragments) {
//				if(fragment.includesCandidateNode(element.getElement())) {
//					fragment.addCandidateElement(element);
//					added = true;
//				}
//			}
//			if(!added) {
//				LOG.error("Could not add : " + element);
//			}
		}
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
	public void setNearestState(int vertex) {
		this.nearestState = vertex;
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
		return fragments;
	}
	
	@Override
	public void setFragments(ArrayList<Fragment> fragments) {
		this.fragments = fragments;
	}
	
	@Override
	public void addFragments(List<VipsRectangle> rectangles, WebDriver driver) {
		if(rectangles == null)
			return;
		
		HashMap<Integer, VipsRectangle> rectangleMap  = new HashMap<>();
		for(VipsRectangle rectangle: rectangles) {
			rectangleMap.put(rectangle.getId(), rectangle);
		}
		fragmentMap  = new HashMap<>();
		for(VipsRectangle rectangle : rectangles) {
//			String xpath = rectangle.getXpath();
			Fragment fragment = new Fragment(rectangle.getId(), rectangle.getNestedBlocks(), rectangle.getRect(), this);
			fragmentMap.put(rectangle.getId(), fragment);
			addFragment(fragment);
		}
		
		for(Integer rectKey : fragmentMap.keySet()) {
			Fragment current = fragmentMap.get(rectKey);
			int parentKey = rectangleMap.get(rectKey).getParentId();
			if(parentKey!= -1)
			{
				Fragment parentFragment = fragmentMap.get(parentKey);
				current.setParent(parentFragment);
				parentFragment.addChild(current);
			}
			
			else {
				this.rootFragment = current;
			}
		}
		
		
		try {
			setParentNode(rootFragment);
		}catch(Exception ex) {
//			ex.printStackTrace();
			LOG.error("Could not set parent node for root fragment in : " + this.getName());
		}
		
		try {
			generateDomFragments(fragmentMap, driver);
		}catch(Exception ex) {
			LOG.error("Could not generate dom fragments {}", getName());
			ex.printStackTrace();
		}
//		cleanFragments(driver);
		
//		exportFragments();
		
		if(candidateElements!=null) {
			addCandidatesToFragments();
		}
	}
	
	public void exportFragments(File screenshotsFolder, BufferedImage pageViewport) {
		
		if(screenshotsFolder.isDirectory()) {
			File fragFolder = new File(screenshotsFolder, FilenameUtils.getBaseName(this.getName()));
			fragFolder.mkdir();
			for(Fragment fragment : fragments) {
				if(!FragmentManager.usefulFragment(fragment)) {
					continue;
				}
				File subImageTarget = new File(fragFolder, "" + fragment.getId()+".png");
				VipsUtils.exportFragment(pageViewport, subImageTarget, fragment.getRect());
			}
		}
			
		
	}

	public int getNextFragmentId() {
		if(fragments == null)
			return -1;
		int maxId = 0;
		for(Fragment fragment: fragments) {
			if(fragment.getId()>maxId)
				maxId = fragment.getId();
		}
		return maxId+1;
	}
	
	private Fragment createDomFragment(Node fragmentParentNode, List<Node> nestedBlocks, Fragment parent, WebDriver driver){
		Fragment newFragment = new Fragment(-1, null, VipsUtils.getRectangle(fragmentParentNode, driver), this);
		newFragment.setFragmentParentNode(fragmentParentNode);
		if(!FragmentManager.usefulFragment(newFragment))
			return null;
	
		newFragment.setId(getNextFragmentId());
		newFragment.setNestedBlocks(nestedBlocks);	

		newFragment.setParent(parent);
		parent.addChild(newFragment);
		
		newFragment.setDomParent(parent);
		parent.addDomChildren(newFragment);
		
		fragments.add(newFragment);
		
		return newFragment;
	}
	
	private List<Fragment> getDomFragments(Node rootNode, List<Node> nestedBlocks, HashMap<Integer, Fragment> fragmentMap, Fragment parent, WebDriver driver) {
		// TODO: If not useful then return 

		Node lca = VipsUtils.getParentBox(nestedBlocks);

		Fragment created = null;
		List<Fragment> returnList = new ArrayList<Fragment>();
		int fragmentId = VipsUtils.getFragParent(rootNode);
		if(fragmentId >=0 ) {
			Fragment parentFragment = fragmentMap.get(fragmentId);
			if(parentFragment.getParent()!=null && parent!=null) {
				if( !parentFragment.getParent().equals(parent)) {
					LOG.debug("Problem with hierarchy {} : insert {}", fragmentId, parent.getId());
				}
				parentFragment.setDomParent(parent);
				parent.addDomChildren(parentFragment);
			}
//			parent = parentFragment;
			
//			LOG.info("{} Already a parent for fragment {}", XPathHelper.getSkeletonXpath(rootNode), fragmentId);
			if(nestedBlocks.size() <= 1) {
//				LOG.info("No DOM division needed for {}", fragmentId);
				return new ArrayList<Fragment>();
			}
			if(! FragmentManager.usefulFragment(parentFragment)){
				// No need to divide small fragments
				return new ArrayList<Fragment>();
			}
		
		}
		else {
			if(lca.isSameNode(rootNode)) {
				created = createDomFragment(rootNode, nestedBlocks, parent, driver);
				if(created!=null)
				{
					LOG.info("Created Dom fragment {}, child of {}, for {} ", created.getId(), parent.getId(), XPathHelper.getSkeletonXpath(rootNode));
					fragmentMap.put(created.getId(), created);
					returnList.add(created);
				}
			}
			else if(DomUtils.contains(rootNode, lca)) {
				// If root node contains the lca of nested blocks, then divide the lca
//				LOG.info("RootNode contains LCA of given nested blocks");
				return getDomFragments(lca, nestedBlocks, fragmentMap, parent, driver);
			}
		}
		
		if(created!=null) {
//			LOG.info("New Fragment created, any dom fragments from children will be children to created {}", created.getId());
			parent = created;
		}
		
		List<Node> children = VipsUtils.getChildren(rootNode);
		for(Node child : children) {
			if(child.getNodeName().equalsIgnoreCase("#text"))
				continue;
			
			// If already a parent  then divide it
			if(VipsUtils.getFragParent(child)>=0) {
				Fragment childFragment = fragmentMap.get(VipsUtils.getFragParent(child));
				returnList.addAll(getDomFragments(child, childFragment.getNestedBlocks(), fragmentMap, parent, driver));
			}
			
			//or atleast one vips block inside it,
			else {
				List<Node> containedNodes = getContainedNodes(child, nestedBlocks);
				if(!containedNodes.isEmpty()) {
					returnList.addAll(getDomFragments(child, containedNodes, fragmentMap, parent, driver));
				}
				else {
//					LOG.info("Child has no vips blocks, ignoring it");
				}
			}
		}
		return returnList;
	}
	
	private List<Node> getContainedNodes(Node node, List<Node> nestedBlocks) {
		List<Node> returnList = new ArrayList<Node>();
		for(Node block: nestedBlocks) {
			if(DomUtils.contains(node, block)) {
				returnList.add(block);
			}
		}
		return returnList;
	}

	public List<Fragment> generateDomFragments(HashMap<Integer, Fragment> fragmentMap, WebDriver driver){
		List<Fragment> added = new ArrayList<>();
		Node rootNode = rootFragment.getFragmentParentNode();
		if(rootNode==null) {
			try {
				rootNode= getDocument().getFirstChild();
			} catch (IOException e) {
				LOG.error("Could not find root node for the state {}", this.getName());
				return added;
			};
		}
		
		added.addAll(getDomFragments(rootNode, rootFragment.getNestedBlocks(), fragmentMap, rootFragment, driver));
		
		return added;
	}
	
	
	
	
	public List<Fragment> cleanFragments(WebDriver driver) {
		// TO divide fragments by DOM 
		List<Fragment> added = new ArrayList<>();
		CopyOnWriteArrayList<Fragment> copyFragments = new CopyOnWriteArrayList<>(fragments);
		for(Fragment fragment: copyFragments) {
			if(!fragment.getChildren().isEmpty()) {
				boolean shouldIgnoreFragment = false;
				for(Fragment child: fragment.getChildren()) {
					// If there is a useful DOM_FRAGMENT child, then ignore the fragment.
					if(child.getFragmentParentNode() != null && FragmentManager.usefulFragment(child)) {
						shouldIgnoreFragment = true;
						break;
					}
				}
				if(shouldIgnoreFragment)
					continue;
			}
			
			if(fragment.getFragmentParentNode() == null && FragmentManager.usefulFragment(fragment) ) {
				List<Fragment> addedChildren = divideFragmentByDom(fragment, driver);
				if(addedChildren!=null)
					added.addAll(addedChildren);
			}
			
		}
		
		return added;
	}

	private List<Fragment> divideFragmentByDom(Fragment fragment, WebDriver driver) {
		if(fragment.getFragmentParentNode()!=null) {
			LOG.warn("Cannot divide a fragment with parent node");
			return null;
		}
		
		Node parentBox = VipsUtils.getParentBox(fragment.getNestedBlocks());
		
		if(fragment.getParent()!=null) {
			List<Fragment> siblings = fragment.getParent().getChildren();
			List<Node> siblingLcas = getSiblingLca(siblings, fragment);
			List<Fragment> toBeAdded = getDifferentiatingNodes(fragment.getNestedBlocks(), siblingLcas, parentBox, driver);
			if(toBeAdded == null)
				return null;
			
			for(Fragment toAdd: toBeAdded) {
				int id = getNextFragmentId();
				toAdd.setId(id);
				toAdd.setParent(fragment);
				fragment.addChild(toAdd);
				toAdd.setUseful(true);
				fragments.add(toAdd);
				LOG.info("Added Fragment {} to {} using DOM division", toAdd.getId(), fragment.getId());
			}
			
			fragment.adjustRectangle();
			return toBeAdded;
		}
		return null;
	}

	private List<Fragment> getDifferentiatingNodes(List<Node> blocks, List<Node> siblingLcas, Node parentBox, WebDriver driver) {
		List<Fragment> fragmentsToAdd = new ArrayList<>();
		for(Node child: VipsUtils.getChildren(parentBox)) {
			
			if(child.getNodeName().equalsIgnoreCase("#text"))
				continue;
			
			Fragment newFragment = new Fragment(-1, null, VipsUtils.getRectangle(child, driver), this);
			newFragment.setFragmentParentNode(child);
			if(!FragmentManager.usefulFragment(newFragment))
				continue;
			
			List<Node> contains = new ArrayList<>();
			for(Node block: blocks) {
				if(DomUtils.contains(child, block)) {
					contains.add(block);
				}
			}
			if(contains.isEmpty())
				continue;
			
			if(isADifferentiator(siblingLcas, child)) {
				newFragment.setNestedBlocks(contains);
				newFragment.setFragmentParentNode(child);
				fragmentsToAdd.add(newFragment);
			}
			else {
				List<Fragment> childDiffNodes = getDifferentiatingNodes(blocks, siblingLcas, child, driver);
				fragmentsToAdd.addAll(childDiffNodes);
			}
			
		}
		
		return fragmentsToAdd;
	}

	

	private List<Node> getSiblingLca(List<Fragment> fragments, Fragment exclude){
		List<Node> lcas = new ArrayList<>();
		
		for(Fragment fragment: fragments) {
			if(fragment.equals(exclude))
				continue;
			Node lca = VipsUtils.getParentBox(fragment.getNestedBlocks());
			lcas.add(lca);
//			System.out.println("Fragment " + fragment.getId() + " : " + XPathHelper.getSkeletonXpath(lca));
		}
		return lcas;
	}
	
	private Node leastCommonAncestor(List<Fragment> fragments, Fragment exclude) {
		Node returnNode = null;
		List<Node> lcas = getSiblingLca(fragments, exclude);
		
		returnNode = VipsUtils.getParentBox(lcas);
		return returnNode;
	}
	
	private boolean isADifferentiator(List<Node> siblingLcas, Node lca) {
		for(Node siblingLca : siblingLcas) {
			if(DomUtils.contains(lca,  siblingLca))
				return false;
		}
		return true;
	}
	
	public Node highestDifferentiator(Fragment fragment, List<Fragment> siblings) {
		Node lca = VipsUtils.getParentBox(fragment.getNestedBlocks());
		
		Node siblingLca = leastCommonAncestor(siblings, fragment);
		
		if(lca==null || siblingLca==null)
			return null;
	
//		System.out.println("Fragment " + fragment.getId() + XPathHelper.getSkeletonXpath(lca));

		List<Node> siblingLcas = getSiblingLca(siblings, fragment);
		
		
//		
//		
//		if(VipsUtils.contains(lca, siblingLca)|| VipsUtils.contains(siblingLca, lca)) {
//			return null;
//		}
		
//		if(!isADifferentiator(siblingLcas, lca))
//			return null;
//		
//		while(lca.getParentNode()!=null) {
//			if(isADifferentiator(siblingLcas, lca.getParentNode()))
//				lca = lca.getParentNode();
//			else
//				break;
//		}
		
		// Using LCA of nested Blocks instead of highest differentiator
		if(isADifferentiator(siblingLcas, lca)) {
			return lca;
		}
		return null;
	}

	private void setParentNode(Fragment fragment) {
		
		if(this.rootFragment!=null && this.rootFragment.equals(fragment)) {
			Node fragmentParentNode = VipsUtils.getParentBox(fragment.getNestedBlocks());
			fragment.setFragmentParentNode(fragmentParentNode);
		}
		if(fragment.getChildren()==null || fragment.getChildren().isEmpty()) {
			return;
		}
		
		List<Fragment> children = fragment.getChildren();
		if(children.size() > 1) {
			for(Fragment child : children) {
				Node parentNode = highestDifferentiator(child, children);
				if(parentNode == null) {
					if(child.getNestedBlocks().size() == 1) {
						LOG.warn("No differentiator for single node{} of {} in {}", XPathHelper.getSkeletonXpath(child.getNestedBlocks().get(0)), child.getId(), child.getReferenceState().getName());
						parentNode = child.getNestedBlocks().get(0);
					}
				}
				child.setFragmentParentNode(parentNode);
//				System.out.println("Fragment " + child.getId() + ":" + XPathHelper.getSkeletonXpath(child.getFragmentParentNode()));
				setParentNode(child);
			}
		}
		else {
			setParentNode(children.get(0));
		}
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean isNearDuplicate() {
		return isNearDuplicate;
	}

	@Override
	public Fragment getRootFragment() {
		return rootFragment;
	}

	private void addFragment(Fragment fragment) {
		if(this.fragments == null) {
			this.fragments = new ArrayList<>();
		}
		if(!this.fragments.contains(fragment))
			this.fragments.add(fragment);
	}

	@Override
	public void setDocument(Document dom) {
		
	}
	
	private void printFragment(Fragment frag, StringBuilder buffer, String prefix, String childrenPrefix, boolean domOnly) {
		buffer.append(prefix);
		buffer.append(frag.getId());
		buffer.append('\n');

		for (Iterator<Fragment> it = domOnly ?  frag.getDomChildren().iterator():  frag.getChildren().iterator(); it.hasNext();) {
		    Fragment next = it.next();
		    if (it.hasNext()) {
		       printFragment(next, buffer, childrenPrefix + "├── ", childrenPrefix + "│   ", domOnly);
		} else {
		       printFragment(next, buffer, childrenPrefix + "└── ", childrenPrefix + "    ", domOnly);
		    }
		}
		    
	}
	
	public void printFragments(boolean domOnly) {
		StringBuilder buffer = new StringBuilder(1000);
		printFragment(rootFragment, buffer, "", "", domOnly);
		System.out.println( buffer.toString());
	}
	
	public Fragment getClosestDomFragment(Node node) {
		Node parent = node;
		while(parent != null ) {
			Fragment fragment = getFragment(VipsUtils.getFragParent(parent));
			if(fragment!= null) {
				LOG.debug("Closest dom fragment {} in {} for {}", fragment.getId(), this.getName(), XPathHelper.getSkeletonXpath(node));
				return fragment;
			}
			parent = parent.getParentNode();
		}
		return null;
	}
	
	private Fragment getFragment(int fragParent) {
		if(fragParent>=0) {
			Fragment frag = fragmentMap.get(fragParent);
			if(frag!=null && FragmentManager.usefulFragment(frag)) {
				return frag;
			}
		}
		return null;
	}

	public Fragment getClosestFragment(Node node) {
		Fragment root = this.getRootFragment();
		if (!root.containsNode(node)) {
			return null;
		}
		
		return getClosestFragment(node, root);
	}

	private Fragment getClosestFragment(Node node, Fragment root) {
//		System.out.println("Node to check :" + XPathHelper.getSkeletonXpath(node));
		if(root.getChildren().isEmpty()) {
			return root;
		}
		boolean foundChild = false;
		for (Fragment child : root.getChildren()) {
//			System.out.println("Fragment " + child.getId() + ":" + XPathHelper.getSkeletonXpath(child.getFragmentParentNode()));
			if (child.containsNode(node)) {
				if (FragmentManager.usefulFragment(child)) {
					root = child;
					foundChild = true;
					break;
				} 
				else {
					return root;
				}
			}
//			else {
//				LOG.info("Child {} does not contain the node ", child.getId());
//			}
		}
		if(foundChild)
			LOG.debug("Child {} is closer and useful" , root.getId());
		
		else {
			LOG.debug("This is the closest Fragmemt {}", root.getId());
		}

		return foundChild ? getClosestFragment(node, root): root;
		
	}
	

	public Fragment getClosestFragment(CandidateElement element, Fragment root) {
		
		if(root.getChildren().isEmpty()) {
			return root;
		}
		boolean foundChild = false;
		for (Fragment child : root.getChildren()) {
			if (child.containsCandidate(element)) {
				if (FragmentManager.usefulFragment(child)) {
					root = child;
					foundChild = true;
					break;
				} 
				else {
					return root;
				}
			}
		}
		

		return foundChild ? getClosestFragment(element, root): root;
		
	}
	


	public Fragment getClosestDomFragment(CandidateElement element){
		if(element.getClosestDomFragment()!=null) {
			return element.getClosestDomFragment();
		}
		Node node =element.getElement();
		Fragment closestFragment = getClosestDomFragment(node);

		if(closestFragment!=null) {
			if(closestFragment.containsCandidate(element)) {
				element.setClosestDomFragment(closestFragment);
				return closestFragment;
			}
		}
		return null;
	}

	public Fragment getClosestFragment(CandidateElement element) throws Exception{
		if(element.getClosestFragment()!=null) {
			return element.getClosestFragment();
		}
		Node node =element.getElement();
		Fragment closestFragment = getClosestFragment(node);
//		Fragment root = this.getRootFragment();
//		if (!root.containsCandidate(element)) {
//			return null;
//		}
		if(closestFragment!=null) {
			if(closestFragment.containsCandidate(element)) {
				element.setClosestFragment(closestFragment);
				return closestFragment;
			}
		}
//		return getClosestFragment(element, root);
		return null;
	}

	@Override
	public boolean hasUnexploredActions() {
		if(this.unexploredActions == false) {
			return false;
		}
		for(CandidateElement element: candidateElements) {
			if(!element.wasExplored()) {
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
		
//		for(CandidateElement candidate: candidateElements) {
//			if(candidate.getElement().isSameNode(equiv)) {
//				return candidate;
//			}
//		}
		if(getCandidateElement(equiv) !=null && !getCandidateElement(equiv).isEmpty()) {
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
		this.cluster= cluster;
		
	}

	@Override
	public void setDirectAccess(CandidateElement element) {
		for(CandidateElement candidate: nodeCandidateMapping.get(element.getElement())) {
			candidate.setDirectAccess(true);
		}
	}

}

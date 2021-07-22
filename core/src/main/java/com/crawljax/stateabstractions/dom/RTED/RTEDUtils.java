package com.crawljax.stateabstractions.dom.RTED;

import com.crawljax.util.DomUtils;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import java.io.IOException;

public class RTEDUtils {

	/**
	 * Get a scalar value for the DOM diversity using the Robust Tree Edit Distance
	 *
	 * @param dom1
	 * @param dom2
	 * @return
	 */
	public static double getRobustTreeEditDistance(String dom1, String dom2) {

		LblTree domTree1 = null, domTree2 = null;
		
		try {
			domTree1 = getDomTree(dom1);
			domTree2 = getDomTree(dom2);
		} catch (IOException e) {
			e.printStackTrace();
		}

		double DD = 0.0;
		RTED_InfoTree_Opt rted;
		double ted;

		rted = new RTED_InfoTree_Opt(1, 1, 1);

		// compute tree edit distance
		rted.init(domTree1, domTree2);

		int maxSize = Math.max(domTree1.getNodeCount(), domTree2.getNodeCount());

		rted.computeOptimalStrategy();
		ted = rted.nonNormalizedTreeDist();
		ted /= (double) maxSize;

		DD = ted;
		return DD;
	}

	private static LblTree getDomTree(String dom1) throws IOException {

		org.w3c.dom.Document doc1 = DomUtils.asDocument(dom1);

		LblTree domTree = null;

		DocumentTraversal traversal = (DocumentTraversal) doc1;
		TreeWalker walker = traversal.createTreeWalker(doc1.getElementsByTagName("body").item(0),
				NodeFilter.SHOW_ELEMENT, null, true);
		domTree = createTree(walker);

		return domTree;
	}

	/**
	 * Recursively construct a LblTree from DOM tree
	 *
	 * @param walker tree walker for DOM tree traversal
	 * @return tree represented by DOM tree
	 */
	private static LblTree createTree(TreeWalker walker) {
		Node parent = walker.getCurrentNode();
		LblTree node = new LblTree(parent.getNodeName(), -1); // treeID = -1
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			node.add(createTree(walker));
		}
		walker.setCurrentNode(parent);
		return node;
	}
}

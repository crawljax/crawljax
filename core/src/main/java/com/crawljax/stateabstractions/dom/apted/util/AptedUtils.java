package com.crawljax.stateabstractions.dom.apted.util;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.TreeWalker;

import com.crawljax.fragmentation.Fragment;
import com.crawljax.stateabstractions.dom.apted.node.AptedNode;
import com.crawljax.stateabstractions.dom.apted.node.StringNodeData;
import com.crawljax.vips_selenium.VipsUtils;

public class AptedUtils {
				
	
	public static AptedNode<StringNodeData> getAptedTree(Document doc, boolean visualData) {

//		AptedNode<StringNodeData> domTree = null;
//
//		DocumentTraversal traversal = (DocumentTraversal) doc;
//		TreeWalker walker = traversal.createTreeWalker(doc.getDocumentElement(),
//				NodeFilter.SHOW_ELEMENT, null, true);
//		domTree = createTree(walker);
//
//		return domTree;
		return getAptedTree(doc.getElementsByTagName("body").item(0), visualData);
	}

	/**
	 * Recursively construct a LblTree from DOM tree
	 *
	 * @param walker tree walker for DOM tree traversal
	 * @return tree represented by DOM tree
	 */
	private static AptedNode<StringNodeData> createTree(TreeWalker walker) {
		Node parent = walker.getCurrentNode();
		AptedNode<StringNodeData> node = new AptedNode<StringNodeData>(new StringNodeData(getNodeStringRepresentation(parent, false)));
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			node.addChild(createTree(walker));
		}
		walker.setCurrentNode(parent);
		return node;
	}

	public static String getNodeStringRepresentation(Node node, boolean visualData) {
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
		if(node==null) {
			return null;
		}
		if(visualData && !node.getNodeName().equalsIgnoreCase("#text")) {
			return (VipsUtils.isDisplayed(node, null)? "V": "H") + node.getNodeName();
		}
		return node.getNodeName();
		/*}*/
	}

//	public static AptedNode getAptedTree(Node fragmentParentNode) {
//		AptedNode<StringNodeData> domTree = null;
//
//		DocumentTraversal traversal = (DocumentTraversal) fragmentParentNode;
//		TreeWalker walker = traversal.createTreeWalker(fragmentParentNode,
//				NodeFilter.SHOW_ELEMENT, null, true);
//		domTree = createTree(walker);
//
//		return domTree;
//	}
	
	public static AptedNode<StringNodeData> getTextNode(){
		AptedNode<StringNodeData> text = new AptedNode<StringNodeData>(new StringNodeData("#text"));
		return text;
	}
	
	public static AptedNode<StringNodeData> getAptedTree(Node node, boolean visualData) {
		if(node ==null) {
			return null;
//			return new AptedNode<StringNodeData>(new StringNodeData(""));
		}
		
		if(getNodeStringRepresentation(node, visualData) == null) {
			System.out.println("string is null for : " + node);
			return null;
		}
		AptedNode<StringNodeData> root = new AptedNode<StringNodeData>(new StringNodeData(getNodeStringRepresentation(node, visualData)));
		
		if(node.getNodeName().equalsIgnoreCase("select")) {
			// Do not add multiple options for select
			for(Node child: VipsUtils.getChildren(node)) {
				if(child.getNodeName().equalsIgnoreCase("option")) {
					AptedNode<StringNodeData> option = new AptedNode<StringNodeData>(new StringNodeData(getNodeStringRepresentation(child, visualData))); 
					root.addChild(option);
					break;
				}
			}
			
			return root;
		}
		
		for (Node item: VipsUtils.getChildren(node)) {
			AptedNode<StringNodeData> childTree = getAptedTree(item, visualData);
			if(childTree != null)
				root.addChild(childTree);
		}

		return root;
	}

	public static AptedNode getAptedTree(Fragment fragment, boolean visualData) {
		
		if(fragment.getFragmentParentNode() != null) {
			return getAptedTree(fragment.getFragmentParentNode(), visualData);
		}
		
		AptedNode<StringNodeData> root = new AptedNode<StringNodeData>(new StringNodeData("dummy"));
		for(Node nestedBlock: fragment.getNestedBlocks()) {
			root.addChild(getAptedTree(nestedBlock, visualData));
		}
		
		return root;
	}
}

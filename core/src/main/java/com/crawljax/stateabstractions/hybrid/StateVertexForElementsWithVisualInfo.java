package com.crawljax.stateabstractions.hybrid;

import java.util.List;
import java.util.Map;

import com.crawljax.core.state.StateVertexImpl;
import com.google.common.collect.Maps;

public class StateVertexForElementsWithVisualInfo extends StateVertexImpl {
	
	private static final long serialVersionUID = -6046894716418208778L;

	// The map from XPath to DOMElementWithVisualInfo, for faster access
	// This causes redundancy in the generated JSON though
	private final Map<String, DOMElementWithVisualInfo> domElementsWithVisualInfoMap;

	public StateVertexForElementsWithVisualInfo(int id, String url, String name, 
			String dom, String strippedDom,
			List<DOMElementWithVisualInfo> domElementsWithVisualInfo) {
		super(id, url, name, dom, strippedDom);
		this.domElementsWithVisualInfoMap = Maps.newHashMap();
		for (DOMElementWithVisualInfo domElement : domElementsWithVisualInfo) {
			domElementsWithVisualInfoMap.put(domElement.getXpath(), domElement);
		}
	}
	
	public DOMElementWithVisualInfo getElementWithVisualInfo(String nodeXPath) {
		return domElementsWithVisualInfoMap.get(nodeXPath);
	}
	
}

package com.crawljax.util;

import java.util.List;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

final class DomDifferenceListener implements DifferenceListener {
	private final List<String> ignoreAttributes;

	DomDifferenceListener(List<String> ignoreAttributes) {
		this.ignoreAttributes = ignoreAttributes;
	}

	@Override
	public void skippedComparison(Node control, Node test) {
	}

	@Override
	public int differenceFound(Difference difference) {
		if (difference.getControlNodeDetail() == null
		        || difference.getControlNodeDetail().getNode() == null
		        || difference.getTestNodeDetail() == null
		        || difference.getTestNodeDetail().getNode() == null) {
			return RETURN_ACCEPT_DIFFERENCE;
		}
		if (ignoreAttributes.contains(difference.getTestNodeDetail().getNode()
		        .getNodeName())
		        || ignoreAttributes.contains(difference.getControlNodeDetail()
		                .getNode().getNodeName())) {
			return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
		}
		return RETURN_ACCEPT_DIFFERENCE;
	}
}
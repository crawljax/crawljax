/**
 * 
 */
package com.crawljax.condition;

import org.w3c.dom.NodeList;

/**
 * Abstract class for Condition.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public abstract class AbstractCondition implements Condition {

	private NodeList affectedNodes;

	/**
	 * @return the affectedNodes. can be null
	 */
	public NodeList getAffectedNodes() {
		return affectedNodes;
	}

	/**
	 * @param affectedNodes
	 *            the affectedNodes to set
	 */
	protected void setAffectedNodes(NodeList affectedNodes) {
		this.affectedNodes = affectedNodes;
	}

}

/**
 * 
 */
package com.crawljax.condition;

import org.w3c.dom.NodeList;

/**
 * Abstract class for Condition.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id: AbstractCondition.java 6388 2009-12-29 13:36:00Z mesbah $
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

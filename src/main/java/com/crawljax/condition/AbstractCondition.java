/**
 * 
 */
package com.crawljax.condition;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;

import org.w3c.dom.NodeList;

/**
 * Abstract class for Condition. This class and derivatives are NOT Thread safe! The
 * setAfftectedNodes and getAffectedNodes are synchronised BUT with a interleaving from two browsers
 * the getAffected nodes can contain data from a other run.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
@NotThreadSafe
public abstract class AbstractCondition implements Condition {

	private NodeList affectedNodes;

	/**
	 * @return the affectedNodes. can be null
	 */
	@GuardedBy("this")
	public synchronized NodeList getAffectedNodes() {
		return affectedNodes;
	}

	/**
	 * @param affectedNodes
	 *            the affectedNodes to set
	 */
	@GuardedBy("this")
	protected synchronized void setAffectedNodes(NodeList affectedNodes) {
		this.affectedNodes = affectedNodes;
	}

}

// Copyright (C) 2012 Mateusz Pawlik and Nikolaus Augsten
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

package com.crawljax.stateabstractions.dom.RTED;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;

/**
 * A node of a tree. Each tree has an ID. The label can be empty, but can not contain trailing
 * spaces (nor consist only of spaces). Two nodes are equal, if there labels are equal, and n1 &lt; n2
 * if label(n1) &lt; label(n2).
 *
 * @author Nikolaus Augsten from approxlib, available at http://www.inf.unibz.it/~augsten/src/
 * modified by Mateusz Pawlik
 */
public class LblTree extends DefaultMutableTreeNode implements Comparable<Object> {

	private static final long serialVersionUID = 7327318198638813930L;

	public static final String TAB_STRING = "    ";
	public static final String ROOT_STRING = "*---+";
	public static final String BRANCH_STRING = "+---+";

	public static final String OPEN_BRACKET = "{";
	public static final String CLOSE_BRACKET = "}";
	public static final String ID_SEPARATOR = ":";

	public static final int HIDE_NOTHING = 0;
	public static final int HIDE_ROOT_LABEL = 1;
	public static final int RENAME_LABELS_TO_LEVEL = 2;
	public static final int HIDE_ALL_LABELS = 3;
	public static final int RANDOM_ROOT_LABEL = 4;

	/**
	 * no node id
	 */
	public final int NO_NODE = -1;

	/**
	 * no tree id is defined
	 */
	public final int NO_TREE_ID = -1;

	int treeID = NO_TREE_ID;
	String label = null;
	Object tmpData = null;
	int nodeID = NO_NODE;

	/**
	 * Use only this constructor!
	 */
	public LblTree(String label, int treeID) {
		super();
		this.treeID = treeID;
		this.label = label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public int getTreeID() {
		if (isRoot()) {
			return treeID;
		} else {
			return ((LblTree) getRoot()).getTreeID();
		}
	}

	public void setTreeID(int treeID) {
		if (isRoot()) {
			this.treeID = treeID;
		} else {
			((LblTree) getRoot()).setTreeID(treeID);
		}
	}

	/**
	 * tmpData: Hook for any data that a method must attach to a tree. Methods can assume, that this
	 * date is null and should return it to be null!
	 */
	public void setTmpData(Object tmpData) {
		this.tmpData = tmpData;
	}

	public Object getTmpData() {
		return tmpData;
	}

	public void prettyPrint() {
		prettyPrint(false);
	}

	public void prettyPrint(boolean printTmpData) {
		for (int i = 0; i < getLevel(); i++) {
			System.out.print(TAB_STRING);
		}
		if (!isRoot()) {
			System.out.print(BRANCH_STRING);
		} else {
			if (getTreeID() != NO_TREE_ID) {
				System.out.println("treeID: " + getTreeID());
			}
			System.out.print(ROOT_STRING);
		}
		System.out.print(" '" + this.getLabel() + "' ");
		if (printTmpData) {
			System.out.println(getTmpData());
		} else {
			System.out.println();
		}
		for (Enumeration<?> e = children(); e.hasMoreElements(); ) {
			((LblTree) e.nextElement()).prettyPrint(printTmpData);
		}

	}

	public int getNodeCount() {
		int sum = 1;
		for (Enumeration<?> e = children(); e.hasMoreElements(); ) {
			sum += ((LblTree) e.nextElement()).getNodeCount();
		}
		return sum;
	}

	/**
	 * String representation of a tree.
	 *
	 * @return string representation of this tree
	 */
	@Override
	public String toString() {
		String res = OPEN_BRACKET + getLabel();
		if ((getTreeID() >= 0) && (isRoot())) {
			res = getTreeID() + ID_SEPARATOR + res;
		}
		for (Enumeration<?> e = children(); e.hasMoreElements(); ) {
			res += e.nextElement().toString();
		}
		res += CLOSE_BRACKET;
		return res;
	}

	/**
	 * Compares the labels.
	 */
	public int compareTo(Object o) {
		return getLabel().compareTo(((LblTree) o).getLabel());
	}

	/**
	 * Clear tmpData in subtree rooted in this node.
	 */
	public void clearTmpData() {
		for (Enumeration<?> e = breadthFirstEnumeration(); e.hasMoreElements(); ) {
			((LblTree) e.nextElement()).setTmpData(null);
		}
	}

}

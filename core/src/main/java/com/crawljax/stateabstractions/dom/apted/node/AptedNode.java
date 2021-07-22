/* MIT License
 *
 * Copyright (c) 2017 Mateusz Pawlik
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.crawljax.stateabstractions.dom.apted.node;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a recursive representation of an ordered tree. Each node stores a
 * list of pointers to its children. The order of children is significant and
 * must be observed while implmeneting a custom input parser.
 *
 * @param <D> the type of node data (node label).
 */
public class AptedNode<D> {

  /**
   * Information associated to and stored at each node. This can be anything
   * and depends on the application, for example, string label, key-value pair,
   * list of values, etc.
   */
  private D nodeData;

  /**
   * Array of pointers to this node's children. The order of children is
   * significant due to the definition of ordered trees.
   */
  private List<AptedNode<D>> children;

  /**
   * Constructs a new node with the passed node data and an empty list of
   * children.
   *
   * @param nodeData instance of node data (node label).
   */
  public AptedNode(D nodeData) {
    this.children = new ArrayList<>();
    setNodeData(nodeData);
  }

  /**
   * Counts the number of nodes in a tree rooted at this node.
   *
   * <p>This method runs in linear time in the tree size.
   *
   * @return number of nodes in the tree rooted at this node.
   */
  public int getNodeCount() {
      int sum = 1;
      for(AptedNode<D> child : getChildren()) {
        sum += child.getNodeCount();
      }
      return sum;
  }

  /**
   * Adds a new child at the end of children list. The added child will be
   * the last child of this node.
   *
   * @param c child node to add.
   */
  public void addChild(AptedNode c) {
    this.children.add(c);
  }

  /**
   * Returns a string representation of the tree in bracket notation.
   *
   * <p>IMPORTANT: Works only for nodes storing {@link node.StringNodeData}
   * due to using {@link node.StringNodeData#getLabel()}.
   *
   * @return tree in bracket notation.
   */
  public String toString() {
    String res = (new StringBuilder("{")).append(((StringNodeData)getNodeData()).getLabel()).toString();
    for(AptedNode<D> child : getChildren()) {
      res = (new StringBuilder(String.valueOf(res))).append(child.toString()).toString();
    }
    res = (new StringBuilder(String.valueOf(res))).append("}").toString();
    return res;
  }

  /**
   * Returns node data. Used especially for calculating rename cost.
   *
   * @return node data (label of a node).
   */
  public D getNodeData() {
    return nodeData;
  }

  /**
   * Sets the node data of this node.
   *
   * @param nodeData instance of node data (node label).
   */
  public void setNodeData(D nodeData) {
    this.nodeData = nodeData;
  }

  /**
   * Returns the list with all node's children.
   *
   * @return children of the node.
   */
  public List<AptedNode<D>> getChildren() {
    return children;
  }

}

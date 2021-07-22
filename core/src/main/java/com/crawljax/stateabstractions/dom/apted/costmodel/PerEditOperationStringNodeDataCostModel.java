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

package com.crawljax.stateabstractions.dom.apted.costmodel;

import com.crawljax.stateabstractions.dom.apted.node.AptedNode;
import com.crawljax.stateabstractions.dom.apted.node.StringNodeData;

/**
 * This is a cost model defined on {@link node.StringNodeData} with a fixed cost
 * per edit operation.
 */
public class PerEditOperationStringNodeDataCostModel implements CostModel<StringNodeData> {

  /**
   * Stores the cost of deleting a node.
   */
  private float delCost;

  /**
   * Stores the cost of inserting a node.
   */
  private float insCost;

  /**
   * Stores the cost of mapping two nodes (renaming their labels).
   */
  private float renCost;

  /**
   * Initialises the cost model with the passed edit operation costs.
   *
   * @param delCost deletion cost.
   * @param insCost insertion cost.
   * @param renCost rename cost.
   */
  public PerEditOperationStringNodeDataCostModel(float delCost, float insCost, float renCost) {
    this.delCost = delCost;
    this.insCost = insCost;
    this.renCost = renCost;
  }

  /**
   * Calculates the cost of deleting a node.
   *
   * @param n the node considered to be deleted.
   * @return the cost of deleting node n.
   */
  public float del(AptedNode<StringNodeData> n) {
    return delCost;
  }

  /**
   * Calculates the cost of inserting a node.
   *
   * @param n the node considered to be inserted.
   * @return the cost of inserting node n.
   */
  public float ins(AptedNode<StringNodeData> n) {
    return insCost;
  }

  /**
   * Calculates the cost of renaming the string labels of two nodes.
   *
   * @param n1 the source node of rename.
   * @param n2 the destination node of rename.
   * @return the cost of renaming node n1 to n2.
   */
  public float ren(AptedNode<StringNodeData> n1, AptedNode<StringNodeData> n2) {
    return (n1.getNodeData().getLabel().equals(n2.getNodeData().getLabel())) ? 0.0f : renCost;
  }
}

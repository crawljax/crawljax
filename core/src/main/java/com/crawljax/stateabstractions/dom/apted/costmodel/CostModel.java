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

/**
 * This interface specifies the methods to implement for a custom cost model.
 * The methods represent the costs of edit operations (delete, insert, rename).
 *
 * <p>If the cost function is a metric, the tree edit distance is a metric too.
 *
 * <p>However, the cost function does not have to be a metric - the costs of
 * deletion, insertion and rename can be arbitrary.
 *
 * <p>IMPORTANT: Mind the <b>float</b> type use for costs.
 *
 * @param <D> type of node data on which the cost model is defined.
 */
public interface CostModel<D> {

  /**
   * Calculates the cost of deleting a node.
   *
   * @param n the node considered to be deleted.
   * @return the cost of deleting node n.
   */
  public float del(AptedNode<D> n);

  /**
   * Calculates the cost of inserting a node.
   *
   * @param n the node considered to be inserted.
   * @return the cost of inserting node n.
   */
  public float ins(AptedNode<D> n);

  /**
   * Calculates the cost of renaming (mapping) two nodes.
   *
   * @param n1 the source node of rename.
   * @param n2 the destination node of rename.
   * @return the cost of renaming (mapping) node n1 to n2.
   */
  public float ren(AptedNode<D> n1, AptedNode<D> n2);
}

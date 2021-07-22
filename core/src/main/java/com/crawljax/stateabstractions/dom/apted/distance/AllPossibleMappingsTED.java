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

package com.crawljax.stateabstractions.dom.apted.distance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.crawljax.stateabstractions.dom.apted.costmodel.CostModel;
import com.crawljax.stateabstractions.dom.apted.node.AptedNode;
import com.crawljax.stateabstractions.dom.apted.node.NodeIndexer;

/**
 * Implements an exponential algorithm for the tree edit distance. It computes
 * all possible TED mappings between two trees and calculated their minimal
 * cost.
 *
 * @param <C> type of cost model.
 * @param <D> type of node data.
 */
public class AllPossibleMappingsTED<C extends CostModel, D> {

  /**
   * Indexer of the source tree.
   *
   * @see node.NodeIndexer
   */
  private NodeIndexer it1;

  /**
   * Indexer of the destination tree.
   *
   * @see node.NodeIndexer
   */
  private NodeIndexer it2;

  /**
   * The size of the source input tree.
   */
  private int size1;

  /**
   * The size of the destination tree.
   */
  private int size2;

  /**
   * Cost model to be used for calculating costs of edit operations.
   */
  private C costModel;

  /**
   * Constructs the AllPossibleMappingsTED algorithm with a specific cost model.
   *
   * @param costModel a cost model used in the algorithm.
   */
  public AllPossibleMappingsTED(C costModel) {
    this.costModel = costModel;
  }

  /**
   * Computes the tree edit distance between two trees by trying all possible
   * TED mappings. It uses the specified cost model.
   *
   * @param t1 source tree.
   * @param t2 destination tree.
   * @return the tree edit distance between two trees.
   */
  public float computeEditDistance(AptedNode<D> t1, AptedNode<D> t2) {
    // Index the nodes of both input trees.
    init(t1, t2);
    ArrayList<ArrayList<int[]>> mappings = generateAllOneToOneMappings();
    removeNonTEDMappings(mappings);
    return getMinCost(mappings);
  }

  /**
   * Indexes the input trees.
   *
   * @param t1 source tree.
   * @param t2 destination tree.
   */
  public void init(AptedNode<D> t1, AptedNode<D> t2) {
    it1 = new NodeIndexer(t1, costModel);
    it2 = new NodeIndexer(t2, costModel);
    size1 = it1.getSize();
    size2 = it2.getSize();
  }

  /**
   * Generate all possible 1-1 mappings.
   *
   * <p>These mappings do not conform to TED conditions (sibling-order and
   * ancestor-descendant).
   *
   * <p>A mapping is a list of pairs (arrays) of preorder IDs (identifying
   * nodes).
   *
   * @return set of all 1-1 mappings.
   */
  private ArrayList<ArrayList<int[]>> generateAllOneToOneMappings() {
    // Start with an empty mapping - all nodes are deleted or inserted.
    ArrayList<ArrayList<int[]>> mappings = new ArrayList<ArrayList<int[]>>(1);
    mappings.add(new ArrayList<int[]>(size1 + size2));
    // Add all deleted nodes.
    for (int n1 = 0; n1 < size1; n1++) {
      mappings.get(0).add(new int[]{n1, -1});
    }
    // Add all inserted nodes.
    for (int n2 = 0; n2 < size2; n2++) {
      mappings.get(0).add(new int[]{-1, n2});
    }
    // For each node in the source tree.
    for (int n1 = 0; n1 < size1; n1++) {
      // Duplicate all mappings and store in mappings_copy.
      ArrayList<ArrayList<int[]>> mappings_copy = deepMappingsCopy(mappings);
      // For each node in the destination tree.
      for (int n2 = 0; n2 < size2; n2++) {
        // For each mapping (produced for all n1 values smaller than
        // current n1).
        for (ArrayList<int[]> m : mappings_copy) {
          // Produce new mappings with the pair (n1, n2) by adding this
          // pair to all mappings where it is valid to add.
          boolean element_add = true;
          // Verify if (n1, n2) can be added to mapping m.
          // All elements in m are checked with (n1, n2) for possible
          // violation.
          // One-to-one condition.
          for (int[] e : m) {
            // n1 is not in any of previous mappings
            if (e[0] != -1 && e[1] != -1 && e[1] == n2) {
              element_add = false;
              // System.out.println("Add " + n2 + " false.");
              break;
            }
          }
          // New mappings must be produced by duplicating a previous
          // mapping and extending it by (n1, n2).
          if (element_add) {
            ArrayList<int[]> m_copy = deepMappingCopy(m);
            m_copy.add(new int[]{n1, n2});
            // If a pair (n1,n2) is added, (n1,-1) and (-1,n2) must be removed.
            removeMappingElement(m_copy, new int[]{n1, -1});
            removeMappingElement(m_copy, new int[]{-1, n2});
            mappings.add(m_copy);
          }
        }
      }
    }
    return mappings;
  }

  /**
   * Given all 1-1 mappings, discard these that violate TED conditions
   * (ancestor-descendant and sibling order).
   *
   * @param mappings set of all 1-1 mappings.
   */
  private void removeNonTEDMappings(ArrayList<ArrayList<int[]>> mappings) {
    // Validate each mapping separately.
    // Iterator safely removes mappings while iterating.
    for (Iterator<ArrayList<int[]>> mit = mappings.iterator(); mit.hasNext();) {
      ArrayList<int[]> m = mit.next();
      if (!isTEDMapping(m)) {
        mit.remove();
      }
    }
  }

  /**
   * Test if a 1-1 mapping is a TED mapping.
   *
   * @param m a 1-1 mapping.
   * @return {@code true} if {@code m} is a TED mapping, and {@code false}
   *         otherwise.
   */
  boolean isTEDMapping(ArrayList<int[]> m) {
    // Validate each pair of pairs of mapped nodes in the mapping.
    for (int[] e1 : m) {
      // Use only pairs of mapped nodes for validation.
      if (e1[0] == -1 || e1[1] == -1) {
        continue;
      }
      for (int[] e2 : m) {
        // Use only pairs of mapped nodes for validation.
        if (e2[0] == -1 || e2[1] == -1) {
          continue;
        }
        // If any of the conditions below doesn't hold, discard m.
        // Validate ancestor-descendant condition.
        boolean a = e1[0] < e2[0] && it1.preL_to_preR[e1[0]] < it1.preL_to_preR[e2[0]];
        boolean b = e1[1] < e2[1] && it2.preL_to_preR[e1[1]] < it2.preL_to_preR[e2[1]];
        if ((a && !b) || (!a && b)) {
          // Discard the mapping.
          // If this condition doesn't hold, the next condition
          // doesn't have to be verified any more and any other
          // pair (e1, e2) doesn't have to be verified any more.
          return false;
        }
        // Validate sibling-order condition.
        a = e1[0] < e2[0] && it1.preL_to_preR[e1[0]] > it1.preL_to_preR[e2[0]];
        b = e1[1] < e2[1] && it2.preL_to_preR[e1[1]] > it2.preL_to_preR[e2[1]];
        if ((a && !b) || (!a && b)) {
          // Discard the mapping.
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Given list of all TED mappings, calculate the cost of the minimal-cost
   * mapping.
   *
   * @param tedMappings set of all TED mappings.
   * @return the minimal cost among all TED mappings.
   */
  float getMinCost(ArrayList<ArrayList<int[]>> tedMappings) {
    // Initialize min_cost to the upper bound.
    float min_cost = size1 + size2;
    // System.out.println("min_cost = " + min_cost);
    // Verify cost of each mapping.
    for (ArrayList<int[]> m : tedMappings) {
      float m_cost = 0;
      // Sum up edit costs for all elements in the mapping m.
      for (int[] e : m) {
        // Add edit operation cost.
        if (e[0] > -1 && e[1] > -1) {
          m_cost += costModel.ren(it1.preL_to_node[e[0]], it2.preL_to_node[e[1]]); // USE COST MODEL - rename e[0] to e[1].
        } else if (e[0] > -1) {
          m_cost += costModel.del(it1.preL_to_node[e[0]]); // USE COST MODEL - insert e[1].
        } else {
          m_cost += costModel.ins(it2.preL_to_node[e[1]]); // USE COST MODEL - delete e[0].
        }
        // Break as soon as the current min_cost is exceeded.
        // Only for early loop break.
        if (m_cost >= min_cost) {
          break;
        }
      }
      // Store the minimal cost - compare m_cost and min_cost
      if (m_cost < min_cost) {
        min_cost = m_cost;
      }
      // System.out.printf("min_cost = %.8f\n", min_cost);
    }
    return min_cost;
  }

  /**
   * Makes a deep copy of a mapping.
   *
   * @param mapping mapping to copy.
   * @return a mapping.
   */
  private ArrayList<int[]> deepMappingCopy(ArrayList<int[]> mapping) {
    ArrayList<int[]> mapping_copy = new ArrayList<int[]>(mapping.size());
    for (int[] me : mapping) { // for each mapping element in a mapping
      mapping_copy.add(Arrays.copyOf(me, me.length));
    }
    return mapping_copy;
  }

  /**
   * Makes a deep copy of a set of mappings.
   *
   * @param mappings set of mappings to copy.
   * @return set of mappings.
   */
  private ArrayList<ArrayList<int[]>> deepMappingsCopy(ArrayList<ArrayList<int[]>> mappings) {
    ArrayList<ArrayList<int[]>> mappings_copy = new ArrayList<ArrayList<int[]>>(mappings.size());
    for (ArrayList<int[]> m : mappings) { // for each mapping in mappings
      ArrayList<int[]> m_copy = new ArrayList<int[]>(m.size());
      for (int[] me : m) { // for each mapping element in a mapping
        m_copy.add(Arrays.copyOf(me, me.length));
      }
      mappings_copy.add(m_copy);
    }
    return mappings_copy;
  }

  /**
   * Constructs a string representation of a set of mappings.
   *
   * @param mappings set of mappings to convert.
   * @return string representation of a set of mappings.
   */
  private String mappingsToString(ArrayList<ArrayList<int[]>> mappings) {
    String result = "Mappings:\n";
    for (ArrayList<int[]> m : mappings) {
      result += "{";
      for (int[] me : m) {
        result += "[" + me[0] + "," + me[1] + "]";
      }
      result += "}\n";
    }
    return result;
  }

  /**
   * Removes an element (edit operation) from a mapping by its value. In our
   * case the element to remove can be always found in the mapping.
   *
   * @param m an edit mapping.
   * @param e element to remove from {@code m}.
   * @return {@code true} if {@code e} has been removed, and {@code false}
   *         otherwise.
   */
  private boolean removeMappingElement(ArrayList<int[]> m, int[] e) {
    for (int[] me : m) {
      if (me[0] == e[0] && me[1] == e[1]) {
        m.remove(me);
        return true;
      }
    }
    return false;
  }
}

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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.crawljax.stateabstractions.dom.apted.costmodel.CostModel;
import com.crawljax.stateabstractions.dom.apted.node.AptedNode;
import com.crawljax.stateabstractions.dom.apted.node.NodeIndexer;

/**
 * Implements APTED algorithm [1,2].
 *
 * <ul>
 * <li>Optimal strategy with all paths.
 * <li>Single-node single path function supports currently only unit cost.
 * <li>Two-node single path function not included.
 * <li>\Delta^L and \Delta^R based on Zhang and Shasha's algorithm for executing
 *     left and right paths (as in [3]). If only left and right paths are used
 *     in the strategy, the memory usage is reduced by one quadratic array.
 * <li>For any other path \Delta^A from [1] is used.
 * </ul>
 *
 * References:
 * <ul>
 * <li>[1] M. Pawlik and N. Augsten. Efficient Computation of the Tree Edit
 *      Distance. ACM Transactions on Database Systems (TODS) 40(1). 2015.
 * <li>[2] M. Pawlik and N. Augsten. Tree edit distance: Robust and memory-
 *      efficient. Information Systems 56. 2016.
 * <li>[3] M. Pawlik and N. Augsten. RTED: A Robust Algorithm for the Tree Edit
 *      Distance. PVLDB 5(4). 2011.
 * </ul>
 *
 * @param <C> type of cost model.
 * @param <D> type of node data.
 */
public class APTED<C extends CostModel, D> {

  /**
   * Identifier of left path type = {@value LEFT};
   */
  private static final byte LEFT = 0;

  /**
   * Identifier of right path type = {@value RIGHT};
   */
  private static final byte RIGHT = 1;

  /**
   * Identifier of inner path type = {@value INNER};
   */
  private static final byte INNER = 2;

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
   * The distance matrix [1, Sections 3.4,8.2,8.3]. Used to store intermediate
   * distances between pairs of subtrees.
   */
  private float delta[][];

  /**
   * One of distance arrays to store intermediate distances in spfA.
   */
  // TODO: Verify if other spf-local arrays are initialised within spf. If yes,
  //       move q to spf to - then, an offset has to be used to access it.
  private float q[];

  /**
   * Array used in the algorithm before [1]. Using it does not change the
   * complexity.
   *
   * <p>TODO: Do not use it [1, Section 8.4].
   */
  private int fn[];

  /**
   * Array used in the algorithm before [1]. Using it does not change the
   * complexity.
   *
   * <p>TODO: Do not use it [1, Section 8.4].
   */
  private int ft[];

  /**
   * Stores the number of subproblems encountered while computing the distance
   * [1, Section 10].
   */
  private long counter;

  /**
   * Cost model to be used for calculating costs of edit operations.
   */
  private C costModel;

  /**
   * Constructs the APTED algorithm object with the specified cost model.
   *
   * @param costModel cost model for edit operations.
   */
  public APTED(C costModel) {
    this.costModel = costModel;
  }

  /**
   * Compute tree edit distance between source and destination trees using
   * APTED algorithm [1,2].
   *
   * @param t1 source tree.
   * @param t2 destination tree.
   * @return tree edit distance.
   */
  public float computeEditDistance(AptedNode<D> t1, AptedNode<D> t2) {
    // Index the nodes of both input trees.
    init(t1, t2);
    // Determine the optimal strategy for the distance computation.
    // Use the heuristic from [2, Section 5.3].
    if (it1.lchl < it1.rchl) {
      delta = computeOptStrategy_postL(it1, it2);
    } else {
      delta = computeOptStrategy_postR(it1, it2);
    }
    // Initialise structures for distance computation.
    tedInit();
    // Compute the distance.
    return gted(it1, it2);
  }

  /**
   * This method is only for testing purspose. It computes TED with a fixed
   * path type in the strategy to trigger execution of a specific single-path
   * function.
   *
   * @param t1 source tree.
   * @param t2 destination tree.
   * @param spfType single-path function to trigger (LEFT or RIGHT).
   * @return tree edit distance.
   */
  public float computeEditDistance_spfTest(AptedNode<D> t1, AptedNode<D> t2, int spfType) {
    // Index the nodes of both input trees.
    init(t1, t2);
    // Initialise delta array.
    delta = new float[size1][size2];
    // Fix a path type to trigger specific spf.
    for (int i = 0; i < delta.length; i++) {
      for (int j = 0; j < delta[i].length; j++) {
        // Fix path type.
        if (spfType == LEFT) {
          delta[i][j] = it1.preL_to_lld(i) + 1;
        } else if (spfType == RIGHT) {
          delta[i][j] = it1.preL_to_rld(i) + 1;
        }
      }
    }
    // Initialise structures for distance computation.
    tedInit();
    // Compute the distance.
    return gted(it1, it2);
  }

  /**
   * Initialises node indexers and stores input tree sizes.
   *
   * @param t1 source input tree.
   * @param t2 destination input tree.
   */
  public void init(AptedNode<D> t1, AptedNode<D> t2) {
    it1 = new NodeIndexer(t1, costModel);
    it2 = new NodeIndexer(t2, costModel);
    size1 = it1.getSize();
    size2 = it2.getSize();
  }

  /**
   * After the optimal strategy is computed, initialises distances of deleting
   * and inserting subtrees without their root nodes.
   */
  private void tedInit() {
    // Reset the subproblems counter.
    counter = 0L;
    // Initialize arrays.
    int maxSize = Math.max(size1, size2) + 1;
    // TODO: Move q initialisation to spfA.
    q = new float[maxSize];
    // TODO: Do not use fn and ft arrays [1, Section 8.4].
    fn = new int[maxSize + 1];
    ft = new int[maxSize + 1];
    // Compute subtree distances without the root nodes when one of subtrees
    // is a single node.
    int sizeX = -1;
    int sizeY = -1;
    int parentX = -1;
    int parentY = -1;
    // Loop over the nodes in reversed left-to-right preorder.
    for(int x = 0; x < size1; x++) {
      sizeX = it1.sizes[x];
      parentX = it1.parents[x];
      for(int y = 0; y < size2; y++) {
        sizeY = it2.sizes[y];
        parentY = it2.parents[y];
        // Set values in delta based on the sums of deletion and insertion
        // costs. Substract the costs for root nodes.
        // In this method we don't have to verify the order of the input trees
        // because it is equal to the original.
        if (sizeX == 1 && sizeY == 1) {
          delta[x][y] = 0.0f;
        } else if (sizeX == 1) {
          delta[x][y] = it2.preL_to_sumInsCost[y] - costModel.ins(it2.preL_to_node[y]); // USE COST MODEL.
        } else if (sizeY == 1) {
          delta[x][y] = it1.preL_to_sumDelCost[x] - costModel.del(it1.preL_to_node[x]); // USE COST MODEL.
        }
      }
    }
  }

  /**
   * Compute the optimal strategy using left-to-right postorder traversal of
   * the nodes [2, Algorithm 1].
   *
   * @param it1 node indexer of the source input tree.
   * @param it2 node indexer of the destination input tree.
   * @return array with the optimal strategy.
   */
  // TODO: Document the internals. Point to lines of the lagorithm.
  public float[][] computeOptStrategy_postL(NodeIndexer it1, NodeIndexer it2) {

    int size1 = it1.getSize();
    int size2 = it2.getSize();
    float strategy[][] = new float[size1][size2];
    float cost1_L[][] = new float[size1][];
    float cost1_R[][] = new float[size1][];
    float cost1_I[][] = new float[size1][];
    float cost2_L[] = new float[size2];
    float cost2_R[] = new float[size2];
    float cost2_I[] = new float[size2];
    int cost2_path[] = new int[size2];
    float leafRow[] = new float[size2];
    int pathIDOffset = size1;
    float minCost = 0x7fffffffffffffffL;
    int strategyPath = -1;

    int[] pre2size1 = it1.sizes;
    int[] pre2size2 = it2.sizes;
    int[] pre2descSum1 = it1.preL_to_desc_sum;
    int[] pre2descSum2 = it2.preL_to_desc_sum;
    int[] pre2krSum1 = it1.preL_to_kr_sum;
    int[] pre2krSum2 = it2.preL_to_kr_sum;
    int[] pre2revkrSum1 = it1.preL_to_rev_kr_sum;
    int[] pre2revkrSum2 = it2.preL_to_rev_kr_sum;
    int[] preL_to_preR_1 = it1.preL_to_preR;
    int[] preL_to_preR_2 = it2.preL_to_preR;
    int[] preR_to_preL_1 = it1.preR_to_preL;
    int[] preR_to_preL_2 = it2.preR_to_preL;
    int[] pre2parent1 = it1.parents;
    int[] pre2parent2 = it2.parents;
    boolean[] nodeType_L_1 = it1.nodeType_L;
    boolean[] nodeType_L_2 = it2.nodeType_L;
    boolean[] nodeType_R_1 = it1.nodeType_R;
    boolean[] nodeType_R_2 = it2.nodeType_R;

    int[] preL_to_postL_1 = it1.preL_to_postL;
    int[] preL_to_postL_2 = it2.preL_to_postL;

    int[] postL_to_preL_1 = it1.postL_to_preL;
    int[] postL_to_preL_2 = it2.postL_to_preL;

    int size_v, parent_v_preL, parent_w_preL, parent_w_postL = -1, size_w, parent_v_postL = -1;
    int leftPath_v, rightPath_v;
    float[] cost_Lpointer_v, cost_Rpointer_v, cost_Ipointer_v;
    float[] strategypointer_v;
    float[] cost_Lpointer_parent_v = null, cost_Rpointer_parent_v = null, cost_Ipointer_parent_v = null;
    float[] strategypointer_parent_v = null;
    int krSum_v, revkrSum_v, descSum_v;
    boolean is_v_leaf;

    int v_in_preL;
    int w_in_preL;

    Stack<float[]> rowsToReuse_L = new Stack<float[]>();
    Stack<float[]> rowsToReuse_R = new Stack<float[]>();
    Stack<float[]> rowsToReuse_I = new Stack<float[]>();

    for(int v = 0; v < size1; v++) {
      v_in_preL = postL_to_preL_1[v];

      is_v_leaf = it1.isLeaf(v_in_preL);
      parent_v_preL = pre2parent1[v_in_preL];

      if (parent_v_preL != -1) {
        parent_v_postL = preL_to_postL_1[parent_v_preL];
      }

      strategypointer_v = strategy[v_in_preL];

      size_v = pre2size1[v_in_preL];
      leftPath_v = -(preR_to_preL_1[preL_to_preR_1[v_in_preL] + size_v - 1] + 1);// this is the left path's ID which is the leftmost leaf node: l-r_preorder(r-l_preorder(v) + |Fv| - 1)
      rightPath_v = v_in_preL + size_v - 1 + 1; // this is the right path's ID which is the rightmost leaf node: l-r_preorder(v) + |Fv| - 1
      krSum_v = pre2krSum1[v_in_preL];
      revkrSum_v = pre2revkrSum1[v_in_preL];
      descSum_v = pre2descSum1[v_in_preL];

      if(is_v_leaf) {
        cost1_L[v] = leafRow;
        cost1_R[v] = leafRow;
        cost1_I[v] = leafRow;
        for(int i = 0; i < size2; i++) {
          strategypointer_v[postL_to_preL_2[i]] = v_in_preL;
        }
      }

      cost_Lpointer_v = cost1_L[v];
      cost_Rpointer_v = cost1_R[v];
      cost_Ipointer_v = cost1_I[v];

      if(parent_v_preL != -1 && cost1_L[parent_v_postL] == null) {
        if (rowsToReuse_L.isEmpty()) {
          cost1_L[parent_v_postL] = new float[size2];
          cost1_R[parent_v_postL] = new float[size2];
          cost1_I[parent_v_postL] = new float[size2];
        } else {
          cost1_L[parent_v_postL] = rowsToReuse_L.pop();
          cost1_R[parent_v_postL] = rowsToReuse_R.pop();
          cost1_I[parent_v_postL] = rowsToReuse_I.pop();
        }
      }

      if (parent_v_preL != -1) {
        cost_Lpointer_parent_v = cost1_L[parent_v_postL];
        cost_Rpointer_parent_v = cost1_R[parent_v_postL];
        cost_Ipointer_parent_v = cost1_I[parent_v_postL];
        strategypointer_parent_v = strategy[parent_v_preL];
      }

      Arrays.fill(cost2_L, 0L);
      Arrays.fill(cost2_R, 0L);
      Arrays.fill(cost2_I, 0L);
      Arrays.fill(cost2_path, 0);

      for(int w = 0; w < size2; w++) {
        w_in_preL = postL_to_preL_2[w];

        parent_w_preL = pre2parent2[w_in_preL];
        if (parent_w_preL != -1) {
          parent_w_postL = preL_to_postL_2[parent_w_preL];
        }

        size_w = pre2size2[w_in_preL];
        if (it2.isLeaf(w_in_preL)) {
          cost2_L[w] = 0L;
          cost2_R[w] = 0L;
          cost2_I[w] = 0L;
          cost2_path[w] = w_in_preL;
        }
        minCost = 0x7fffffffffffffffL;
        strategyPath = -1;
        float tmpCost = 0x7fffffffffffffffL;

        if (size_v <= 1 || size_w <= 1) { // USE NEW SINGLE_PATH FUNCTIONS FOR SMALL SUBTREES
          minCost = Math.max(size_v, size_w);
        } else {
          tmpCost = (float) size_v * (float) pre2krSum2[w_in_preL] + cost_Lpointer_v[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = leftPath_v;
          }
          tmpCost = (float) size_v * (float) pre2revkrSum2[w_in_preL] + cost_Rpointer_v[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = rightPath_v;
          }
          tmpCost = (float) size_v * (float) pre2descSum2[w_in_preL] + cost_Ipointer_v[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = (int)strategypointer_v[w_in_preL] + 1;
          }
          tmpCost = (float) size_w * (float) krSum_v + cost2_L[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = -(preR_to_preL_2[preL_to_preR_2[w_in_preL] + size_w - 1] + pathIDOffset + 1);
          }
          tmpCost = (float) size_w * (float) revkrSum_v + cost2_R[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = w_in_preL + size_w - 1 + pathIDOffset + 1;
          }
          tmpCost = (float) size_w * (float) descSum_v + cost2_I[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = cost2_path[w] + pathIDOffset + 1;
          }
        }

        if (parent_v_preL != -1) {
          cost_Rpointer_parent_v[w] += minCost;
          tmpCost = -minCost + cost1_I[v][w];
          if (tmpCost < cost1_I[parent_v_postL][w]) {
            cost_Ipointer_parent_v[w] = tmpCost;
            strategypointer_parent_v[w_in_preL] = strategypointer_v[w_in_preL];
          }
          if (nodeType_R_1[v_in_preL]) {
            cost_Ipointer_parent_v[w] += cost_Rpointer_parent_v[w];
            cost_Rpointer_parent_v[w] += cost_Rpointer_v[w] - minCost;
          }
          if (nodeType_L_1[v_in_preL]) {
            cost_Lpointer_parent_v[w] += cost_Lpointer_v[w];
          } else {
            cost_Lpointer_parent_v[w] += minCost;
          }
        }
        if (parent_w_preL != -1) {
          cost2_R[parent_w_postL] += minCost;
          tmpCost = -minCost + cost2_I[w];
          if (tmpCost < cost2_I[parent_w_postL]) {
            cost2_I[parent_w_postL] = tmpCost;
            cost2_path[parent_w_postL] = cost2_path[w];
          }
          if (nodeType_R_2[w_in_preL]) {
            cost2_I[parent_w_postL] += cost2_R[parent_w_postL];
            cost2_R[parent_w_postL] += cost2_R[w] - minCost;
          }
          if (nodeType_L_2[w_in_preL]) {
            cost2_L[parent_w_postL] += cost2_L[w];
          } else {
            cost2_L[parent_w_postL] += minCost;
          }
        }
        strategypointer_v[w_in_preL] = strategyPath;
      }

      if (!it1.isLeaf(v_in_preL)) {
        Arrays.fill(cost1_L[v], 0);
        Arrays.fill(cost1_R[v], 0);
        Arrays.fill(cost1_I[v], 0);
        rowsToReuse_L.push(cost1_L[v]);
        rowsToReuse_R.push(cost1_R[v]);
        rowsToReuse_I.push(cost1_I[v]);
      }

    }
    return strategy;
  }

  /**
   * Compute the optimal strategy using right-to-left postorder traversal of
   * the nodes [2, Algorithm 1].
   *
   * @param it1 node indexer of the source input tree.
   * @param it2 node indexer of the destination input tree.
   * @return array with the optimal strategy.
   */
  // QUESTION: Is it possible to merge it with the other strategy computation?
  // TODO: Document the internals. Point to lines of the lagorithm.
  public float[][] computeOptStrategy_postR(NodeIndexer it1, NodeIndexer it2) {
    int size1 = it1.getSize();
    int size2 = it2.getSize();
    float strategy[][] = new float[size1][size2];
    float cost1_L[][] = new float[size1][];
    float cost1_R[][] = new float[size1][];
    float cost1_I[][] = new float[size1][];
    float cost2_L[] = new float[size2];
    float cost2_R[] = new float[size2];
    float cost2_I[] = new float[size2];
    int cost2_path[] = new int[size2];
    float leafRow[] = new float[size2];
    int pathIDOffset = size1;
    float minCost = 0x7fffffffffffffffL;
    int strategyPath = -1;

    int[] pre2size1 = it1.sizes;
    int[] pre2size2 = it2.sizes;
    int[] pre2descSum1 = it1.preL_to_desc_sum;
    int[] pre2descSum2 = it2.preL_to_desc_sum;
    int[] pre2krSum1 = it1.preL_to_kr_sum;
    int[] pre2krSum2 = it2.preL_to_kr_sum;
    int[] pre2revkrSum1 = it1.preL_to_rev_kr_sum;
    int[] pre2revkrSum2 = it2.preL_to_rev_kr_sum;
    int[] preL_to_preR_1 = it1.preL_to_preR;
    int[] preL_to_preR_2 = it2.preL_to_preR;
    int[] preR_to_preL_1 = it1.preR_to_preL;
    int[] preR_to_preL_2 = it2.preR_to_preL;
    int[] pre2parent1 = it1.parents;
    int[] pre2parent2 = it2.parents;
    boolean[] nodeType_L_1 = it1.nodeType_L;
    boolean[] nodeType_L_2 = it2.nodeType_L;
    boolean[] nodeType_R_1 = it1.nodeType_R;
    boolean[] nodeType_R_2 = it2.nodeType_R;

    int size_v, parent_v, parent_w, size_w;
    int leftPath_v, rightPath_v;
    float[] cost_Lpointer_v, cost_Rpointer_v, cost_Ipointer_v;
    float[] strategypointer_v;
    float[] cost_Lpointer_parent_v = null, cost_Rpointer_parent_v = null, cost_Ipointer_parent_v = null;
    float[] strategypointer_parent_v = null;
    int krSum_v, revkrSum_v, descSum_v;
    boolean is_v_leaf;

    Stack<float[]> rowsToReuse_L = new Stack<float[]>();
    Stack<float[]> rowsToReuse_R = new Stack<float[]>();
    Stack<float[]> rowsToReuse_I = new Stack<float[]>();

    for(int v = size1 - 1; v >= 0; v--) {
      is_v_leaf = it1.isLeaf(v);
      parent_v = pre2parent1[v];

      strategypointer_v = strategy[v];

      size_v = pre2size1[v];
      leftPath_v = -(preR_to_preL_1[preL_to_preR_1[v] + pre2size1[v] - 1] + 1);// this is the left path's ID which is the leftmost leaf node: l-r_preorder(r-l_preorder(v) + |Fv| - 1)
      rightPath_v = v + pre2size1[v] - 1 + 1; // this is the right path's ID which is the rightmost leaf node: l-r_preorder(v) + |Fv| - 1
      krSum_v = pre2krSum1[v];
      revkrSum_v = pre2revkrSum1[v];
      descSum_v = pre2descSum1[v];

      if (is_v_leaf) {
        cost1_L[v] = leafRow;
        cost1_R[v] = leafRow;
        cost1_I[v] = leafRow;
        for (int i = 0; i < size2; i++) {
          strategypointer_v[i] = v;
        }
      }

      cost_Lpointer_v = cost1_L[v];
      cost_Rpointer_v = cost1_R[v];
      cost_Ipointer_v = cost1_I[v];

      if (parent_v != -1 && cost1_L[parent_v] == null) {
        if (rowsToReuse_L.isEmpty()) {
          cost1_L[parent_v] = new float[size2];
          cost1_R[parent_v] = new float[size2];
          cost1_I[parent_v] = new float[size2];
        } else {
          cost1_L[parent_v] = rowsToReuse_L.pop();
          cost1_R[parent_v] = rowsToReuse_R.pop();
          cost1_I[parent_v] = rowsToReuse_I.pop();
        }
      }

      if (parent_v != -1) {
        cost_Lpointer_parent_v = cost1_L[parent_v];
        cost_Rpointer_parent_v = cost1_R[parent_v];
        cost_Ipointer_parent_v = cost1_I[parent_v];
        strategypointer_parent_v = strategy[parent_v];
      }

      Arrays.fill(cost2_L, 0L);
      Arrays.fill(cost2_R, 0L);
      Arrays.fill(cost2_I, 0L);
      Arrays.fill(cost2_path, 0);
      for (int w = size2 - 1; w >= 0; w--) {
        size_w = pre2size2[w];
        if (it2.isLeaf(w)) {
          cost2_L[w] = 0L;
          cost2_R[w] = 0L;
          cost2_I[w] = 0L;
          cost2_path[w] = w;
        }
        minCost = 0x7fffffffffffffffL;
        strategyPath = -1;
        float tmpCost = 0x7fffffffffffffffL;

        if (size_v <= 1 || size_w <= 1) { // USE NEW SINGLE_PATH FUNCTIONS FOR SMALL SUBTREES
        	minCost = Math.max(size_v, size_w);
        } else {
          tmpCost = (float) size_v * (float) pre2krSum2[w] + cost_Lpointer_v[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = leftPath_v;
          }
          tmpCost = (float) size_v * (float) pre2revkrSum2[w] + cost_Rpointer_v[w];
          if (tmpCost < minCost){
            minCost = tmpCost;
            strategyPath = rightPath_v;
          }
          tmpCost = (float) size_v * (float) pre2descSum2[w] + cost_Ipointer_v[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = (int)strategypointer_v[w] + 1;
          }
          tmpCost = (float) size_w * (float) krSum_v + cost2_L[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = -(preR_to_preL_2[preL_to_preR_2[w] + size_w - 1] + pathIDOffset + 1);
          }
          tmpCost = (float) size_w * (float) revkrSum_v + cost2_R[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = w + size_w - 1 + pathIDOffset + 1;
          }
          tmpCost = (float) size_w * (float) descSum_v + cost2_I[w];
          if (tmpCost < minCost) {
            minCost = tmpCost;
            strategyPath = cost2_path[w] + pathIDOffset + 1;
          }
        }

        if (parent_v != -1) {
          cost_Lpointer_parent_v[w] += minCost;
          tmpCost = -minCost + cost1_I[v][w];
          if (tmpCost < cost1_I[parent_v][w]) {
            cost_Ipointer_parent_v[w] = tmpCost;
            strategypointer_parent_v[w] = strategypointer_v[w];
          }
          if (nodeType_L_1[v]) {
            cost_Ipointer_parent_v[w] += cost_Lpointer_parent_v[w];
            cost_Lpointer_parent_v[w] += cost_Lpointer_v[w] - minCost;
          }
          if (nodeType_R_1[v]) {
              cost_Rpointer_parent_v[w] += cost_Rpointer_v[w];
          } else {
            cost_Rpointer_parent_v[w] += minCost;
          }
        }
        parent_w = pre2parent2[w];
        if (parent_w != -1) {
          cost2_L[parent_w] += minCost;
          tmpCost = -minCost + cost2_I[w];
          if (tmpCost < cost2_I[parent_w]) {
            cost2_I[parent_w] = tmpCost;
            cost2_path[parent_w] = cost2_path[w];
          }
          if (nodeType_L_2[w]) {
            cost2_I[parent_w] += cost2_L[parent_w];
            cost2_L[parent_w] += cost2_L[w] - minCost;
          }
          if (nodeType_R_2[w]) {
              cost2_R[parent_w] += cost2_R[w];
          } else {
            cost2_R[parent_w] += minCost;
          }
        }
        strategypointer_v[w] = strategyPath;
      }

      if (!it1.isLeaf(v)) {
        Arrays.fill(cost1_L[v], 0);
        Arrays.fill(cost1_R[v], 0);
        Arrays.fill(cost1_I[v], 0);
        rowsToReuse_L.push(cost1_L[v]);
        rowsToReuse_R.push(cost1_R[v]);
        rowsToReuse_I.push(cost1_I[v]);
        }
      }
      return strategy;
  }

  /**
   * Implements spf1 single path function for the case when one of the subtrees
   * is a single node [2, Section 6.1, Algorithm 2].
   *
   * <p>We allow an arbitrary cost model which in principle may allow renames to
   * have a lower cost than the respective deletion plus insertion. Thus,
   * Formula 4 in [2] has to be modified to account for that case.
   *
   * <p>In this method we don't have to verify if input subtrees have been
   * swapped because they're always passed in the original input order.
   *
   * @param ni1 node indexer for the source input subtree.
   * @param ni2 node indexer for the destination input subtree.
   * @param subtreeRootNode1 root node of a subtree in the source input tree.
   * @param subtreeRootNode2 root node of a subtree in the destination input tree.
   * @return the tree edit distance between two subtrees of the source and destination input subtrees.
   */
  // TODO: Merge the initialisation loop in tedInit with this method.
  //       Currently, spf1 doesn't have to store distances in delta, because
  //       all of them have been stored in tedInit.
  private float spf1 (NodeIndexer ni1, int subtreeRootNode1, NodeIndexer ni2, int subtreeRootNode2) {
    int subtreeSize1 = ni1.sizes[subtreeRootNode1];
    int subtreeSize2 = ni2.sizes[subtreeRootNode2];
    if (subtreeSize1 == 1 && subtreeSize2 == 1) {
      AptedNode<D> n1 = ni1.preL_to_node[subtreeRootNode1];
      AptedNode<D> n2 = ni2.preL_to_node[subtreeRootNode2];
      float maxCost = costModel.del(n1) + costModel.ins(n2);
      float renCost = costModel.ren(n1, n2);
      return renCost < maxCost ? renCost : maxCost;
    }
    if (subtreeSize1 == 1) {
      AptedNode<D> n1 = ni1.preL_to_node[subtreeRootNode1];
      AptedNode<D> n2 = null;
      float cost = ni2.preL_to_sumInsCost[subtreeRootNode2];
      float maxCost = cost + costModel.del(n1);
      float minRenMinusIns = cost;
      float nodeRenMinusIns = 0;
      for (int i = subtreeRootNode2; i < subtreeRootNode2 + subtreeSize2; i++) {
        n2 = ni2.preL_to_node[i];
        nodeRenMinusIns = costModel.ren(n1, n2) - costModel.ins(n2);
        if (nodeRenMinusIns < minRenMinusIns) {
          minRenMinusIns = nodeRenMinusIns;
        }
      }
      cost += minRenMinusIns;
      return cost < maxCost ? cost : maxCost;
    }
    if (subtreeSize2 == 1) {
      AptedNode<D> n1 = null;
      AptedNode<D> n2 = ni2.preL_to_node[subtreeRootNode2];
      float cost = ni1.preL_to_sumDelCost[subtreeRootNode1];
      float maxCost = cost + costModel.ins(n2);
      float minRenMinusDel = cost;
      float nodeRenMinusDel = 0;
      for (int i = subtreeRootNode1; i < subtreeRootNode1 + subtreeSize1; i++) {
        n1 = ni1.preL_to_node[i];
        nodeRenMinusDel = costModel.ren(n1, n2) - costModel.del(n1);
        if (nodeRenMinusDel < minRenMinusDel) {
          minRenMinusDel = nodeRenMinusDel;
        }
      }
      cost += minRenMinusDel;
      return cost < maxCost ? cost : maxCost;
    }
    return -1;
  }

  /**
   * Implements GTED algorithm [1, Section 3.4].
   *
   * @param it1 node indexer for the source input tree.
   * @param it2 node indexer for the destination input tree.
   * @return the tree edit distance between the source and destination trees.
   */
  // TODO: Document the internals. Point to lines of the algorithm.
  private float gted(NodeIndexer it1, NodeIndexer it2) {
    int currentSubtree1 = it1.getCurrentNode();
    int currentSubtree2 = it2.getCurrentNode();
    int subtreeSize1 = it1.sizes[currentSubtree1];
    int subtreeSize2 = it2.sizes[currentSubtree2];

    // Use spf1.
    if ((subtreeSize1 == 1 || subtreeSize2 == 1)) {
      return spf1(it1, currentSubtree1, it2, currentSubtree2);
    }

    int strategyPathID = (int)delta[currentSubtree1][currentSubtree2];

    byte strategyPathType = -1;
    int currentPathNode = Math.abs(strategyPathID) - 1;
    int pathIDOffset = it1.getSize();

    int parent = -1;
    if(currentPathNode < pathIDOffset) {
      strategyPathType = getStrategyPathType(strategyPathID, pathIDOffset, it1, currentSubtree1, subtreeSize1);
      while((parent = it1.parents[currentPathNode]) >= currentSubtree1) {
        int ai[];
        int k = (ai = it1.children[parent]).length;
        for(int i = 0; i < k; i++) {
          int child = ai[i];
          if(child != currentPathNode) {
            it1.setCurrentNode(child);
            gted(it1, it2);
          }
        }
        currentPathNode = parent;
      }
      // TODO: Move this property away from node indexer and pass directly to spfs.
      it1.setCurrentNode(currentSubtree1);

      // Pass to spfs a boolean that says says if the order of input subtrees
      // has been swapped compared to the order of the initial input trees.
      // Used for accessing delta array and deciding on the edit operation
      // [1, Section 3.4].
      if (strategyPathType == 0) {
        return spfL(it1, it2, false);
      }
      if (strategyPathType == 1) {
        return spfR(it1, it2, false);
      }
      return spfA(it1, it2, Math.abs(strategyPathID) - 1, strategyPathType, false);
    }

    currentPathNode -= pathIDOffset;
    strategyPathType = getStrategyPathType(strategyPathID, pathIDOffset, it2, currentSubtree2, subtreeSize2);
    while((parent = it2.parents[currentPathNode]) >= currentSubtree2) {
      int ai1[];
      int l = (ai1 = it2.children[parent]).length;
      for(int j = 0; j < l; j++) {
        int child = ai1[j];
        if(child != currentPathNode) {
          it2.setCurrentNode(child);
          gted(it1, it2);
        }
      }
      currentPathNode = parent;
    }
    // TODO: Move this property away from node indexer and pass directly to spfs.
    it2.setCurrentNode(currentSubtree2);

    // Pass to spfs a boolean that says says if the order of input subtrees
    // has been swapped compared to the order of the initial input trees. Used
    // for accessing delta array and deciding on the edit operation
    // [1, Section 3.4].
    if (strategyPathType == 0) {
      return spfL(it2, it1, true);
    }
    if (strategyPathType == 1) {
      return spfR(it2, it1, true);
    }
    return spfA(it2, it1, Math.abs(strategyPathID) - pathIDOffset - 1, strategyPathType, true);
  }

  /**
   * Implements the single-path function spfA. Here, we use it strictly for
   * inner paths (spfL and spfR have better performance for leaft and right
   * paths, respectively) [1, Sections 7 and 8]. However, in this stage it
   * also executes correctly for left and right paths.
   *
   * @param it1 node indexer of the left-hand input subtree.
   * @param it2 node indexer of the right-hand input subtree.
   * @param pathID the left-to-right preorder id of the strategy path's leaf node.
   * @param pathType type of the strategy path (LEFT, RIGHT, INNER).
   * @param treesSwapped says if the order of input subtrees has been swapped
   *                     compared to the order of the initial input trees. Used
   *                     for accessing delta array and deciding on the edit
   *                     operation.
   * @return tree edit distance between left-hand and right-hand input subtrees.
   */
  // TODO: Document the internals. Point to lines of the algorithm.
  // The implementation has been micro-tuned: variables initialised once,
  // pointers to arrays precomputed and fixed for entire lower-level loops,
  // parts of lower-level loops that don't change moved to upper-level loops.
  private float spfA(NodeIndexer it1, NodeIndexer it2, int pathID, byte pathType, boolean treesSwapped) {
    AptedNode<D>[] it2nodes = it2.preL_to_node;
    AptedNode<D> lFNode;
    int[] it1sizes = it1.sizes;
    int[] it2sizes = it2.sizes;
    int[] it1parents = it1.parents;
    int[] it2parents = it2.parents;
    int[] it1preL_to_preR = it1.preL_to_preR;
    int[] it2preL_to_preR = it2.preL_to_preR;
    int[] it1preR_to_preL = it1.preR_to_preL;
    int[] it2preR_to_preL = it2.preR_to_preL;
    int currentSubtreePreL1 = it1.getCurrentNode();
    int currentSubtreePreL2 = it2.getCurrentNode();

    // Variables to incrementally sum up the forest sizes.
    int currentForestSize1 = 0;
    int currentForestSize2 = 0;
    int tmpForestSize1 = 0;
    // Variables to incrementally sum up the forest cost.
    float currentForestCost1 = 0;
    float currentForestCost2 = 0;
    float tmpForestCost1 = 0;

    int subtreeSize2 = it2.sizes[currentSubtreePreL2];
    int subtreeSize1 = it1.sizes[currentSubtreePreL1];
    float[][] t = new float[subtreeSize2+1][subtreeSize2+1];
    float[][] s = new float[subtreeSize1+1][subtreeSize2+1];
    float minCost = -1;
    // sp1, sp2 and sp3 correspond to three elements of the minimum in the
    // recursive formula [1, Figure 12].
    float sp1 = 0;
    float sp2 = 0;
    float sp3 = 0;
    int startPathNode = -1;
    int endPathNode = pathID;
    int it1PreLoff = endPathNode;
    int it2PreLoff = currentSubtreePreL2;
    int it1PreRoff = it1preL_to_preR[endPathNode];
    int it2PreRoff = it2preL_to_preR[it2PreLoff];
    // variable declarations which were inside the loops
    int rFlast,lFlast,endPathNode_in_preR,startPathNode_in_preR,parent_of_endPathNode,parent_of_endPathNode_in_preR,
    lFfirst,rFfirst,rGlast,rGfirst,lGfirst,rG_in_preL,rGminus1_in_preL,parent_of_rG_in_preL,lGlast,lF_in_preR,lFSubtreeSize,
    lGminus1_in_preR,parent_of_lG,parent_of_lG_in_preR,rF_in_preL,rFSubtreeSize,
    rGfirst_in_preL;
    boolean leftPart,rightPart,fForestIsTree,lFIsConsecutiveNodeOfCurrentPathNode,lFIsLeftSiblingOfCurrentPathNode,
    rFIsConsecutiveNodeOfCurrentPathNode,rFIsRightSiblingOfCurrentPathNode;
    float[] sp1spointer,sp2spointer,sp3spointer,sp3deltapointer,swritepointer,sp1tpointer,sp3tpointer;
    // These variables store the id of the source (which array) of looking up
    // elements of the minimum in the recursive formula [1, Figures 12,13].
    byte sp1source,sp3source;
    // Loop A [1, Algorithm 3] - walk up the path.
    while (endPathNode >= currentSubtreePreL1) {
      it1PreLoff = endPathNode;
      it1PreRoff = it1preL_to_preR[endPathNode];
      rFlast = -1;
      lFlast = -1;
      endPathNode_in_preR = it1preL_to_preR[endPathNode];
      startPathNode_in_preR = startPathNode == -1 ? 0x7fffffff : it1preL_to_preR[startPathNode];
      parent_of_endPathNode = it1parents[endPathNode];
      parent_of_endPathNode_in_preR = parent_of_endPathNode == -1 ? 0x7fffffff : it1preL_to_preR[parent_of_endPathNode];
      if (startPathNode - endPathNode > 1) {
        leftPart = true;
      } else {
        leftPart = false;
      }
      if (startPathNode >= 0 && startPathNode_in_preR - endPathNode_in_preR > 1) {
        rightPart = true;
      } else {
        rightPart = false;
      }
      // Deal with nodes to the left of the path.
      if (pathType == 1 || pathType == 2 && leftPart) {
        if (startPathNode == -1) {
          rFfirst = endPathNode_in_preR;
          lFfirst = endPathNode;
        } else {
          rFfirst = startPathNode_in_preR;
          lFfirst = startPathNode - 1;
        }
        if (!rightPart) {
          rFlast = endPathNode_in_preR;
        }
        rGlast = it2preL_to_preR[currentSubtreePreL2];
        rGfirst = (rGlast + subtreeSize2) - 1;
        lFlast = rightPart ? endPathNode + 1 : endPathNode;
        fn[fn.length - 1] = -1;
        for (int i = currentSubtreePreL2; i < currentSubtreePreL2 + subtreeSize2; i++) {
            fn[i] = -1;
            ft[i] = -1;
        }
        // Store the current size and cost of forest in F.
        tmpForestSize1 = currentForestSize1;
        tmpForestCost1 = currentForestCost1;
        // Loop B [1, Algoritm 3] - for all nodes in G (right-hand input tree).
        for (int rG = rGfirst; rG >= rGlast; rG--) {
          lGfirst = it2preR_to_preL[rG];
          rG_in_preL = it2preR_to_preL[rG];
          rGminus1_in_preL = rG <= it2preL_to_preR[currentSubtreePreL2] ? 0x7fffffff : it2preR_to_preL[rG - 1];
          parent_of_rG_in_preL = it2parents[rG_in_preL];
          // This if statement decides on the last lG node for Loop D [1, Algorithm 3];
          if (pathType == 1){
            if (lGfirst == currentSubtreePreL2 || rGminus1_in_preL != parent_of_rG_in_preL) {
              lGlast = lGfirst;
            } else {
              lGlast = it2parents[lGfirst]+1;
            }
          } else {
            lGlast = lGfirst == currentSubtreePreL2 ? lGfirst : currentSubtreePreL2+1;
          }
          updateFnArray(it2.preL_to_ln[lGfirst], lGfirst, currentSubtreePreL2);
          updateFtArray(it2.preL_to_ln[lGfirst], lGfirst);
          int rF = rFfirst;
          // Reset size and cost of the forest in F.
          currentForestSize1 = tmpForestSize1;
          currentForestCost1 = tmpForestCost1;
          // Loop C [1, Algorithm 3] - for all nodes to the left of the path node.
          for (int lF = lFfirst; lF >= lFlast; lF--) {
            // This if statement fixes rF node.
            if (lF == lFlast && !rightPart) {
              rF = rFlast;
            }
            lFNode = it1.preL_to_node[lF];
            // Increment size and cost of F forest by node lF.
            currentForestSize1++;
            currentForestCost1 += (treesSwapped ? costModel.ins(lFNode) : costModel.del(lFNode)); // USE COST MODEL - sum up deletion cost of a forest.
            // Reset size and cost of forest in G to subtree G_lGfirst.
            currentForestSize2 = it2sizes[lGfirst];
            currentForestCost2 = (treesSwapped ? it2.preL_to_sumDelCost[lGfirst] : it2.preL_to_sumInsCost[lGfirst]); // USE COST MODEL - reset to subtree insertion cost.
            lF_in_preR = it1preL_to_preR[lF];
            fForestIsTree = lF_in_preR == rF;
            lFSubtreeSize = it1sizes[lF];
            lFIsConsecutiveNodeOfCurrentPathNode = startPathNode - lF == 1;
            lFIsLeftSiblingOfCurrentPathNode = lF + lFSubtreeSize == startPathNode;
            sp1spointer = s[(lF + 1) - it1PreLoff];
            sp2spointer = s[lF - it1PreLoff];
            sp3spointer = s[0];
            sp3deltapointer = treesSwapped ? null : delta[lF];
            swritepointer = s[lF - it1PreLoff];
            sp1source = 1; // Search sp1 value in s array by default.
            sp3source = 1; // Search second part of sp3 value in s array by default.
            if (fForestIsTree) { // F_{lF,rF} is a tree.
              if (lFSubtreeSize == 1) { // F_{lF,rF} is a single node.
                sp1source = 3;
              } else if (lFIsConsecutiveNodeOfCurrentPathNode) { // F_{lF,rF}-lF is the path node subtree.
                sp1source = 2;
              }
              sp3 = 0;
              sp3source = 2;
            } else {
              if (lFIsConsecutiveNodeOfCurrentPathNode) {
                sp1source = 2;
              }
              sp3 = currentForestCost1 - (treesSwapped ? it1.preL_to_sumInsCost[lF] : it1.preL_to_sumDelCost[lF]); // USE COST MODEL - Delete F_{lF,rF}-F_lF.
              if (lFIsLeftSiblingOfCurrentPathNode) {
                sp3source = 3;
              }
            }
            if (sp3source == 1) {
              sp3spointer = s[(lF + lFSubtreeSize) - it1PreLoff];
            }
            // Go to first lG.
            int lG = lGfirst;
            // currentForestSize2++;
            // sp1, sp2, sp3 -- Done here for the first node in Loop D. It differs for consecutive nodes.
            // sp1 -- START
            switch(sp1source) {
              case 1: sp1 = sp1spointer[lG - it2PreLoff]; break;
              case 2: sp1 = t[lG - it2PreLoff][rG - it2PreRoff]; break;
              case 3: sp1 = currentForestCost2; break; // USE COST MODEL - Insert G_{lG,rG}.
            }
            sp1 += (treesSwapped ? costModel.ins(lFNode) : costModel.del(lFNode));// USE COST MODEL - Delete lF, leftmost root node in F_{lF,rF}.
            // sp1 -- END
            minCost = sp1; // Start with sp1 as minimal value.
            // sp2 -- START
            if (currentForestSize2 == 1) { // G_{lG,rG} is a single node.
              sp2 = currentForestCost1; // USE COST MODEL - Delete F_{lF,rF}.
            } else { // G_{lG,rG} is a tree.
              sp2 = q[lF];
            }
            sp2 += (treesSwapped ? costModel.del(it2nodes[lG]) : costModel.ins(it2nodes[lG]));// USE COST MODEL - Insert lG, leftmost root node in G_{lG,rG}.
            if (sp2 < minCost) { // Check if sp2 is minimal value.
              minCost = sp2;
            }
            // sp2 -- END
            // sp3 -- START
            if (sp3 < minCost) {
              sp3 += treesSwapped ? delta[lG][lF] : sp3deltapointer[lG];
              if (sp3 < minCost) {
                sp3 += (treesSwapped ? costModel.ren(it2nodes[lG], lFNode) : costModel.ren(lFNode, it2nodes[lG])); // USE COST MODEL - Rename the leftmost root nodes in F_{lF,rF} and G_{lG,rG}.
                if(sp3 < minCost) {
                  minCost = sp3;
                }
              }
            }
            // sp3 -- END
            swritepointer[lG - it2PreLoff] = minCost;
            // Go to next lG.
            lG = ft[lG];
            counter++;
            // Loop D [1, Algorithm 3] - for all nodes to the left of rG.
            while (lG >= lGlast) {
              // Increment size and cost of G forest by node lG.
              currentForestSize2++;
              currentForestCost2 += (treesSwapped ? costModel.del(it2nodes[lG]) : costModel.ins(it2nodes[lG]));
              switch(sp1source) {
                case 1: sp1 = sp1spointer[lG - it2PreLoff] + (treesSwapped ? costModel.ins(lFNode) : costModel.del(lFNode)); break; // USE COST MODEL - Delete lF, leftmost root node in F_{lF,rF}.
                case 2: sp1 = t[lG - it2PreLoff][rG - it2PreRoff] + (treesSwapped ? costModel.ins(lFNode) : costModel.del(lFNode)); break; // USE COST MODEL - Delete lF, leftmost root node in F_{lF,rF}.
                case 3: sp1 = currentForestCost2 + (treesSwapped ? costModel.ins(lFNode) : costModel.del(lFNode)); break; // USE COST MODEL - Insert G_{lG,rG} and elete lF, leftmost root node in F_{lF,rF}.
              }
              sp2 = sp2spointer[fn[lG] - it2PreLoff] + (treesSwapped ? costModel.del(it2nodes[lG]) : costModel.ins(it2nodes[lG])); // USE COST MODEL - Insert lG, leftmost root node in G_{lG,rG}.
              minCost = sp1;
              if(sp2 < minCost) {
                minCost = sp2;
              }
              sp3 = treesSwapped ? delta[lG][lF] : sp3deltapointer[lG];
              if (sp3 < minCost) {
                switch(sp3source) {
                    case 1: sp3 += sp3spointer[fn[(lG + it2sizes[lG]) - 1] - it2PreLoff]; break;
                    case 2: sp3 += currentForestCost2 - (treesSwapped ? it2.preL_to_sumDelCost[lG] : it2.preL_to_sumInsCost[lG]); break; // USE COST MODEL - Insert G_{lG,rG}-G_lG.
                    case 3: sp3 += t[fn[(lG + it2sizes[lG]) - 1] - it2PreLoff][rG - it2PreRoff]; break;
                }
                if (sp3 < minCost) {
                  sp3 += (treesSwapped ? costModel.ren(it2nodes[lG], lFNode) : costModel.ren(lFNode, it2nodes[lG])); // USE COST MODEL - Rename the leftmost root nodes in F_{lF,rF} and G_{lG,rG}.
                  if (sp3 < minCost) {
                    minCost = sp3;
                  }
                }
              }
              swritepointer[lG - it2PreLoff] = minCost;
              lG = ft[lG];
              counter++;
            }
          }
          if (rGminus1_in_preL == parent_of_rG_in_preL) {
            if (!rightPart) {
              if (leftPart) {
                if (treesSwapped) {
                  delta[parent_of_rG_in_preL][endPathNode] = s[(lFlast + 1) - it1PreLoff][(rGminus1_in_preL + 1) - it2PreLoff];
                } else {
                  delta[endPathNode][parent_of_rG_in_preL] = s[(lFlast + 1) - it1PreLoff][(rGminus1_in_preL + 1) - it2PreLoff];
                }
              }
              if (endPathNode > 0 && endPathNode == parent_of_endPathNode + 1 && endPathNode_in_preR == parent_of_endPathNode_in_preR + 1) {
                if (treesSwapped) {
                  delta[parent_of_rG_in_preL][parent_of_endPathNode] = s[lFlast - it1PreLoff][(rGminus1_in_preL + 1) - it2PreLoff];
                } else {
                  delta[parent_of_endPathNode][parent_of_rG_in_preL] = s[lFlast - it1PreLoff][(rGminus1_in_preL + 1) - it2PreLoff];
                }
              }
            }
            for (int lF = lFfirst; lF >= lFlast; lF--) {
              q[lF] = s[lF - it1PreLoff][(parent_of_rG_in_preL + 1) - it2PreLoff];
            }
          }
          // TODO: first pointers can be precomputed
          for (int lG = lGfirst; lG >= lGlast; lG = ft[lG]) {
            t[lG - it2PreLoff][rG - it2PreRoff] = s[lFlast - it1PreLoff][lG - it2PreLoff];
          }
        }
      }
      // Deal with nodes to the right of the path.
      if (pathType == 0 || pathType == 2 && rightPart || pathType == 2 && !leftPart && !rightPart) {
        if (startPathNode == -1) {
          lFfirst = endPathNode;
          rFfirst = it1preL_to_preR[endPathNode];
        } else {
          rFfirst = it1preL_to_preR[startPathNode] - 1;
          lFfirst = endPathNode + 1;
        }
        lFlast = endPathNode;
        lGlast = currentSubtreePreL2;
        lGfirst = (lGlast + subtreeSize2) - 1;
        rFlast = it1preL_to_preR[endPathNode];
        fn[fn.length - 1] = -1;
        for (int i = currentSubtreePreL2; i < currentSubtreePreL2 + subtreeSize2; i++){
          fn[i] = -1;
          ft[i] = -1;
        }
        // Store size and cost of the current forest in F.
        tmpForestSize1 = currentForestSize1;
        tmpForestCost1 = currentForestCost1;
        // Loop B' [1, Algorithm 3] - for all nodes in G.
        for (int lG = lGfirst; lG >= lGlast; lG--) {
          rGfirst = it2preL_to_preR[lG];
          updateFnArray(it2.preR_to_ln[rGfirst], rGfirst, it2preL_to_preR[currentSubtreePreL2]);
          updateFtArray(it2.preR_to_ln[rGfirst], rGfirst);
          int lF = lFfirst;
          lGminus1_in_preR = lG <= currentSubtreePreL2 ? 0x7fffffff : it2preL_to_preR[lG - 1];
          parent_of_lG = it2parents[lG];
          parent_of_lG_in_preR = parent_of_lG == -1 ? -1 : it2preL_to_preR[parent_of_lG];
          // Reset size and cost of forest if F.
          currentForestSize1 = tmpForestSize1;
          currentForestCost1 = tmpForestCost1;
          if (pathType == 0) {
            if (lG == currentSubtreePreL2) {
              rGlast = rGfirst;
            } else if (it2.children[parent_of_lG][0] != lG) {
              rGlast = rGfirst;
            } else {
              rGlast = it2preL_to_preR[parent_of_lG]+1;
            }
          } else {
            rGlast = rGfirst == it2preL_to_preR[currentSubtreePreL2] ? rGfirst : it2preL_to_preR[currentSubtreePreL2];
          }
          // Loop C' [1, Algorithm 3] - for all nodes to the right of the path node.
          for (int rF = rFfirst; rF >= rFlast; rF--) {
            if (rF == rFlast) {
              lF = lFlast;
            }
            rF_in_preL = it1preR_to_preL[rF];
            // Increment size and cost of F forest by node rF.
            currentForestSize1++;
            currentForestCost1 += (treesSwapped ? costModel.ins(it1.preL_to_node[rF_in_preL]) : costModel.del(it1.preL_to_node[rF_in_preL])); // USE COST MODEL - sum up deletion cost of a forest.
            // Reset size and cost of G forest to G_lG.
            currentForestSize2 = it2sizes[lG];
            currentForestCost2 = (treesSwapped ? it2.preL_to_sumDelCost[lG] : it2.preL_to_sumInsCost[lG]); // USE COST MODEL - reset to subtree insertion cost.
            rFSubtreeSize = it1sizes[rF_in_preL];
            if (startPathNode > 0) {
              rFIsConsecutiveNodeOfCurrentPathNode = startPathNode_in_preR - rF == 1;
              rFIsRightSiblingOfCurrentPathNode = rF + rFSubtreeSize == startPathNode_in_preR;
            } else {
              rFIsConsecutiveNodeOfCurrentPathNode = false;
              rFIsRightSiblingOfCurrentPathNode = false;
            }
            fForestIsTree = rF_in_preL == lF;
            AptedNode<D> rFNode = it1.preL_to_node[rF_in_preL];
            sp1spointer = s[(rF + 1) - it1PreRoff];
            sp2spointer = s[rF - it1PreRoff];
            sp3spointer = s[0];
            sp3deltapointer = treesSwapped ? null : delta[rF_in_preL];
            swritepointer = s[rF - it1PreRoff];
            sp1tpointer = t[lG - it2PreLoff];
            sp3tpointer = t[lG - it2PreLoff];
            sp1source = 1;
            sp3source = 1;
            if (fForestIsTree) {
              if (rFSubtreeSize == 1) {
                sp1source = 3;
              } else if (rFIsConsecutiveNodeOfCurrentPathNode) {
                sp1source = 2;
              }
              sp3 = 0;
              sp3source = 2;
            } else {
              if (rFIsConsecutiveNodeOfCurrentPathNode) {
                sp1source = 2;
              }
              sp3 = currentForestCost1 - (treesSwapped ? it1.preL_to_sumInsCost[rF_in_preL] : it1.preL_to_sumDelCost[rF_in_preL]); // USE COST MODEL - Delete F_{lF,rF}-F_rF.
              if (rFIsRightSiblingOfCurrentPathNode) {
                sp3source = 3;
              }
            }
            if (sp3source == 1) {
              sp3spointer = s[(rF + rFSubtreeSize) - it1PreRoff];
            }
            if (currentForestSize2 == 1) {
              sp2 = currentForestCost1;// USE COST MODEL - Delete F_{lF,rF}.
            } else {
              sp2 = q[rF];
            }
            int rG = rGfirst;
            rGfirst_in_preL = it2preR_to_preL[rGfirst];
            currentForestSize2++;
            switch (sp1source) {
              case 1: sp1 = sp1spointer[rG - it2PreRoff]; break;
              case 2: sp1 = sp1tpointer[rG - it2PreRoff]; break;
              case 3: sp1 = currentForestCost2; break; // USE COST MODEL - Insert G_{lG,rG}.
            }
            sp1 += (treesSwapped ? costModel.ins(rFNode) : costModel.del(rFNode)); // USE COST MODEL - Delete rF.
            minCost = sp1;
            sp2 += (treesSwapped ? costModel.del(it2nodes[rGfirst_in_preL]) : costModel.ins(it2nodes[rGfirst_in_preL])); // USE COST MODEL - Insert rG.
            if (sp2 < minCost) {
              minCost = sp2;
            }
            if (sp3 < minCost) {
              sp3 += treesSwapped ? delta[rGfirst_in_preL][rF_in_preL] : sp3deltapointer[rGfirst_in_preL];
              if (sp3 < minCost) {
                sp3 += (treesSwapped ? costModel.ren(it2nodes[rGfirst_in_preL], rFNode) : costModel.ren(rFNode, it2nodes[rGfirst_in_preL]));
                if (sp3 < minCost) {
                  minCost = sp3;
                }
              }
            }
            swritepointer[rG - it2PreRoff] = minCost;
            rG = ft[rG];
            counter++;
            // Loop D' [1, Algorithm 3] - for all nodes to the right of lG;
            while (rG >= rGlast) {
              rG_in_preL = it2preR_to_preL[rG];
              // Increment size and cost of G forest by node rG.
              currentForestSize2++;
              currentForestCost2 += (treesSwapped ? costModel.del(it2nodes[rG_in_preL]) : costModel.ins(it2nodes[rG_in_preL]));
              switch (sp1source) {
                case 1: sp1 = sp1spointer[rG - it2PreRoff] + (treesSwapped ? costModel.ins(rFNode) : costModel.del(rFNode)); break; // USE COST MODEL - Delete rF.
                case 2: sp1 = sp1tpointer[rG - it2PreRoff] + (treesSwapped ? costModel.ins(rFNode) : costModel.del(rFNode)); break; // USE COST MODEL - Delete rF.
                case 3: sp1 = currentForestCost2 + (treesSwapped ? costModel.ins(rFNode) : costModel.del(rFNode)); break; // USE COST MODEL - Insert G_{lG,rG} and delete rF.
              }
              sp2 = sp2spointer[fn[rG] - it2PreRoff] + (treesSwapped ? costModel.del(it2nodes[rG_in_preL]) : costModel.ins(it2nodes[rG_in_preL])); // USE COST MODEL - Insert rG.
              minCost = sp1;
              if (sp2 < minCost) {
                minCost = sp2;
              }
              sp3 = treesSwapped ? delta[rG_in_preL][rF_in_preL] : sp3deltapointer[rG_in_preL];
              if (sp3 < minCost) {
                switch (sp3source) {
                  case 1: sp3 += sp3spointer[fn[(rG + it2sizes[rG_in_preL]) - 1] - it2PreRoff]; break;
                  case 2: sp3 += currentForestCost2 - (treesSwapped ? it2.preL_to_sumDelCost[rG_in_preL] : it2.preL_to_sumInsCost[rG_in_preL]); break; // USE COST MODEL - Insert G_{lG,rG}-G_rG.
                  case 3: sp3 += sp3tpointer[fn[(rG + it2sizes[rG_in_preL]) - 1] - it2PreRoff]; break;
                }
                if (sp3 < minCost) {
                  sp3 += (treesSwapped ? costModel.ren(it2nodes[rG_in_preL], rFNode) : costModel.ren(rFNode, it2nodes[rG_in_preL])); // USE COST MODEL - Rename rF to rG.
                  if (sp3 < minCost) {
                    minCost = sp3;
                  }
                }
              }
              swritepointer[rG - it2PreRoff] = minCost;
              rG = ft[rG];
              counter++;
            }
          }
          if (lG > currentSubtreePreL2 && lG - 1 == parent_of_lG) {
            if (rightPart) {
              if (treesSwapped) {
                delta[parent_of_lG][endPathNode] = s[(rFlast + 1) - it1PreRoff][(lGminus1_in_preR + 1) - it2PreRoff];
              } else {
                delta[endPathNode][parent_of_lG] = s[(rFlast + 1) - it1PreRoff][(lGminus1_in_preR + 1) - it2PreRoff];
              }
            }
            if (endPathNode > 0 && endPathNode == parent_of_endPathNode + 1 && endPathNode_in_preR == parent_of_endPathNode_in_preR + 1)
              if (treesSwapped) {
                delta[parent_of_lG][parent_of_endPathNode] = s[rFlast - it1PreRoff][(lGminus1_in_preR + 1) - it2PreRoff];
              } else {
                delta[parent_of_endPathNode][parent_of_lG] = s[rFlast - it1PreRoff][(lGminus1_in_preR + 1) - it2PreRoff];
              }
            for (int rF = rFfirst; rF >= rFlast; rF--) {
              q[rF] = s[rF - it1PreRoff][(parent_of_lG_in_preR + 1) - it2PreRoff];
            }
          }
          // TODO: first pointers can be precomputed
          for (int rG = rGfirst; rG >= rGlast; rG = ft[rG]) {
            t[lG - it2PreLoff][rG - it2PreRoff] = s[rFlast - it1PreRoff][rG - it2PreRoff];
          }
        }
      }
      // Walk up the path by one node.
      startPathNode = endPathNode;
      endPathNode = it1parents[endPathNode];
    }
    return minCost;
  }

  // ===================== BEGIN spfL
  /**
   * Implements single-path function for left paths [1, Sections 3.3,3.4,3.5].
   * The parameters represent input subtrees for the single-path function.
   * The order of the parameters is important. We use this single-path function
   * due to better performance compared to spfA.
   *
   * @param it1 node indexer of the left-hand input subtree.
   * @param it2 node indexer of the right-hand input subtree.
   * @param treesSwapped says if the order of input subtrees has been swapped
   *                     compared to the order of the initial input trees. Used
   *                     for accessing delta array and deciding on the edit
   *                     operation.
   * @return tree edit distance between left-hand and right-hand input subtrees.
   */
  private float spfL(NodeIndexer it1, NodeIndexer it2, boolean treesSwapped) {
    // Initialise the array to store the keyroot nodes in the right-hand input
    // subtree.
    int[] keyRoots = new int[it2.sizes[it2.getCurrentNode()]];
    Arrays.fill(keyRoots, -1);
    // Get the leftmost leaf node of the right-hand input subtree.
    int pathID = it2.preL_to_lld(it2.getCurrentNode());
    // Calculate the keyroot nodes in the right-hand input subtree.
    // firstKeyRoot is the index in keyRoots of the first keyroot node that
    // we have to process. We need this index because keyRoots array is larger
    // than the number of keyroot nodes.
    int firstKeyRoot = computeKeyRoots(it2, it2.getCurrentNode(), pathID, keyRoots, 0);
    // Initialise an array to store intermediate distances for subforest pairs.
    float[][] forestdist = new float[it1.sizes[it1.getCurrentNode()]+1][it2.sizes[it2.getCurrentNode()]+1];
    // Compute the distances between pairs of keyroot nodes. In the left-hand
    // input subtree only the root is the keyroot. Thus, we compute the distance
    // between the left-hand input subtree and all keyroot nodes in the
    // right-hand input subtree.
    for (int i = firstKeyRoot-1; i >= 0; i--) {
      treeEditDist(it1, it2, it1.getCurrentNode(), keyRoots[i], forestdist, treesSwapped);
    }
    // Return the distance between the input subtrees.
    return forestdist[it1.sizes[it1.getCurrentNode()]][it2.sizes[it2.getCurrentNode()]];
  }

  /**
   * Calculates and stores keyroot nodes for left paths of the given subtree
   * recursively.
   *
   * @param it2 node indexer.
   * @param subtreeRootNode keyroot node - recursion point.
   * @param pathID left-to-right preorder id of the leftmost leaf node of subtreeRootNode.
   * @param keyRoots array that stores all key roots in the order of their left-to-right preorder ids.
   * @param index the index of keyRoots array where to store the next keyroot node.
   * @return the index of the first keyroot node to process.
   */
  // TODO: Merge with computeRevKeyRoots - the only difference is between leftmost and rightmost leaf.
  private int computeKeyRoots(NodeIndexer it2, int subtreeRootNode, int pathID, int[] keyRoots, int index) {
    // The subtreeRootNode is a keyroot node. Add it to keyRoots.
    keyRoots[index] = subtreeRootNode;
    // Increment the index to know where to store the next keyroot node.
    index++;
    // Walk up the left path starting with the leftmost leaf of subtreeRootNode,
    // until the child of subtreeRootNode.
    int pathNode = pathID;
    while (pathNode > subtreeRootNode) {
      int parent = it2.parents[pathNode];
      // For each sibling to the right of pathNode, execute this method recursively.
      // Each right sibling of pathNode is a keyroot node.
      for (int child : it2.children[parent]) {
        // Execute computeKeyRoots recursively for the new subtree rooted at child and child's leftmost leaf node.
        if (child != pathNode) index = computeKeyRoots(it2, child, it2.preL_to_lld(child), keyRoots, index);
      }
      // Walk up.
      pathNode = parent;
    }
    return index;
  }

  /**
   * Implements the core of spfL. Fills in forestdist array with intermediate
   * distances of subforest pairs in dynamic-programming fashion.
   *
   * @param it1 node indexer of the left-hand input subtree.
   * @param it2 node indexer of the right-hand input subtree.
   * @param it1subtree left-to-right preorder id of the root node of the
   *                   left-hand input subtree.
   * @param it2subtree left-to-right preorder id of the root node of the
   *                   right-hand input subtree.
   * @param forestdist the array to be filled in with intermediate distances of subforest pairs.
   * @param treesSwapped says if the order of input subtrees has been swapped
   *                     compared to the order of the initial input trees. Used
   *                     for accessing delta array and deciding on the edit
   *                     operation.
   */
  private void treeEditDist(NodeIndexer it1, NodeIndexer it2, int it1subtree, int it2subtree, float[][] forestdist, boolean treesSwapped) {
    // Translate input subtree root nodes to left-to-right postorder.
    int i = it1.preL_to_postL[it1subtree];
    int j = it2.preL_to_postL[it2subtree];
    // We need to offset the node ids for accessing forestdist array which has
    // indices from 0 to subtree size. However, the subtree node indices do not
    // necessarily start with 0.
    // Whenever the original left-to-right postorder id has to be accessed, use
    // i+ioff and j+joff.
    int ioff = it1.postL_to_lld[i] - 1;
    int joff = it2.postL_to_lld[j] - 1;
    // Variables holding costs of each minimum element.
    float da = 0;
    float db = 0;
    float dc = 0;
    // Initialize forestdist array with deletion and insertion costs of each
    // relevant subforest.
    forestdist[0][0] = 0;
    for (int i1 = 1; i1 <= i - ioff; i1++) {
      forestdist[i1][0] = forestdist[i1 - 1][0] + (treesSwapped ? costModel.ins(it1.postL_to_node(i1 + ioff)) : costModel.del(it1.postL_to_node(i1 + ioff))); // USE COST MODEL - delete i1.
    }
    for (int j1 = 1; j1 <= j - joff; j1++) {
      forestdist[0][j1] = forestdist[0][j1 - 1] + (treesSwapped ? costModel.del(it2.postL_to_node(j1 + joff)) : costModel.ins(it2.postL_to_node(j1 + joff))); // USE COST MODEL - insert j1.
    }
    // Fill in the remaining costs.
    for (int i1 = 1; i1 <= i - ioff; i1++) {
      for (int j1 = 1; j1 <= j - joff; j1++) {
        // Increment the number of subproblems.
        counter++;
        // Calculate partial distance values for this subproblem.
        float u = (treesSwapped ? costModel.ren(it2.postL_to_node(j1 + joff), it1.postL_to_node(i1 + ioff)) : costModel.ren(it1.postL_to_node(i1 + ioff), it2.postL_to_node(j1 + joff))); // USE COST MODEL - rename i1 to j1.
        da = forestdist[i1 - 1][j1] + (treesSwapped ? costModel.ins(it1.postL_to_node(i1 + ioff)) : costModel.del(it1.postL_to_node(i1 + ioff))); // USE COST MODEL - delete i1.
        db = forestdist[i1][j1 - 1] + (treesSwapped ? costModel.del(it2.postL_to_node(j1 + joff)) : costModel.ins(it2.postL_to_node(j1 + joff))); // USE COST MODEL - insert j1.
        // If current subforests are subtrees.
        if (it1.postL_to_lld[i1 + ioff] == it1.postL_to_lld[i] && it2.postL_to_lld[j1 + joff] == it2.postL_to_lld[j]) {
          dc = forestdist[i1 - 1][j1 - 1] + u;
          // Store the relevant distance value in delta array.
          if (treesSwapped) {
            delta[it2.postL_to_preL[j1 + joff]][it1.postL_to_preL[i1 + ioff]] = forestdist[i1 - 1][j1 - 1];
          } else {
            delta[it1.postL_to_preL[i1 + ioff]][it2.postL_to_preL[j1 + joff]] = forestdist[i1 - 1][j1 - 1];
          }
        } else {
          dc = forestdist[it1.postL_to_lld[i1 + ioff] - 1 - ioff][it2.postL_to_lld[j1 + joff] - 1 - joff] +
            (treesSwapped ? delta[it2.postL_to_preL[j1 + joff]][it1.postL_to_preL[i1 + ioff]] : delta[it1.postL_to_preL[i1 + ioff]][it2.postL_to_preL[j1 + joff]]) + u;
        }
        // Calculate final minimum.
        forestdist[i1][j1] = da >= db ? db >= dc ? dc : db : da >= dc ? dc : da;
      }
    }
  }
  // ===================== END spfL

  // ===================== BEGIN spfR
  /**
   * Implements single-path function for right paths [1, Sections 3.3,3.4,3.5].
   * The parameters represent input subtrees for the single-path function.
   * The order of the parameters is important. We use this single-path function
   * due to better performance compared to spfA.
   *
   * @param it1 node indexer of the left-hand input subtree.
   * @param it2 node indexer of the right-hand input subtree.
   * @param treesSwapped says if the order of input subtrees has been swapped
   *                     compared to the order of the initial input trees. Used
   *                     for accessing delta array and deciding on the edit
   *                     operation.
   * @return tree edit distance between left-hand and right-hand input subtrees.
   */
  private float spfR(NodeIndexer it1, NodeIndexer it2, boolean treesSwapped) {
    // Initialise the array to store the keyroot nodes in the right-hand input
    // subtree.
    int[] revKeyRoots = new int[it2.sizes[it2.getCurrentNode()]];
    Arrays.fill(revKeyRoots, -1);
    // Get the rightmost leaf node of the right-hand input subtree.
    int pathID = it2.preL_to_rld(it2.getCurrentNode());
    // Calculate the keyroot nodes in the right-hand input subtree.
    // firstKeyRoot is the index in keyRoots of the first keyroot node that
    // we have to process. We need this index because keyRoots array is larger
    // than the number of keyroot nodes.
    int firstKeyRoot = computeRevKeyRoots(it2, it2.getCurrentNode(), pathID, revKeyRoots, 0);
    // Initialise an array to store intermediate distances for subforest pairs.
    float[][] forestdist = new float[it1.sizes[it1.getCurrentNode()]+1][it2.sizes[it2.getCurrentNode()]+1];
    // Compute the distances between pairs of keyroot nodes. In the left-hand
    // input subtree only the root is the keyroot. Thus, we compute the distance
    // between the left-hand input subtree and all keyroot nodes in the
    // right-hand input subtree.
    for (int i = firstKeyRoot-1; i >= 0; i--) {
      revTreeEditDist(it1, it2, it1.getCurrentNode(), revKeyRoots[i], forestdist, treesSwapped);
    }
    // Return the distance between the input subtrees.
    return forestdist[it1.sizes[it1.getCurrentNode()]][it2.sizes[it2.getCurrentNode()]];
  }

  /**
   * Calculates and stores keyroot nodes for right paths of the given subtree
   * recursively.
   *
   * @param it2 node indexer.
   * @param subtreeRootNode keyroot node - recursion point.
   * @param pathID left-to-right preorder id of the rightmost leaf node of subtreeRootNode.
   * @param revKeyRoots array that stores all key roots in the order of their left-to-right preorder ids.
   * @param index the index of keyRoots array where to store the next keyroot node.
   * @return the index of the first keyroot node to process.
   */
  private int computeRevKeyRoots(NodeIndexer it2, int subtreeRootNode, int pathID, int[] revKeyRoots, int index) {
    // The subtreeRootNode is a keyroot node. Add it to keyRoots.
    revKeyRoots[index] = subtreeRootNode;
    // Increment the index to know where to store the next keyroot node.
    index++;
    // Walk up the right path starting with the rightmost leaf of
    // subtreeRootNode, until the child of subtreeRootNode.
    int pathNode = pathID;
    while (pathNode > subtreeRootNode) {
      int parent = it2.parents[pathNode];
      // For each sibling to the left of pathNode, execute this method recursively.
      // Each left sibling of pathNode is a keyroot node.
      for (int child : it2.children[parent]) {
        // Execute computeRevKeyRoots recursively for the new subtree rooted at child and child's rightmost leaf node.
        if (child != pathNode) index = computeRevKeyRoots(it2, child, it2.preL_to_rld(child), revKeyRoots, index);
      }
      // Walk up.
      pathNode = parent;
    }
    return index;
  }

  /**
   * Implements the core of spfR. Fills in forestdist array with intermediate
   * distances of subforest pairs in dynamic-programming fashion.
   *
   * @param it1 node indexer of the left-hand input subtree.
   * @param it2 node indexer of the right-hand input subtree.
   * @param it1subtree left-to-right preorder id of the root node of the
   *                   left-hand input subtree.
   * @param it2subtree left-to-right preorder id of the root node of the
   *                   right-hand input subtree.
   * @param forestdist the array to be filled in with intermediate distances of
   *                   subforest pairs.
   * @param treesSwapped says if the order of input subtrees has been swapped
   *                     compared to the order of the initial input trees. Used
   *                     for accessing delta array and deciding on the edit
   *                     operation.
   */
  private void revTreeEditDist(NodeIndexer it1, NodeIndexer it2, int it1subtree, int it2subtree, float[][] forestdist, boolean treesSwapped) {
    // Translate input subtree root nodes to right-to-left postorder.
    int i = it1.preL_to_postR[it1subtree];
    int j = it2.preL_to_postR[it2subtree];
    // We need to offset the node ids for accessing forestdist array which has
    // indices from 0 to subtree size. However, the subtree node indices do not
    // necessarily start with 0.
    // Whenever the original right-to-left postorder id has to be accessed, use
    // i+ioff and j+joff.
    int ioff = it1.postR_to_rld[i] - 1;
    int joff = it2.postR_to_rld[j] - 1;
    // Variables holding costs of each minimum element.
    float da = 0;
    float db = 0;
    float dc = 0;
    // Initialize forestdist array with deletion and insertion costs of each
    // relevant subforest.
    forestdist[0][0] = 0;
    for (int i1 = 1; i1 <= i - ioff; i1++) {
      forestdist[i1][0] = forestdist[i1 - 1][0] + (treesSwapped ? costModel.ins(it1.postR_to_node(i1 + ioff)) : costModel.del(it1.postR_to_node(i1 + ioff))); // USE COST MODEL - delete i1.
    }
    for (int j1 = 1; j1 <= j - joff; j1++) {
      forestdist[0][j1] = forestdist[0][j1 - 1] + (treesSwapped ? costModel.del(it2.postR_to_node(j1 + joff)) : costModel.ins(it2.postR_to_node(j1 + joff))); // USE COST MODEL - insert j1.
    }
    // Fill in the remaining costs.
    for (int i1 = 1; i1 <= i - ioff; i1++) {
      for (int j1 = 1; j1 <= j - joff; j1++) {
        // Increment the number of subproblems.
        counter++;
        // Calculate partial distance values for this subproblem.
        float u = (treesSwapped ? costModel.ren(it2.postR_to_node(j1 + joff), it1.postR_to_node(i1 + ioff)) : costModel.ren(it1.postR_to_node(i1 + ioff), it2.postR_to_node(j1 + joff))); // USE COST MODEL - rename i1 to j1.
        da = forestdist[i1 - 1][j1] + (treesSwapped ? costModel.ins(it1.postR_to_node(i1 + ioff)) : costModel.del(it1.postR_to_node(i1 + ioff))); // USE COST MODEL - delete i1.
        db = forestdist[i1][j1 - 1] + (treesSwapped ? costModel.del(it2.postR_to_node(j1 + joff)) : costModel.ins(it2.postR_to_node(j1 + joff))); // USE COST MODEL - insert j1.
        // If current subforests are subtrees.
        if (it1.postR_to_rld[i1 + ioff] == it1.postR_to_rld[i] && it2.postR_to_rld[j1 + joff] == it2.postR_to_rld[j]) {
          dc = forestdist[i1 - 1][j1 - 1] + u;
          // Store the relevant distance value in delta array.
          if (treesSwapped) {
            delta[it2.postR_to_preL[j1+joff]][it1.postR_to_preL[i1+ioff]] = forestdist[i1 - 1][j1 - 1];
          } else {
            delta[it1.postR_to_preL[i1+ioff]][it2.postR_to_preL[j1+joff]] = forestdist[i1 - 1][j1 - 1];
          }
        } else {
          dc = forestdist[it1.postR_to_rld[i1 + ioff] - 1 - ioff][it2.postR_to_rld[j1 + joff] - 1 - joff] +
            (treesSwapped ? delta[it2.postR_to_preL[j1 + joff]][it1.postR_to_preL[i1 + ioff]] : delta[it1.postR_to_preL[i1 + ioff]][it2.postR_to_preL[j1 + joff]]) + u;
        }
        // Calculate final minimum.
        forestdist[i1][j1] = da >= db ? db >= dc ? dc : db : da >= dc ? dc : da;
      }
    }
  }
  // ===================== END spfR

  /**
   * Decodes the path from the optimal strategy to its type.
   *
   * @param pathIDWithPathIDOffset raw path id from strategy array.
   * @param pathIDOffset offset used to distinguish between paths in the source and destination trees.
   * @param it node indexer.
   * @param currentRootNodePreL the left-to-right preorder id of the current subtree processed in tree decomposition phase.
   * @param currentSubtreeSize the size of the subtree currently processed in tree decomposition phase.
   * @return type of the strategy path (LEFT, RIGHT, INNER).
   */
  private byte getStrategyPathType(int pathIDWithPathIDOffset, int pathIDOffset, NodeIndexer it, int currentRootNodePreL, int currentSubtreeSize) {
    if (Integer.signum(pathIDWithPathIDOffset) == -1) {
      return LEFT;
    }
    int pathID = Math.abs(pathIDWithPathIDOffset) - 1;
    if (pathID >= pathIDOffset) {
      pathID = pathID - pathIDOffset;
    }
    if (pathID == (currentRootNodePreL + currentSubtreeSize) - 1) {
      return RIGHT;
    }
    return INNER;
  }

  /**
   * fn array used in the algorithm before [1]. Using it does not change the
   * complexity.
   *
   * <p>TODO: Do not use it [1, Section 8.4].
   *
   * @param lnForNode ---
   * @param node ---
   * @param currentSubtreePreL ---
   */
  private void updateFnArray(int lnForNode, int node, int currentSubtreePreL) {
    if (lnForNode >= currentSubtreePreL) {
      fn[node] = fn[lnForNode];
      fn[lnForNode] = node;
    } else {
      fn[node] = fn[fn.length - 1];
      fn[fn.length - 1] = node;
    }
  }

  /**
   * ft array used in the algorithm before [1]. Using it does not change the
   * complexity.
   *
   * <p>TODO: Do not use it [1, Section 8.4].
   *
   * @param lnForNode ---
   * @param node ---
   */
  private void updateFtArray(int lnForNode, int node) {
    ft[node] = lnForNode;
    if(fn[node] > -1) {
      ft[fn[node]] = node;
    }
  }

  /**
   * Compute the edit mapping between two trees. The trees are input trees
   * to the distance computation and the distance must be computed before
   * computing the edit mapping (distances of subtree pairs are required).
   *
   * @return Returns list of pairs of nodes that are mapped as pairs of their
   *         postorder IDs (starting with 1). Nodes that are deleted or
   *         inserted are mapped to 0.
   */
  // TODO: Mapping computation requires more thorough documentation
  //       (methods computeEditMapping, forestDist, mappingCost).
  // TODO: Before computing the mapping, verify if TED has been computed.
  //       Mapping computation should trigger distance computation if
  //       necessary.
  public List<int[]> computeEditMapping() {

    // Initialize tree and forest distance arrays.
    // Arrays for subtree distrances is not needed because the distances
    // between subtrees without the root nodes are already stored in delta.
    float[][] forestdist = new float[size1 + 1][size2 + 1];

    boolean rootNodePair = true;

    // forestdist for input trees has to be computed
    forestDist(it1, it2, size1, size2, forestdist);

    // empty edit mapping
    LinkedList<int[]> editMapping = new LinkedList<int[]>();

    // empty stack of tree Pairs
    LinkedList<int[]> treePairs = new LinkedList<int[]>();

    // push the pair of trees (ted1,ted2) to stack
    treePairs.push(new int[] { size1, size2 });

    while (!treePairs.isEmpty()) {
      // get next tree pair to be processed
      int[] treePair = treePairs.pop();
      int lastRow = treePair[0];
      int lastCol = treePair[1];

      // compute forest distance matrix
      if (!rootNodePair) {
        forestDist(it1, it2, lastRow, lastCol, forestdist);
      }
      rootNodePair = false;

      // compute mapping for current forest distance matrix
      int firstRow = it1.postL_to_lld[lastRow-1];
      int firstCol = it2.postL_to_lld[lastCol-1];
      int row = lastRow;
      int col = lastCol;
      while ((row > firstRow) || (col > firstCol)) {
        if ((row > firstRow) && (forestdist[row - 1][col] + costModel.del(it1.postL_to_node(row-1)) == forestdist[row][col])) { // USE COST MODEL - Delete node row of source tree.
          // node with postorderID row is deleted from ted1
          editMapping.push(new int[] { row, 0 });
          row--;
        } else if ((col > firstCol) && (forestdist[row][col - 1] + costModel.ins(it2.postL_to_node(col-1)) == forestdist[row][col])) { // USE COST MODEL - Insert node col of destination tree.
          // node with postorderID col is inserted into ted2
          editMapping.push(new int[] { 0, col });
          col--;
        } else {
          // node with postorderID row in ted1 is renamed to node col
          // in ted2
          if ((it1.postL_to_lld[row-1] == it1.postL_to_lld[lastRow-1]) && (it2.postL_to_lld[col-1] == it2.postL_to_lld[lastCol-1])) {
            // if both subforests are trees, map nodes
            editMapping.push(new int[] { row, col });
            row--;
            col--;
          } else {
            // push subtree pair
            treePairs.push(new int[] { row, col });

            // continue with forest to the left of the popped
            // subtree pair
            row = it1.postL_to_lld[row-1];
            col = it2.postL_to_lld[col-1];
          }
        }
      }
    }
    return editMapping;
  }


  /**
   * Recalculates distances between subforests of two subtrees. These values
   * are used in mapping computation to track back the origin of minimum values.
   * It is basen on Zhang and Shasha algorithm.
   *
   * <p>The rename cost must be added in the last line. Otherwise the formula is
   * incorrect. This is due to delta storing distances between subtrees
   * without the root nodes.
   *
   * <p>i and j are postorder ids of the nodes - starting with 1.
   *
   * @param ted1 node indexer of the source input tree.
   * @param ted2 node indexer of the destination input tree.
   * @param i subtree root of source tree that is to be mapped.
   * @param j subtree root of destination tree that is to be mapped.
   * @param forestdist array to store distances between subforest pairs.
   */
  private void forestDist(NodeIndexer ted1, NodeIndexer ted2, int i, int j, float[][] forestdist) {

    forestdist[ted1.postL_to_lld[i-1]][ted2.postL_to_lld[j-1]] = 0;

    for (int di = ted1.postL_to_lld[i-1]+1; di <= i; di++) {
      forestdist[di][ted2.postL_to_lld[j-1]] = forestdist[di - 1][ted2.postL_to_lld[j-1]] + costModel.del(ted1.postL_to_node(di-1));
      for (int dj = ted2.postL_to_lld[j-1]+1; dj <= j; dj++) {
        forestdist[ted1.postL_to_lld[i-1]][dj] = forestdist[ted1.postL_to_lld[i-1]][dj - 1] + costModel.ins(ted2.postL_to_node(dj-1));
        float costRen = costModel.ren(ted1.postL_to_node(di-1), ted2.postL_to_node(dj-1));
        // TODO: The first two elements of the minimum can be computed here,
        //       similarly to spfL and spfR.
        if ((ted1.postL_to_lld[di-1] == ted1.postL_to_lld[i-1]) && (ted2.postL_to_lld[dj-1] == ted2.postL_to_lld[j-1])) {
          forestdist[di][dj] = Math.min(Math.min(
                  forestdist[di - 1][dj] + costModel.del(ted1.postL_to_node(di-1)),
                  forestdist[di][dj - 1] + costModel.ins(ted2.postL_to_node(dj-1))),
                  forestdist[di - 1][dj - 1] + costRen);
          // If substituted with delta, this will overwrite the value
          // in delta.
          // It looks that we don't have to write this value.
          // Conceptually it is correct because we already have all
          // the values in delta for subtrees without the root nodes,
          // and we need these.
          // treedist[di][dj] = forestdist[di][dj];
        } else {
          // di and dj are postorder ids of the nodes - starting with 1
          // Substituted 'treedist[di][dj]' with 'delta[it1.postL_to_preL[di-1]][it2.postL_to_preL[dj-1]]'
          forestdist[di][dj] = Math.min(Math.min(
                  forestdist[di - 1][dj] + costModel.del(ted1.postL_to_node(di-1)),
                  forestdist[di][dj - 1] + costModel.ins(ted2.postL_to_node(dj-1))),
                  forestdist[ted1.postL_to_lld[di-1]][ted2.postL_to_lld[dj-1]] + delta[it1.postL_to_preL[di-1]][it2.postL_to_preL[dj-1]] + costRen);
        }
      }
    }
  }

  /**
   * Calculates the cost of an edit mapping. It traverses the mapping and sums
   * up the cost of each operation. The costs are taken from the cost model.
   *
   * @param mapping an edit mapping.
   * @return cost of edit mapping.
   */
  public float mappingCost(List<int[]> mapping) {
    float cost = 0.0f;
    for (int i = 0; i < mapping.size(); i++) {
      if (mapping.get(i)[0] == 0) { // Insertion.
          cost += costModel.ins(it2.postL_to_node(mapping.get(i)[1]-1));
      } else if (mapping.get(i)[1] == 0) { // Deletion.
          cost += costModel.del(it1.postL_to_node(mapping.get(i)[0]-1));
      } else { // Rename.
        cost += costModel.ren(it1.postL_to_node(mapping.get(i)[0]-1), it2.postL_to_node(mapping.get(i)[1]-1));
      }
    }
    return cost;
  }

}

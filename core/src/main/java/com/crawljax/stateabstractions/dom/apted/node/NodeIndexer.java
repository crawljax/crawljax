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
import java.util.Iterator;

import com.crawljax.stateabstractions.dom.apted.costmodel.CostModel;

/**
 * Indexes nodes of the input tree to the algorithm that is already parsed to
 * tree structure using {@link node.AptedNode} class. Stores various indices on
 * nodes required for efficient computation of APTED [1,2]. Additionally, it
 * stores
 * single-value properties of the tree.
 *
 * <p>For indexing we use four tree traversals that assign ids to the nodes:
 * <ul>
 * <li>left-to-right preorder [1],
 * <li>right-to-left preorder [1],
 * <li>left-to-right postorder [2],
 * <li>right-to-left postorder [2].
 * </ul>
 *
 * <p>See the source code for more algorithm-related comments.
 *
 * <p>References:
 * <ul>
 * <li>[1] M. Pawlik and N. Augsten. Efficient Computation of the Tree Edit
 *      Distance. ACM Transactions on Database Systems (TODS) 40(1). 2015.
 * <li>[2] M. Pawlik and N. Augsten. Tree edit distance: Robust and memory-
 *      efficient. Information Systems 56. 2016.
 * </ul>
 *
 * @param <D> type of node data.
 * @param <C> type of cost model.
 * @see node.AptedNode
 * @see parser.InputParser
 */
public class NodeIndexer<D, C extends CostModel> {

  // [TODO] Be consistent in naming index variables: <FROM>_to_<TO>.

  // Structure indices.

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to AptedNode object corresponding to n. Used for cost of edit operations.
   *
   * @see node.Node
   */
  public AptedNode<D> preL_to_node[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the size of n's subtree (node n and all its descendants).
   */
  public int sizes[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the left-to-right preorder id of n's parent.
   */
  public int parents[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the array of n's children. Size of children array at node n equals the number
   * of n's children.
   */
  public int children[][];

  /**
   * Index from left-to-right postorder id of node n (starting with {@code 0})
   * to the left-to-right postorder id of n's leftmost leaf descendant.
   */
  public int postL_to_lld[];

  /**
   * Index from right-to-left postorder id of node n (starting with {@code 0})
   * to the right-to-left postorder id of n's rightmost leaf descendant.
   */
  public int postR_to_rld[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the left-to-right preorder id of the first leaf node to the left of n.
   * If there is no leaf node to the left of n, it is represented with the
   * value {@code -1} [1, Section 8.4].
   */
  public int preL_to_ln[];

  /**
   * Index from right-to-left preorder id of node n (starting with {@code 0})
   * to the right-to-left preorder id of the first leaf node to the right of n.
   * If there is no leaf node to the right of n, it is represented with the
   * value {@code -1} [1, Section 8.4].
   */
  public int preR_to_ln[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to a boolean value that states if node n lies on the leftmost path
   * starting at n's parent [2, Algorithm 1, Lines 26,36].
   */
  public boolean nodeType_L[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to a boolean value that states if node n lies on the rightmost path
   * starting at n's parent input tree [2, Section 5.3, Algorithm 1, Lines 26,36].
   */
  public boolean nodeType_R[];

  // Traversal translation indices.

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the right-to-left preorder id of n.
   */
  public int preL_to_preR[];

  /**
   * Index from right-to-left preorder id of node n (starting with {@code 0})
   * to the left-to-right preorder id of n.
   */
  public int preR_to_preL[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the left-to-right postorder id of n.
   */
  public int preL_to_postL[];

  /**
   * Index from left-to-right postorder id of node n (starting with {@code 0})
   * to the left-to-right preorder id of n.
   */
  public int postL_to_preL[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the right-to-left postorder id of n.
   */
  public int preL_to_postR[];

  /**
   * Index from right-to-left postorder id of node n (starting with {@code 0})
   * to the left-to-right preorder id of n.
   */
  public int postR_to_preL[];

  // Cost indices.

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the cost of spf_L (single path function using the leftmost path) for
   * the subtree rooted at n [1, Section 5.2].
   */
  public int preL_to_kr_sum[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the cost of spf_R (single path function using the rightmost path) for
   * the subtree rooted at n [1, Section 5.2].
   */
  public int preL_to_rev_kr_sum[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the cost of spf_A (single path function using an inner path) for the
   * subtree rooted at n [1, Section 5.2].
   */
  public int preL_to_desc_sum[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the cost of deleting all nodes in the subtree rooted at n.
   */
  public float preL_to_sumDelCost[];

  /**
   * Index from left-to-right preorder id of node n (starting with {@code 0})
   * to the cost of inserting all nodes in the subtree rooted at n.
   */
  public float preL_to_sumInsCost[];

  // Variables holding values modified at runtime while the algorithm executes.

  /**
   * Stores the left-to-right preorder id of the current subtree's root node.
   * Used in the tree decomposition phase of APTED [1, Algorithm 1].
   */
  private int currentNode;

  // Structure single-value variables.

  /**
   * Stores the size of the input tree.
   */
  private int treeSize;

  /**
   * Stores the number of leftmost-child leaf nodes in the input tree
   * [2, Section 5.3].
   */
  public int lchl;

  /**
   * Stores the number of rightmost-child leaf nodes in the input tree
   * [2, Section 5.3].
   */
  public int rchl;

  // Variables used temporarily while indexing.

  /**
   * Temporary variable used in indexing for storing subtree size.
   */
  private int sizeTmp;

  /**
   * Temporary variable used in indexing for storing sum of subtree sizes
   * rooted at descendant nodes.
   */
  private int descSizesTmp;

  /**
   * Temporary variable used in indexing for storing sum of keyroot node sizes.
   */
  private int krSizesSumTmp;

  /**
   * Temporary variable used in indexing for storing sum of right-to-left
   * keyroot node sizes.
   */
  private int revkrSizesSumTmp;

  /**
   * Temporary variable used in indexing for storing preorder index of a node.
   */
  private int preorderTmp;

  private C costModel;

  /**
   * Indexes the nodes of input trees and stores the indices for quick access
   * from APTED algorithm.
   *
   * @param inputTree an input tree to APTED. Its nodes will be indexed.
   * @param costModel instance of a cost model to compute preL_to_sumDelCost
   *                  and preL_to_sumInsCost.
   */
  public NodeIndexer(AptedNode<D> inputTree, C costModel) {
    // Initialise variables.
    sizeTmp = 0;
    descSizesTmp = 0;
    krSizesSumTmp = 0;
    revkrSizesSumTmp = 0;
    preorderTmp = 0;
    currentNode = 0;
    treeSize = inputTree.getNodeCount();

    // Initialise indices with the lengths equal to the tree size.
    sizes = new int[treeSize];
    preL_to_preR = new int[treeSize];
    preR_to_preL = new int[treeSize];
    preL_to_postL = new int[treeSize];
    postL_to_preL = new int[treeSize];
    preL_to_postR = new int[treeSize];
    postR_to_preL = new int[treeSize];
    postL_to_lld = new int[treeSize];
    postR_to_rld = new int[treeSize];
    preL_to_node = new AptedNode[treeSize];
    preL_to_ln = new int[treeSize];
    preR_to_ln = new int[treeSize];
    preL_to_kr_sum = new int[treeSize];
    preL_to_rev_kr_sum = new int[treeSize];
    preL_to_desc_sum = new int[treeSize];

    preL_to_sumDelCost = new float[treeSize];
    preL_to_sumInsCost = new float[treeSize];

    children = new int[treeSize][];
    nodeType_L = new boolean[treeSize];
    nodeType_R = new boolean[treeSize];
    parents = new int[treeSize];
    parents[0] = -1; // The root has no parent.

    this.costModel = costModel;

    // Index the nodes.
    indexNodes(inputTree, -1);
    postTraversalIndexing();
  }

  /**
   * Indexes the nodes of the input tree. Stores information about each tree
   * node in index arrays. It computes the following indices: {@link #parents},
   * {@link #children}, {@link #nodeType_L}, {@link #nodeType_R},
   * {@link #preL_to_desc_sum}, {@link #preL_to_kr_sum},
   * {@link #preL_to_rev_kr_sum}, {@link #preL_to_node}, {@link #sizes},
   * {@link #preL_to_preR}, {@link #preR_to_preL}, {@link #postL_to_preL},
   * {@link #preL_to_postL}, {@link #preL_to_postR}, {@link #postR_to_preL}.
   *
   * <p>It is a recursive method that traverses the tree once.
   *
   * @param node is the current node while traversing the input tree.
   * @param postorder is the postorder id of the current node.
   * @return postorder id of the current node.
   */
  private int indexNodes(AptedNode<D> node, int postorder) {
    // Initialise variables.
    int currentSize = 0;
    int childrenCount = 0;
    int descSizes = 0;
    int krSizesSum = 0;
    int revkrSizesSum = 0;
    int preorder = preorderTmp;
    int preorderR = 0;
    int currentPreorder = -1;
    // Initialise empty array to store children of this node.
    ArrayList<Integer> childrenPreorders = new ArrayList<>();

    // Store the preorder id of the current node to use it after the recursion.
    preorderTmp++;

    // Loop over children of a node.
    Iterator<AptedNode<D>> childrenIt = node.getChildren().iterator();
    while (childrenIt.hasNext()) {
      childrenCount++;
      currentPreorder = preorderTmp;
      parents[currentPreorder] = preorder;

      // Execute method recursively for next child.
      postorder = indexNodes(childrenIt.next(), postorder);

      childrenPreorders.add(Integer.valueOf(currentPreorder));

      currentSize += 1 + sizeTmp;
      descSizes += descSizesTmp;
      if(childrenCount > 1) {
          krSizesSum += krSizesSumTmp + sizeTmp + 1;
      } else {
          krSizesSum += krSizesSumTmp;
          nodeType_L[currentPreorder] = true;
      }
      if(childrenIt.hasNext()) {
          revkrSizesSum += revkrSizesSumTmp + sizeTmp + 1;
      } else {
          revkrSizesSum += revkrSizesSumTmp;
          nodeType_R[currentPreorder] = true;
      }
    }

    postorder++;

    int currentDescSizes = descSizes + currentSize + 1;
    preL_to_desc_sum[preorder] = ((currentSize + 1) * (currentSize + 1 + 3)) / 2 - currentDescSizes;
    preL_to_kr_sum[preorder] = krSizesSum + currentSize + 1;
    preL_to_rev_kr_sum[preorder] = revkrSizesSum + currentSize + 1;

    // Store pointer to a node object corresponding to preorder.
    preL_to_node[preorder] = node;

    sizes[preorder] = currentSize + 1;
    preorderR = treeSize - 1 - postorder;
    preL_to_preR[preorder] = preorderR;
    preR_to_preL[preorderR] = preorder;

    children[preorder] = toIntArray(childrenPreorders);

    descSizesTmp = currentDescSizes;
    sizeTmp = currentSize;
    krSizesSumTmp = krSizesSum;
    revkrSizesSumTmp = revkrSizesSum;

    postL_to_preL[postorder] = preorder;
    preL_to_postL[preorder] = postorder;
    preL_to_postR[preorder] = treeSize-1-preorder;
    postR_to_preL[treeSize-1-preorder] = preorder;

    return postorder;
  }

  /**
   * Indexes the nodes of the input tree. It computes the following indices,
   * which could not be computed immediately while traversing the tree in
   * {@link #indexNodes}: {@link #preL_to_ln}, {@link #postL_to_lld},
   * {@link #postR_to_rld}, {@link #preR_to_ln}.
   *
   * <p>Runs in linear time in the input tree size. Currently requires two
   * loops over input tree nodes. Can be reduced to one loop (see the code).
   */
  private void postTraversalIndexing() {
    int currentLeaf = -1;
    int nodeForSum = -1;
    int parentForSum = -1;
    for(int i = 0; i < treeSize; i++) {
      preL_to_ln[i] = currentLeaf;
      if(isLeaf(i)) {
          currentLeaf = i;
      }

      // This block stores leftmost leaf descendants for each node
      // indexed in postorder. Used for mapping computation.
      // Added by Victor.
      int postl = i; // Assume that the for loop iterates postorder.
      int preorder = postL_to_preL[i];
      if (sizes[preorder] == 1) {
        postL_to_lld[postl] = postl;
      } else {
        postL_to_lld[postl] = postL_to_lld[preL_to_postL[children[preorder][0]]];
      }
      // This block stores rightmost leaf descendants for each node
      // indexed in right-to-left postorder.
      // [TODO] Use postL_to_lld and postR_to_rld instead of APTED.getLLD
      //        and APTED.gerRLD methods, remove these method.
      //        Result: faster lookup of these values.
      int postr = i; // Assume that the for loop iterates reversed postorder.
      preorder = postR_to_preL[postr];
      if (sizes[preorder] == 1) {
        postR_to_rld[postr] = postr;
      } else {
        postR_to_rld[postr] = postR_to_rld[preL_to_postR[children[preorder][children[preorder].length-1]]];
      }
      // Count lchl and rchl.
      // [TODO] There are no values for parent node.
      if (sizes[i] == 1) {
        int parent = parents[i];
        if (parent > -1) {
          if (parent+1 == i) {
          	lchl++;
          } else if (preL_to_preR[parent]+1 == preL_to_preR[i]) {
          	rchl++;
          }
        }
      }

      // Sum up costs of deleting and inserting entire subtrees.
      // Reverse the node index. Here, we need traverse nodes bottom-up.
      nodeForSum = treeSize - i - 1;
      parentForSum = parents[nodeForSum];
      // Update myself.
      preL_to_sumDelCost[nodeForSum] += costModel.del(preL_to_node[nodeForSum]);
      preL_to_sumInsCost[nodeForSum] += costModel.ins(preL_to_node[nodeForSum]);
      if (parentForSum > -1) {
        // Update my parent.
        preL_to_sumDelCost[parentForSum] += preL_to_sumDelCost[nodeForSum];
        preL_to_sumInsCost[parentForSum] += preL_to_sumInsCost[nodeForSum];
      }
    }

    currentLeaf = -1;
    // [TODO] Merge with the other loop. Assume different traversal.
    for(int i = 0; i < sizes[0]; i++) {
      preR_to_ln[i] = currentLeaf;
      if(isLeaf(preR_to_preL[i])) {
        currentLeaf = i;
      }
    }
  }

  /**
   * An abbreviation that uses indices to calculate the left-to-right preorder
   * id of the leftmost leaf node of the given node.
   *
   * @param preL left-to-right preorder id of a node.
   * @return left-to-right preorder id of the leftmost leaf node of preL.
   */
  public int preL_to_lld(int preL) {
    return postL_to_preL[postL_to_lld[preL_to_postL[preL]]];
  }

  /**
   * An abbreviation that uses indices to calculate the left-to-right preorder
   * id of the rightmost leaf node of the given node.
   *
   * @param preL left-to-right preorder id of a node.
   * @return left-to-right preorder id of the rightmost leaf node of preL.
   */
  public int preL_to_rld(int preL) {
    return postR_to_preL[postR_to_rld[preL_to_postR[preL]]];
  }

  /**
   * An abbreviation that uses indices to retrieve pointer to {@link node.AptedNode}
   * of the given node.
   *
   * @param postL left-to-right postorder id of a node.
   * @return {@link node.AptedNode} corresponding to postL.
   */
  public AptedNode<D> postL_to_node(int postL) {
    return preL_to_node[postL_to_preL[postL]];
  }

  /**
   * An abbreviation that uses indices to retrieve pointer to {@link node.AptedNode}
   * of the given node.
   *
   * @param postR right-to-left postorder id of a node.
   * @return {@link node.AptedNode} corresponding to postR.
   */
  public AptedNode<D> postR_to_node(int postR) {
    return preL_to_node[postR_to_preL[postR]];
  }

  /**
   * Returns the number of nodes in the input tree.
   *
   * @return number of nodes in the tree.
   */
  public int getSize() {
    return treeSize;
  }

  /**
   * Verifies if node is a leaf.
   *
   * @param node preorder id of a node to verify.
   * @return {@code true} if {@code node} is a leaf, {@code false} otherwise.
   */
  public boolean isLeaf(int node) {
    return sizes[node] == 1;
  }

  /**
   * Converts {@link ArrayList} of integer values to an array. Reads all items
   * in the list and copies to the output array. The size of output array equals
   * the number of elements in the list.
   *
   * @param integers ArrayList with integer values.
   * @return array with values from input ArrayList.
   */
  private int[] toIntArray(ArrayList<Integer> integers) {
    int ints[] = new int[integers.size()];
    int i = 0;
    for (Integer n : integers) {
      ints[i++] = n.intValue();
    }
    return ints;
  }

  /**
   * Returns the root node of the currently processed subtree in the tree
   * decomposition part of APTED [1, Algorithm 1]. At each point, we have to
   * know which subtree do we process.
   *
   * @return current subtree root node.
   */
  public int getCurrentNode() {
    return currentNode;
  }

  /**
   * Stores the root nodes's preorder id of the currently processes subtree.
   *
   * @param preorder preorder id of the root node.
   */
  public void setCurrentNode(int preorder) {
    currentNode = preorder;
  }

}

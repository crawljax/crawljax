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

package com.crawljax.stateabstractions.dom.apted.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.List;

import com.crawljax.stateabstractions.dom.apted.costmodel.CostModel;
import com.crawljax.stateabstractions.dom.apted.costmodel.StringUnitCostModel;
import com.crawljax.stateabstractions.dom.apted.distance.APTED;
import com.crawljax.stateabstractions.dom.apted.node.AptedNode;
import com.crawljax.stateabstractions.dom.apted.node.StringNodeData;
import com.crawljax.stateabstractions.dom.apted.parser.BracketStringInputParser;
import com.crawljax.stateabstractions.dom.apted.parser.InputParser;

/**
 * This is the command line interface for executing APTED algorithm.
 *
 * @param <C> type of cost model.
 * @param <P> type of input parser.
 * @see CostModel
 * @see InputParser
 */
public class CommandLine<C extends CostModel, P extends InputParser> {

  private String helpMessage =
      "\n" +
      "Compute the edit distance between two trees.\n" +
      "\n" +
      "SYNTAX\n" +
      "\n" +
      "    java -jar APTED.jar {-t TREE1 TREE2 | -f FILE1 FILE2} [-m] [-v]\n" +
      "\n" +
      "    java -jar APTED.jar -h\n" +
      "\n" +
      "DESCRIPTION\n" +
      "\n" +
      "    Compute the edit distance between two trees with APTED algorithm [1,2].\n" +
      "    APTED supersedes our RTED algorithm [3].\n" +
      "    By default unit cost model is supported where each edit operation\n" +
      "    has cost 1 (in case of equal labels the cost is 0).\n" +
      "\n" +
      "    For implementing other cost models see the details on github website\n" +
      "    (https://github.com/DatabaseGroup/apted).\n" +
      "\n" +
      "LICENCE\n" +
      "\n" +
      "    The source code of this program is published under the MIT licence and\n" +
      "    can be found on github (https://github.com/DatabaseGroup/apted).\n" +
      "\n" +
      "OPTIONS\n" +
      "\n" +
      "    -h, --help \n" +
      "        print this help message.\n" +
      "\n" +
      "    -t TREE1 TREE2,\n" +
      "    --trees TREE1 TREE2\n" +
      "        compute the tree edit distance between TREE1 and TREE2. The\n" +
      "        trees are encoded in the bracket notation, for example, in tree\n" +
      "        {A{B{X}{Y}{F}}{C}} the root node has label A and two children\n" +
      "        with labels B and C. B has three children with labels X, Y, F.\n" +
      "\n" +
      "    -f FILE1 FILE2, \n" +
      "    --files FILE1 FILE2\n" +
      "        compute the tree edit distance between the two trees stored in\n" +
      "        the files FILE1 and FILE2. The trees are encoded in bracket\n" +
      "        notation.\n" +
      // "\n" +
      // "    -c CD CI CR, \n" +
      // "    --costs CD CI CR\n" +
      // "        set custom cost for edit operations. Default is -c 1 1 1.\n" +
      // "        CD - cost of node deletion\n" +
      // "        CI - cost of node insertion\n" +
      // "        CR - cost of node renaming\n" +
      "\n" +
      "    -v, --verbose\n" +
      "        print verbose output, including tree edit distance, runtime,\n" +
      "        number of relevant subproblems and strategy statistics.\n" +
      "\n" +
      "    -m, --mapping\n" +
      "        compute the minimal edit mapping between two trees. There might\n" +
      "        be multiple minimal edit mappings. This option computes only one\n" +
      "        of them. The first line of the output is the cost of the mapping.\n" +
      "        The following lines represent the edit operations. n and m are\n" +
      "        postorder IDs (beginning with 1) of nodes in the left-hand and\n" +
      "        the right-hand trees respectively.\n" +
      "            n->m - rename node n to m\n" +
      "            n->0 - delete node n\n" +
      "            0->m - insert node m\n" +
      "EXAMPLES\n" +
      "\n" +
      "    java -jar APTED.jar -t {a{b}{c}} {a{b{d}}}\n" +// -c 1 1 0.5\n" +
      "    java -jar APTED.jar -f 1.tree 2.tree\n" +
      "    java -jar APTED.jar -t {a{b}{c}} {a{b{d}}} -m -v\n" +
      "\n" +
      "REFERENCES\n" +
      "\n" +
      "    [1] M. Pawlik and N. Augsten. Efficient Computation of the Tree Edit\n" +
      "        Distance. ACM Transactions on Database Systems (TODS) 40(1). 2015.\n" +
      "    [2] M. Pawlik and N. Augsten. Tree edit distance: Robust and memory-\n" +
      "        efficient. Information Systems 56. 2016.\n" +
      "    [3] M. Pawlik and N. Augsten. RTED: A Robust Algorithm for the Tree Edit\n" +
      "        Distance. PVLDB 5(4). 2011.\n" +
      "\n" +
      "AUTHORS\n" +
      "\n" +
      "    Mateusz Pawlik, Nikolaus Augsten";

  // TODO: Review if all fields are necessary.
  private String wrongArgumentsMessage = "Wrong arguments. Try \"java -jar RTED.jar --help\" for help.";

  private boolean run, custom, array, strategy, ifSwitch, sota, verbose, demaine, mapping;
  private int sotaStrategy;
  private String customStrategy, customStrategyArrayFile;
  private APTED rted;
  private double ted;

  private C costModel;
  private P inputParser;
  private AptedNode t1;
  private AptedNode t2;

  /**
   * Constructs the command line. Initialises the cost model and input parser
   * of specific types.
   *
   * @param costModel instance of a specific cost model.
   * @param inputParser instance of a specific inputParser.
   * @see CostModel
   * @see InputParser
   */
  public CommandLine(C costModel, P inputParser) {
    this.costModel = costModel;
    this.inputParser = inputParser;
  }

  /**
   * Main method, invoced when executing the jar file.
   *
   * @param args array of command line arguments passed when executing jar file.
   */
  public static void main(String[] args) {
    CommandLine<StringUnitCostModel, BracketStringInputParser> rtedCL = new CommandLine<>(new StringUnitCostModel(), new BracketStringInputParser());
    rtedCL.runCommandLine(args);
  }

  /**
   * Run the command line with given arguments.
   *
   * @param args array of command line arguments passed when executing jar file.
   */
   public void runCommandLine(String[] args) {
    rted = new APTED<C, StringNodeData>(costModel);
    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("--help") || args[i].equals("-h")) {
          System.out.println(helpMessage);
          System.exit(0);
        } else if (args[i].equals("-t") || args[i].equals("--trees")) {
          parseTreesFromCommandLine(args[i+1], args[i+2]);
          i = i+2;
          run = true;
        } else if (args[i].equals("-f") || args[i].equals("--files")) {
          parseTreesFromFiles(args[i+1], args[i+2]);
          i = i+2;
          run = true;
        // TODO: -f option temporarily disabled for refactoring.
        // } else if (args[i].equals("-c") || args[i].equals("--costs")) {
        //   setCosts(args[i+1], args[i+2], args[i+3]);
        //   i = i+3;
        } else if (args[i].equals("-v") || args[i].equals("--verbose")) {
          verbose = true;
        } else if (args[i].equals("-m") || args[i].equals("--mapping")) {
          mapping = true;
        } else {
          System.out.println(wrongArgumentsMessage);
          System.exit(0);
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Too few arguments.");
      System.exit(0);
    }

    if (!run) {
      System.out.println(wrongArgumentsMessage);
      System.exit(0);
    }

    long time1 = (new Date()).getTime();

    ted = rted.computeEditDistance(t1, t2);

    long time2 = (new Date()).getTime();
    if (verbose) {
      System.out.println("distance:             " + ted);
      System.out.println("runtime:              " + ((time2 - time1) / 1000.0));
    } else {
      System.out.println(ted);
    }

    if (mapping) { // TED is computed anyways.
      List<int[]> editMapping = rted.computeEditMapping();
      for (int[] nodeAlignment : editMapping) {
        System.out.println(nodeAlignment[0] + "->" + nodeAlignment[1]);
      }
    }

  }

  /**
   * Parse two input trees from the command line and convert them to tree
   * representation using {@link AptedNode} class.
   *
   * @param ts1 source input tree as string.
   * @param ts2 destination input tree as string.
   * @see AptedNode
   */
  private void parseTreesFromCommandLine(String ts1, String ts2) {
    try {
      t1 = inputParser.fromString(ts1);
    } catch (Exception e) {
      System.out.println("TREE1 argument has wrong format");
      System.exit(0);
    }
    try {
      t2 = inputParser.fromString(ts2);
    } catch (Exception e) {
      System.out.println("TREE2 argument has wrong format");
      System.exit(0);
    }
	}


  /**
   * Parses two input trees from given files and convert them to tree
   * representation using {@link AptedNode} class.
   *
   * @param fs1 path to file with source tree.
   * @param fs2 path to file with destination tree.
   * @see AptedNode
   */
  private void parseTreesFromFiles(String fs1, String fs2) {
    try {
      t1 = inputParser.fromString((new BufferedReader(new FileReader(fs1))).readLine());
    } catch (Exception e) {
      System.out.println("TREE1 argument has wrong format");
      System.exit(0);
    }
    try {
      t2 = inputParser.fromString((new BufferedReader(new FileReader(fs2))).readLine());
    } catch (Exception e) {
      System.out.println("TREE2 argument has wrong format");
      System.exit(0);
    }
  }

  // TODO: Bring the functionalitites below back to life.

  // /**
  //  * Set custom costs for the edit operations.
  //  *
  //  * @deprecated
  //  * @param cds cost of deletion.
  //  * @param cis cost of insertion.
  //  * @param cms cost of rename (mapping).
  //  */
  //  private void setCosts(String cds, String cis, String cms) {
  //    try {
  //      rted.setCustomCosts(Float.parseFloat(cds), Float.parseFloat(cis), Float.parseFloat(cms));
  //    } catch (Exception e) {
  //      System.out.println("One of the costs has wrong format.");
  //      System.exit(0);
  //    }
  // }

}

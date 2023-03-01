package com.crawljax.stateabstractions.hybrid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.crawljax.util.DomUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@RunWith(Parameterized.class)
public class TreeDiffParamTest {

    private final String state1;
    private final String state2;
    private final Integer distance;

    private final String addedNode;

    private final String removedNode;

    public TreeDiffParamTest(String state1, String state2, Integer difference, String addedNode, String removedNode) {
        this.state1 = state1;
        this.state2 = state2;
        this.distance = difference;
        this.addedNode = addedNode;
        this.removedNode = removedNode;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"frag_state487.html", "frag_state408.html", 5, "#text", null},
            {"frag_state487.html", "frag_state503.html", 2, "#text", null},
            {"frag_state408.html", "frag_state503.html", 3, null, "input"}
        });
    }

    @Test
    public void testTreeDiff() throws IOException {

        String docString = FileUtils.readFileToString(new File("src/test/resources/crawls/" + state1));
        Document doc1 = DomUtils.asDocument(docString);

        String docString2 = FileUtils.readFileToString(new File("src/test/resources/crawls/" + state2));
        Document doc2 = DomUtils.asDocument(docString2);

        double compDist = HybridStateVertexImpl.computeDistance(doc1, doc2, false);
        assertEquals(this.distance, compDist, 0.0);

        List<List<Node>> differentNodes = HybridStateVertexImpl.getChangedNodes(doc1, doc2, false);

        List<Node> addedNodes = differentNodes.get(0);
        List<Node> removedNodes = differentNodes.get(1);

        if (addedNode != null) {
            assertTrue(addedNodes.get(0).getNodeName().equalsIgnoreCase(addedNode));
        }

        if (removedNode != null) {
            assertTrue(removedNodes.get(0).getNodeName().equalsIgnoreCase(removedNode));
        }
    }
}

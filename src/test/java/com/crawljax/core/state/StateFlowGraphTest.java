/**
 * Created Dec 19, 2007
 */
package com.crawljax.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.junit.Test;

import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification.How;

/**
 * @author mesbah
 * @version $Id$
 */
public class StateFlowGraphTest {

	@Test
	public void testSFG() throws Exception {

		StateVertix index = new StateVertix("index", "<table><div>index</div></table>");
		StateVertix state2 = new StateVertix("STATE_TWO", "<table><div>state2</div></table>");
		StateVertix state3 = new StateVertix("STATE_THREE", "<table><div>state3</div></table>");
		StateVertix state4 = new StateVertix("STATE_FOUR", "<table><div>state4</div></table>");
		StateVertix state5 = new StateVertix("STATE_FIVE", "<table><div>state5</div></table>");

		StateFlowGraph graph = new StateFlowGraph(index);

		StateVertix DOUBLE = new StateVertix("index", "<table><div>index</div></table>");
		assertTrue(graph.addState(state2) == null);
		assertTrue(graph.addState(state3) == null);
		assertTrue(graph.addState(state4) == null);
		assertTrue(graph.addState(state5) == null);

		assertFalse(graph.addState(DOUBLE) == null);

		assertTrue(graph.addEdge(index, state2, new Eventable(new Identification(How.xpath,
		        "/body/div[4]"), EventType.click)));

		assertTrue(graph.addEdge(state2, index, new Eventable(new Identification(How.xpath,
		        "/body/div[89]"), EventType.click)));

		assertTrue(graph.addEdge(state2, state3, new Eventable(new Identification(How.xpath,
		        "/home/a"), EventType.click)));
		assertTrue(graph.addEdge(index, state4, new Eventable(new Identification(How.xpath,
		        "/body/div[2]/div"), EventType.click)));
		assertTrue(graph.addEdge(state2, state5, new Eventable(new Identification(How.xpath,
		        "/body/div[5]"), EventType.click)));

		assertFalse(graph.addEdge(state2, state5, new Eventable(new Identification(How.xpath,
		        "/body/div[5]"), EventType.click)));

		Set<Eventable> clickables = graph.getOutgoingClickables(state2);
		assertEquals(3, clickables.size());

		clickables = graph.getIncomingClickable(state2);
		assertTrue(clickables.size() == 1);

		assertNotNull(graph.toString());

		assertEquals(state2.hashCode(), state2.hashCode());

		assertTrue(state2.equals(new StateVertix("STATE_2", "<table><div>state2</div></table>")));

		assertTrue(graph.canGoTo(state2, state3));
		assertTrue(graph.canGoTo(state2, state5));
		assertFalse(graph.canGoTo(state2, state4));

		assertTrue(graph.canGoTo(state2, index));

		// Dijkstra
		List<Eventable> list = graph.getShortestPath(index, state3);
		assertEquals(list.size(), 2);

		Eventable c = list.get(0);
		assertEquals(c.getIdentification().getValue(), "/body/div[4]");
		c = list.get(1);
		assertEquals(c.getIdentification().getValue(), "/home/a");

		StateVertix hold = new StateVertix(index.getName(), index.getDom());

		list = graph.getShortestPath(hold, state3);
		assertEquals(list.size(), 2);

		Set<StateVertix> states = graph.getOutgoingStates(index);
		assertTrue(states.size() == 2);
		assertTrue(states.contains(state2));
		assertTrue(states.contains(state4));
		assertFalse(states.contains(state3));

		Set<StateVertix> allStates = graph.getAllStates();

		assertTrue(allStates.size() == 5);
	}

	@Test
	public void testCloneStates() throws Exception {
		StateVertix index = new StateVertix("index", "<table><div>index</div></table>");
		StateVertix state2 = new StateVertix("STATE_TWO", "<table><div>state2</div></table>");
		StateVertix state3 = new StateVertix("STATE_THREE", "<table><div>state2</div></table>");

		StateFlowGraph graph = new StateFlowGraph(index);

		StateVertix state4 = new StateVertix("STATE_FOUR", "<table><div>state4</div></table>");
		assertTrue(graph.addState(state2) == null);
		assertTrue(graph.addState(state4) == null);
		// assertFalse(graph.addState(state3));
		assertTrue(graph.addEdge(index, state2, new Eventable(new Identification(How.xpath,
		        "/body/div[4]"), EventType.click)));

		// if (graph.containsVertex(state3)) {
		// StateVertix state_clone = graph.getStateInGraph(state3);
		// assertEquals(state3, state_clone);
		// }

		assertTrue(graph.addEdge(state4, state3, new Eventable(new Identification(How.xpath,
		        "/home/a"), EventType.click)));
		// System.out.println(graph.toString());
		// assertNull(graph.getStateInGraph(new StateVertix("STATE_TEST",
		// "<table><div>TEST</div></table>")));
	}

	@Test
	public void testGetMeanStateStringSize() {
		String HTML1 =
		        "<SCRIPT src='js/jquery-1.2.1.js' type='text/javascript'></SCRIPT> "
		                + "<SCRIPT src='js/jquery-1.2.3.js' type='text/javascript'></SCRIPT>"
		                + "<body><div id='firstdiv' class='orange'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";

		String HTML2 =
		        "<SCRIPT src='js/jquery-1.2.1.js' type='text/javascript'></SCRIPT> "
		                + "<SCRIPT src='js/jquery-1.2.3.js' type='text/javascript'></SCRIPT>"
		                + "<body><div id='firstdiv' class='orange'>";

		StateFlowGraph g = new StateFlowGraph(new StateVertix("", HTML1));
		g.addState(new StateVertix("", HTML2));

		assertEquals(206, g.getMeanStateStringSize());
	}

	@Test
	public void testDoubleEvents() {

		StateVertix state1 = new StateVertix("STATE_ONE", "<table><div>state1</div></table>");
		StateVertix state2 = new StateVertix("STATE_TWO", "<table><div>state2</div></table>");
		StateFlowGraph sfg = new StateFlowGraph(state1);

		Eventable c1 =
		        new Eventable(new Identification(How.xpath, "/body/div[4]"), EventType.click);
		Eventable c2 =
		        new Eventable(new Identification(How.xpath, "/body/div[4]/div[2]"),
		                EventType.click);
		sfg.addState(state1);
		sfg.addState(state2);

		sfg.addEdge(state1, state2, c1);
		sfg.addEdge(state1, state2, c2);
		assertEquals(2, sfg.getAllEdges().size());
	}

	@Test
	public void testAllPossiblePaths() {
		StateVertix index = new StateVertix("index", "<table><div>index</div></table>");
		StateVertix state2 = new StateVertix("STATE_TWO", "<table><div>state2</div></table>");
		StateVertix state3 = new StateVertix("STATE_THREE", "<table><div>state3</div></table>");
		StateVertix state4 = new StateVertix("STATE_FOUR", "<table><div>state4</div></table>");
		StateVertix state5 = new StateVertix("STATE_FIVE", "<table><div>state5</div></table>");
		StateFlowGraph g = new StateFlowGraph(index);
		g.addState(state2);
		g.addState(state3);
		g.addState(state4);
		g.addState(state5);

		g.addEdge(index, state2, new Eventable(new Identification(How.xpath, "/index/2"),
		        EventType.click));
		g.addEdge(state2, index, new Eventable(new Identification(How.xpath, "/2/index"),
		        EventType.click));
		g.addEdge(state2, state3, new Eventable(new Identification(How.xpath, "/2/3"),
		        EventType.click));
		g.addEdge(index, state4, new Eventable(new Identification(How.xpath, "/index/4"),
		        EventType.click));
		g.addEdge(state2, state5, new Eventable(new Identification(How.xpath, "/2/5"),
		        EventType.click));
		g.addEdge(state4, index, new Eventable(new Identification(How.xpath, "/4/index"),
		        EventType.click));
		g.addEdge(index, state5, new Eventable(new Identification(How.xpath, "/index/5"),
		        EventType.click));
		g.addEdge(state4, state2, new Eventable(new Identification(How.xpath, "/4/2"),
		        EventType.click));
		g.addEdge(state3, state5, new Eventable(new Identification(How.xpath, "/3/5"),
		        EventType.click));

		g.addEdge(state3, state4, new Eventable(new Identification(How.xpath, "/3/4"),
		        EventType.click));

		List<List<GraphPath<StateVertix, Eventable>>> results = g.getAllPossiblePaths(index);

		assertEquals(2, results.size());

		List<GraphPath<StateVertix, Eventable>> p = results.get(0);

		assertEquals(5, p.size());

		GraphPath<StateVertix, Eventable> e = p.get(0);

		assertEquals(1, e.getEdgeList().size());

		p = results.get(1);

		assertEquals(2, p.size());

		// int x = 0;
		// int y = 0;
		//
		// for (List<GraphPath<StateVertix, Eventable>> paths : results) {
		// for (GraphPath<StateVertix, Eventable> path : paths) {
		// // System.out.print(" Edge: " + x + ":" + y);
		//
		// for (Eventable edge : path.getEdgeList()) {
		// // System.out.print(", " + edge.toString());
		//
		// }
		//
		// // System.out.println(" ");
		// y++;
		// }
		//
		// x++;
		// }
	}

	@Test
	public void largetTest() {
		StateVertix index = new StateVertix("index", "<table><div>index</div></table>");
		StateVertix state2 = new StateVertix("STATE_TWO", "<table><div>state2</div></table>");
		StateVertix state3 = new StateVertix("STATE_THREE", "<table><div>state3</div></table>");
		StateVertix state4 = new StateVertix("STATE_FOUR", "<table><div>state4</div></table>");
		StateVertix state5 = new StateVertix("STATE_FIVE", "<table><div>state5</div></table>");
		StateFlowGraph g = new StateFlowGraph(index);
		g.addState(state2);
		g.addState(state3);
		g.addState(state4);
		g.addState(state5);

		g.addEdge(index, state2, new Eventable(new Identification(How.xpath, "/index/2"),
		        EventType.click));
		g.addEdge(state2, index, new Eventable(new Identification(How.xpath, "/2/index"),
		        EventType.click));
		g.addEdge(state2, state3, new Eventable(new Identification(How.xpath, "/2/3"),
		        EventType.click));
		g.addEdge(index, state4, new Eventable(new Identification(How.xpath, "/index/4"),
		        EventType.click));
		g.addEdge(state2, state5, new Eventable(new Identification(How.xpath, "/2/5"),
		        EventType.click));
		g.addEdge(state4, index, new Eventable(new Identification(How.xpath, "/4/index"),
		        EventType.click));
		g.addEdge(index, state5, new Eventable(new Identification(How.xpath, "/index/5"),
		        EventType.click));
		g.addEdge(state4, state2, new Eventable(new Identification(How.xpath, "/4/2"),
		        EventType.click));
		g.addEdge(state3, state5, new Eventable(new Identification(How.xpath, "/3/5"),
		        EventType.click));

		List<List<GraphPath<StateVertix, Eventable>>> results = g.getAllPossiblePaths(index);

		assertEquals(2, results.size());

		List<GraphPath<StateVertix, Eventable>> p = results.get(0);

		assertEquals(5, p.size());

		GraphPath<StateVertix, Eventable> e = p.get(0);

		assertEquals(1, e.getEdgeList().size());

		p = results.get(1);

		assertEquals(2, p.size());
		int x = 0;
		int y = 0;
		// int max = 0;
		Set<Eventable> uEvents = new HashSet<Eventable>();

		for (List<GraphPath<StateVertix, Eventable>> paths : results) {
			for (GraphPath<StateVertix, Eventable> path : paths) {
				// System.out.print(" Edge: " + x + ":" + y);
				// int z = 0;
				for (Eventable edge : path.getEdgeList()) {
					// System.out.print(", " + edge.toString());
					if (!uEvents.contains(edge)) {
						uEvents.add(edge);
					}

				}

				// System.out.println(" z:" + path.getEdgeList().size());

				y++;
			}

			x++;
		}
	}

	@Test
	public void guidedCrawlingFlag() {
		StateVertix index = new StateVertix("index", "<table><div>index</div></table>");
		StateVertix state2 = new StateVertix("STATE_TWO", "<table><div>state2</div></table>");
		StateVertix state3 = new StateVertix("STATE_THREE", "<table><div>state3</div></table>");
		StateVertix state4 = new StateVertix("STATE_FOUR", "<table><div>state4</div></table>");
		StateVertix state5 = new StateVertix("STATE_FIVE", "<table><div>state5</div></table>");
		StateFlowGraph g = new StateFlowGraph(index);
		g.addState(state2);
		g.addState(state3);
		g.addState(state4);
		g.addState(state5);

		assertEquals(5, g.getAllStates().size());

		StateVertix state6 = new StateVertix("STATE_FIVE", "<table><div>state5</div></table>");
		state6.setGuidedCrawling(false);
		g.addState(state6);

		assertEquals(5, g.getAllStates().size());

		state6.setGuidedCrawling(true);

		g.addState(state6);
		assertEquals(6, g.getAllStates().size());

	}

	@Test
	public void testEdges() throws Exception {

		StateVertix index = new StateVertix("index", "<table><div>index</div></table>");
		StateVertix state2 = new StateVertix("STATE_TWO", "<table><div>state2</div></table>");
		StateVertix state3 = new StateVertix("STATE_THREE", "<table><div>state3</div></table>");
		StateVertix state4 = new StateVertix("STATE_FOUR", "<table><div>state4</div></table>");
		StateFlowGraph graph = new StateFlowGraph(index);
		assertTrue(graph.addState(state2) == null);
		assertTrue(graph.addState(state3) == null);
		assertTrue(graph.addState(state4) == null);

		Eventable e1 = new Eventable(new Identification(How.xpath, "/4/index"), EventType.click);

		Eventable e2 = new Eventable(new Identification(How.xpath, "/4/index"), EventType.click);

		Eventable e3 = new Eventable(new Identification(How.xpath, "/4/index"), EventType.click);

		Eventable e4 = new Eventable(new Identification(How.xpath, "/5/index"), EventType.click);

		Eventable e5 = new Eventable(new Identification(How.xpath, "/4/index"), EventType.click);
		Eventable e6 = new Eventable(new Identification(How.xpath, "/5/index"), EventType.click);

		assertTrue(graph.addEdge(index, state2, e1));
		assertFalse(graph.addEdge(index, state2, e1));
		assertFalse(graph.addEdge(index, state2, e3));

		assertFalse(graph.addEdge(state2, state3, e1));
		assertTrue(graph.addEdge(state2, state3, e2));

		assertTrue(graph.addEdge(index, state4, e3));
		assertTrue(graph.addEdge(index, state4, e4));
		assertFalse(graph.addEdge(index, state4, e5));
		assertFalse(graph.addEdge(index, state4, e6));
	}
}

package com.crawljax.core.state;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasEdges;
import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;
import org.jgrapht.GraphPath;
import org.junit.Before;
import org.junit.Test;

import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification.How;

public class StateFlowGraphTest {

	private StateVertex index;
	private StateVertex state2;
	private StateVertex state3;
	private StateVertex state4;
	private StateVertex state5;
	private StateFlowGraph graph;

	@Before
	public void setup() {
		index = new StateVertex("index", "<table><div>index</div></table>");
		state2 = new StateVertex("STATE_TWO", "<table><div>state2</div></table>");
		state3 = new StateVertex("STATE_THREE", "<table><div>state3</div></table>");
		state4 = new StateVertex("STATE_FOUR", "<table><div>state4</div></table>");
		state5 = new StateVertex("STATE_FIVE", "<table><div>state5</div></table>");
		graph = new StateFlowGraph(index);
	}

	@Test
	public void testSFG() throws Exception {
		StateVertex DOUBLE = new StateVertex("index", "<table><div>index</div></table>");
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

		assertTrue(state2.equals(new StateVertex("STATE_2", "<table><div>state2</div></table>")));

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

		StateVertex hold = new StateVertex(index.getName(), index.getDom());

		list = graph.getShortestPath(hold, state3);
		assertEquals(list.size(), 2);

		Set<StateVertex> states = graph.getOutgoingStates(index);
		assertTrue(states.size() == 2);
		assertTrue(states.contains(state2));
		assertTrue(states.contains(state4));
		assertFalse(states.contains(state3));

		Set<StateVertex> allStates = graph.getAllStates();

		assertTrue(allStates.size() == 5);
	}

	@Test
	public void testCloneStates() throws Exception {
		StateVertex state3clone2 =
		        new StateVertex("STATE_THREE", "<table><div>state2</div></table>");

		assertTrue(graph.addState(state2) == null);
		assertTrue(graph.addState(state4) == null);
		// assertFalse(graph.addState(state3));
		assertTrue(graph.addEdge(index, state2, new Eventable(new Identification(How.xpath,
		        "/body/div[4]"), EventType.click)));

		// if (graph.containsVertex(state3)) {
		// StateVertix state_clone = graph.getStateInGraph(state3);
		// assertEquals(state3, state_clone);
		// }

		assertTrue(graph.addEdge(state4, state3clone2, new Eventable(new Identification(
		        How.xpath,
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

		StateFlowGraph g = new StateFlowGraph(new StateVertex("", HTML1));
		g.addState(new StateVertex("", HTML2));

		assertEquals(206, g.getMeanStateStringSize());
	}

	@Test
	public void testDoubleEvents() {
		Eventable c1 =
		        new Eventable(new Identification(How.xpath, "/body/div[4]"), EventType.click);
		Eventable c2 =
		        new Eventable(new Identification(How.xpath, "/body/div[4]/div[2]"),
		                EventType.click);
		graph.addState(index);
		graph.addState(state2);

		graph.addEdge(index, state2, c1);
		graph.addEdge(index, state2, c2);
		assertEquals(2, graph.getAllEdges().size());
	}

	@Test
	public void testAllPossiblePaths() {
		graph.addState(state2);
		graph.addState(state3);
		graph.addState(state4);
		graph.addState(state5);

		graph.addEdge(index, state2, new Eventable(new Identification(How.xpath, "/index/2"),
		        EventType.click));
		graph.addEdge(state2, index, new Eventable(new Identification(How.xpath, "/2/index"),
		        EventType.click));
		graph.addEdge(state2, state3, new Eventable(new Identification(How.xpath, "/2/3"),
		        EventType.click));
		graph.addEdge(index, state4, new Eventable(new Identification(How.xpath, "/index/4"),
		        EventType.click));
		graph.addEdge(state2, state5, new Eventable(new Identification(How.xpath, "/2/5"),
		        EventType.click));
		graph.addEdge(state4, index, new Eventable(new Identification(How.xpath, "/4/index"),
		        EventType.click));
		graph.addEdge(index, state5, new Eventable(new Identification(How.xpath, "/index/5"),
		        EventType.click));
		graph.addEdge(state4, state2, new Eventable(new Identification(How.xpath, "/4/2"),
		        EventType.click));
		graph.addEdge(state3, state5, new Eventable(new Identification(How.xpath, "/3/5"),
		        EventType.click));

		graph.addEdge(state3, state4, new Eventable(new Identification(How.xpath, "/3/4"),
		        EventType.click));

		List<List<GraphPath<StateVertex, Eventable>>> results = graph.getAllPossiblePaths(index);

		assertEquals(2, results.size());

		List<GraphPath<StateVertex, Eventable>> p = results.get(0);

		assertEquals(5, p.size());

		GraphPath<StateVertex, Eventable> e = p.get(0);

		assertEquals(1, e.getEdgeList().size());

		p = results.get(1);

		assertEquals(2, p.size());

	}

	@Test
	public void largetTest() {
		graph.addState(state2);
		graph.addState(state3);
		graph.addState(state4);
		graph.addState(state5);

		graph.addEdge(index, state2, new Eventable(new Identification(How.xpath, "/index/2"),
		        EventType.click));
		graph.addEdge(state2, index, new Eventable(new Identification(How.xpath, "/2/index"),
		        EventType.click));
		graph.addEdge(state2, state3, new Eventable(new Identification(How.xpath, "/2/3"),
		        EventType.click));
		graph.addEdge(index, state4, new Eventable(new Identification(How.xpath, "/index/4"),
		        EventType.click));
		graph.addEdge(state2, state5, new Eventable(new Identification(How.xpath, "/2/5"),
		        EventType.click));
		graph.addEdge(state4, index, new Eventable(new Identification(How.xpath, "/4/index"),
		        EventType.click));
		graph.addEdge(index, state5, new Eventable(new Identification(How.xpath, "/index/5"),
		        EventType.click));
		graph.addEdge(state4, state2, new Eventable(new Identification(How.xpath, "/4/2"),
		        EventType.click));
		graph.addEdge(state3, state5, new Eventable(new Identification(How.xpath, "/3/5"),
		        EventType.click));

		List<List<GraphPath<StateVertex, Eventable>>> results = graph.getAllPossiblePaths(index);

		assertThat(results, hasSize(2));

		assertThat(results.get(0), hasSize(5));

		assertThat(results.get(0).get(0).getEdgeList(), hasSize(1));

		assertThat(results.get(1), hasSize(2));
		// int max = 0;
		Set<Eventable> uEvents = new HashSet<Eventable>();

		for (List<GraphPath<StateVertex, Eventable>> paths : results) {
			for (GraphPath<StateVertex, Eventable> p : paths) {
				// System.out.print(" Edge: " + x + ":" + y);
				// int z = 0;
				for (Eventable edge : p.getEdgeList()) {
					// System.out.print(", " + edge.toString());
					if (!uEvents.contains(edge)) {
						uEvents.add(edge);
					}

				}
			}
		}
	}

	@Test
	public void guidedCrawlingFlag() {
		graph.addState(state2);
		graph.addState(state3);
		graph.addState(state4);
		graph.addState(state5);

		assertThat(graph, hasStates(5));

		StateVertex state6 = new StateVertex("STATE_FIVE", "<table><div>state5</div></table>");
		state6.setGuidedCrawling(false);
		graph.addState(state6);

		assertThat(graph, hasStates(5));

		state6.setGuidedCrawling(true);

		graph.addState(state6);
		assertThat(graph, hasStates(6));

	}

	@Test
	public void testEdges() throws Exception {
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

	@Test
	public void testSerializability() throws UnsupportedEncodingException {
		Eventable c1 =
		        new Eventable(new Identification(How.xpath, "/body/div[4]"), EventType.click);
		Eventable c2 =
		        new Eventable(new Identification(How.xpath, "/body/div[4]/div[2]"),
		                EventType.click);

		Eventable c3 =
		        new Eventable(new Identification(How.xpath, "/body/div[4]/div[6]"),
		                EventType.click);

		graph.addState(index);
		graph.addState(state2);
		graph.addState(state3);

		graph.addEdge(index, state2, c1);
		graph.addEdge(index, state2, c2);
		graph.addEdge(state2, state3, c3);
		assertThat(graph, hasStates(3));
		assertThat(graph, hasEdges(3));
		assertThat(graph.getOutgoingClickables(index).size(), is(2));

		byte[] serializedSFG = SerializationUtils.serialize(graph);
		StateFlowGraph deserializedSfg =
		        (StateFlowGraph) SerializationUtils.deserialize(serializedSFG);

		assertThat(deserializedSfg, hasStates(3));
		assertThat(deserializedSfg, hasEdges(3));
		assertThat(deserializedSfg.getOutgoingClickables(index).size(), is(2));
	}

	@Test
	public void whenStateAddedTheGraphCouterIsIncremented() {
		assertThat(graph.getNumberOfStates(), is(1));
		graph.addState(state2);
		assertThat(graph.getNumberOfStates(), is(2));
	}

}

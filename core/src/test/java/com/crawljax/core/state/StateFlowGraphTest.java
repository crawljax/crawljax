package com.crawljax.core.state;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.junit.Before;
import org.junit.Test;

import com.crawljax.core.ExitNotifier;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification.How;

public class StateFlowGraphTest {

	private StateVertex index;
	private StateVertex state2;
	private StateVertex state3;
	private StateVertex state4;
	private StateVertex state5;
	private InMemoryStateFlowGraph graph;

	@Before
	public void setup() {
		index =
		        new StateVertexImpl(StateVertex.INDEX_ID, "index",
		                "<table><div>index</div></table>");
		state2 = new StateVertexImpl(2, "STATE_TWO", "<table><div>state2</div></table>");
		state3 = new StateVertexImpl(3, "STATE_THREE", "<table><div>state3</div></table>");
		state4 = new StateVertexImpl(4, "STATE_FOUR", "<table><div>state4</div></table>");
		state5 = new StateVertexImpl(5, "STATE_FIVE", "<table><div>state5</div></table>");
		graph = new InMemoryStateFlowGraph(new ExitNotifier(0), new DefaultStateVertexFactory());
		graph.putIndex(index);
	}

	@Test
	public void testDuplicationAdding() throws Exception {
		assertThat(graph.putIfAbsent(index), is(not(nullValue())));
		assertThat(graph.putIfAbsent(state2), is(nullValue()));
		assertThat(graph.putIfAbsent(state3), is(nullValue()));
		assertThat(graph.putIfAbsent(state4), is(nullValue()));
		assertThat(graph.putIfAbsent(state5), is(nullValue()));

		assertTrue(graph.addEdge(index, state2, newXpathEventable("/body/div[4]")));
		assertTrue(graph.addEdge(state2, index, newXpathEventable("/body/div[89]")));
		assertTrue(graph.addEdge(state2, state3, newXpathEventable("/home/a")));
		assertTrue(graph.addEdge(index, state4, newXpathEventable("/body/div[2]/div")));
		assertTrue(graph.addEdge(state2, state5, newXpathEventable("/body/div[5]")));

		assertFalse(graph.addEdge(state2, state5, newXpathEventable("/body/div[5]")));

		Set<Eventable> clickables = graph.getOutgoingClickables(state2);
		assertEquals(3, clickables.size());

		clickables = graph.getIncomingClickable(state2);
		assertTrue(clickables.size() == 1);

		assertNotNull(graph.toString());

		assertEquals(state2.hashCode(), state2.hashCode());

		assertTrue(state2
		        .equals(new StateVertexImpl(2, "STATE_2", "<table><div>state2</div></table>")));

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

		StateVertex hold =
		        new StateVertexImpl(StateVertex.INDEX_ID, index.getName(), index.getDom());

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

	private Eventable newXpathEventable(String xPath) {
		return new Eventable(new Identification(How.xpath, xPath), EventType.click);
	}

	@Test
	public void testCloneStates() throws Exception {
		StateVertex state3clone2 =
		        new StateVertexImpl(3, "STATE_THREE", "<table><div>state2</div></table>");

		assertTrue(graph.putIfAbsent(state2) == null);
		assertTrue(graph.putIfAbsent(state4) == null);
		// assertFalse(graph.addState(state3));
		assertTrue(graph.addEdge(index, state2, newXpathEventable("/body/div[4]")));

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

		InMemoryStateFlowGraph g = new InMemoryStateFlowGraph(new ExitNotifier(0), new DefaultStateVertexFactory());
		g.putIndex(new StateVertexImpl(1, "", HTML1));
		g.putIfAbsent(new StateVertexImpl(2, "", HTML2));

		assertEquals(206, g.getMeanStateStringSize());
	}

	@Test
	public void testDoubleEvents() {
		Eventable c1 = newXpathEventable("/body/div[4]");
		Eventable c2 = newXpathEventable("/body/div[4]/div[2]");
		graph.putIfAbsent(index);
		graph.putIfAbsent(state2);

		graph.addEdge(index, state2, c1);
		graph.addEdge(index, state2, c2);
		assertEquals(2, graph.getAllEdges().size());
	}

	@Test
	public void testEdges() throws Exception {
		assertTrue(graph.putIfAbsent(state2) == null);
		assertTrue(graph.putIfAbsent(state3) == null);
		assertTrue(graph.putIfAbsent(state4) == null);

		Eventable e1 = newXpathEventable("/4/index");

		Eventable e2 = newXpathEventable("/4/index");

		Eventable e3 = newXpathEventable("/4/index");

		Eventable e4 = newXpathEventable("/5/index");

		Eventable e5 = newXpathEventable("/4/index");
		Eventable e6 = newXpathEventable("/5/index");

		assertTrue(graph.addEdge(index, state2, e1));
		assertFalse(graph.addEdge(index, state2, e1));
		assertFalse(graph.addEdge(index, state2, e3));

		assertTrue(graph.addEdge(state2, state3, e2));
		assertFalse(graph.addEdge(state2, state3, e1));

		assertTrue(graph.addEdge(index, state4, e3));
		assertTrue(graph.addEdge(index, state4, e4));
		assertFalse(graph.addEdge(index, state4, e5));
		assertFalse(graph.addEdge(index, state4, e6));
	}

	@Test
	public void whenStateAddedTheGraphCouterIsIncremented() {
		assertThat(graph.getNumberOfStates(), is(1));
		graph.putIfAbsent(state2);
		assertThat(graph.getNumberOfStates(), is(2));
	}

	@Test
	public void testAllPossiblePaths() {
		graph.putIfAbsent(state2);
		graph.putIfAbsent(state3);
		graph.putIfAbsent(state4);
		graph.putIfAbsent(state5);

		graph.addEdge(index, state2, newXpathEventable("/index/2"));
		graph.addEdge(state2, index, newXpathEventable("/2/index"));
		graph.addEdge(state2, state3, newXpathEventable("/2/3"));
		graph.addEdge(index, state4, newXpathEventable("/index/4"));
		graph.addEdge(state2, state5, newXpathEventable("/2/5"));
		graph.addEdge(state4, index, newXpathEventable("/4/index"));
		graph.addEdge(index, state5, newXpathEventable("/index/5"));
		graph.addEdge(state4, state2, newXpathEventable("/4/2"));
		graph.addEdge(state3, state5, newXpathEventable("/3/5"));

		graph.addEdge(state3, state4, newXpathEventable("/3/4"));

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
		graph.putIfAbsent(state2);
		graph.putIfAbsent(state3);
		graph.putIfAbsent(state4);
		graph.putIfAbsent(state5);

		graph.addEdge(index, state2, newXpathEventable("/index/2"));
		graph.addEdge(state2, index, newXpathEventable("/2/index"));
		graph.addEdge(state2, state3, newXpathEventable("/2/3"));
		graph.addEdge(index, state4, newXpathEventable("/index/4"));
		graph.addEdge(state2, state5, newXpathEventable("/2/5"));
		graph.addEdge(state4, index, newXpathEventable("/4/index"));
		graph.addEdge(index, state5, newXpathEventable("/index/5"));
		graph.addEdge(state4, state2, newXpathEventable("/4/2"));
		graph.addEdge(state3, state5, newXpathEventable("/3/5"));

		List<List<GraphPath<StateVertex, Eventable>>> results = graph.getAllPossiblePaths(index);

		assertThat(results, hasSize(2));

		assertThat(results.get(0), hasSize(5));

		assertThat(results.get(0).get(0).getEdgeList(), hasSize(1));

		assertThat(results.get(1), hasSize(2));
		// int max = 0;
		Set<Eventable> uEvents = new HashSet<>();

		for (List<GraphPath<StateVertex, Eventable>> paths : results) {
			for (GraphPath<StateVertex, Eventable> p : paths) {
				// int z = 0;
				for (Eventable edge : p.getEdgeList()) {
					if (!uEvents.contains(edge)) {
						uEvents.add(edge);
					}

				}
			}
		}
	}

}

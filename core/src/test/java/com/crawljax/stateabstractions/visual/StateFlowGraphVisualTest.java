package com.crawljax.stateabstractions.visual;

import com.crawljax.core.ExitNotifier;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.stateabstractions.visual.imagehashes.DHashStateVertexFactory;
import com.crawljax.stateabstractions.visual.imagehashes.DHashStateVertexImpl;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateVertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;

public class StateFlowGraphVisualTest {

	private StateVertex index;
	private StateVertex state2;
	private StateVertex state3;
	private StateVertex state4;
	private StateVertex state5;
	private InMemoryStateFlowGraph graph;

	@Before
	public void setup() {
		index = new DHashStateVertexImpl(StateVertex.INDEX_ID, "index",
				"<table><div>index</div></table>", "001");
		state2 = new DHashStateVertexImpl(2, "STATE_TWO", "<table><div>state2</div></table>",
				"002");
		state3 = new DHashStateVertexImpl(3, "STATE_THREE", "<table><div>state3</div></table>",
				"003");
		state4 = new DHashStateVertexImpl(4, "STATE_FOUR", "<table><div>state4</div></table>",
				"004");
		state5 = new DHashStateVertexImpl(5, "STATE_FIVE", "<table><div>state5</div></table>",
				"005");
		graph = new InMemoryStateFlowGraph(new ExitNotifier(0), new DHashStateVertexFactory());
		graph.putIndex(index);
	}

	@Test
	public void testDuplicationAdding() {
		assertThat(graph.putIfAbsent(index), is(not(nullValue())));
		assertThat(graph.putIfAbsent(state2), is(nullValue()));
		assertThat(graph.putIfAbsent(state3), is(nullValue()));
		assertThat(graph.putIfAbsent(state4), is(nullValue()));
		assertThat(graph.putIfAbsent(state5), is(nullValue()));

		/*
		 * should not be able to add a new state with the same PHash value (should
		 * return a non-null state equivalent to this one.
		 */
		StateVertex stateReturned = graph.putIfAbsent(
				new DHashStateVertexImpl(12, "STATE_12",
						"<table><div><span>state22</span></div></table>", "002"));

		assertThat(stateReturned, is(not(nullValue())));

		assertThat(((DHashStateVertexImpl) stateReturned).getDHashVisual(), is("002"));

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

		assertTrue(state2.equals(
				new DHashStateVertexImpl(2, "STATE_2", "<table><div>state2</div></table>",
						"002")));

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
				new DHashStateVertexImpl(StateVertex.INDEX_ID, index.getName(), index.getDom(),
						"001");

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
	public void testCloneStates() {
		StateVertex state3clone2 =
				new DHashStateVertexImpl(3, "STATE_THREE", "<table><div>state2</div></table>",
						"002");

		assertTrue(graph.putIfAbsent(state2) == null);
		assertTrue(graph.putIfAbsent(state4) == null);
		assertTrue(graph.addEdge(index, state2, newXpathEventable("/body/div[4]")));

		assertTrue(graph.addEdge(state4, state3clone2,
				new Eventable(new Identification(How.xpath, "/home/a"), EventType.click)));
		System.out.println(graph.toString());
	}

}

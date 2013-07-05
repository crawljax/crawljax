package com.crawljax.core.state;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.junit.Test;

import com.crawljax.core.ExitNotifier;

public class InMemoryStateFlowGraphTest extends StateFlowGraphTest {

	public StateFlowGraph createStateFlowGraph() {
		return new InMemoryStateFlowGraph(new ExitNotifier(0));

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

		List<List<GraphPath<StateVertex, Eventable>>> results =
		        ((InMemoryStateFlowGraph) graph).getAllPossiblePaths(index);

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

		List<List<GraphPath<StateVertex, Eventable>>> results =
		        ((InMemoryStateFlowGraph) graph).getAllPossiblePaths(index);

		assertThat(results, hasSize(2));

		assertThat(results.get(0), hasSize(5));

		assertThat(results.get(0).get(0).getEdgeList(), hasSize(1));

		assertThat(results.get(1), hasSize(2));
		Set<Eventable> uEvents = new HashSet<Eventable>();

		for (List<GraphPath<StateVertex, Eventable>> paths : results) {
			for (GraphPath<StateVertex, Eventable> p : paths) {
				for (Eventable edge : p.getEdgeList()) {
					if (!uEvents.contains(edge)) {
						uEvents.add(edge);
					}

				}
			}
		}
	}

}

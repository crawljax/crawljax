/**
 * 
 */
package com.crawljax.core.state;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.crawljax.core.ExitNotifier;

/**
 * @author arz
 */
public class InDatabaseStateFlowGraphTest extends StateFlowGraphTest {

	/**
	 * @throws java.lang.Exception
	 */

	@Override
	public StateFlowGraph createStateFlowGraph() {
		return new InDatabaseStateFlowGraph(new ExitNotifier(0));
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeStateVertex(com.crawljax.core.state.StateVertex)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeStateVertex(byte[])}.
	 */
	@Test
	public void testSerializationForStateVertexes() {

		testSerializationForOneState(index);
		testSerializationForOneState(state2);
		testSerializationForOneState(state3);
		testSerializationForOneState(state4);
		testSerializationForOneState(state5);

	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeStateVertex(com.crawljax.core.state.StateVertex)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeStateVertex(byte[])}.
	 */
	private void testSerializationForOneState(StateVertex stateVertex)
	{
		byte[] serializedStateVertex = InDatabaseStateFlowGraph.serializeStateVertex(stateVertex);
		StateVertex deserializedStateVertex =
		        InDatabaseStateFlowGraph.deserializeStateVertex(serializedStateVertex);
		assertTrue(stateVertex.equals(deserializedStateVertex));

	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 */
	@Test
	public void testSerializatoinForEventablesWithoutSourceAndWithoutTargetStates() {

		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		testSerializatoinForOneEventable(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 */

	@Test
	public void testSerializatoinForEventablesWithSourceButWithoutTargetStates() {

		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		original.setSource(index);
		testSerializatoinForOneEventable(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 */
	@Test
	public void testSerializatoinForEventables() {

		System.out.println("hi");
		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		original.setSource(index);
		original.setTarget(state4);
		testSerializatoinForOneEventable(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 * 
	 * @param original
	 */
	private void testSerializatoinForOneEventable(Eventable original) {
		byte[] serializedEventable = InDatabaseStateFlowGraph.serializeEventable(original);
		Eventable deserializedEventable =
		        InDatabaseStateFlowGraph.deserializeEventable(serializedEventable);

		assertTrue(original.equals(deserializedEventable));
	}

	/**
	 * Test method for {@link com.crawljax.core.state.InDatabaseStateFlowGraph#buildJgraphT()}.
	 */
	@Test
	public void testBuildJgraphT() {

		graph.putIfAbsent(state2);
		graph.putIfAbsent(state3);
		graph.putIfAbsent(state4);
		graph.putIfAbsent(state5);

		graph.addEdge(index, state2, newXpathEventable("/body/div[12]"));
		graph.addEdge(index, state3, newXpathEventable("/body/div[13]"));
		graph.addEdge(index, state5, newXpathEventable("/body/div[15]"));

		graph.addEdge(state2, index, newXpathEventable("/body/div[21]"));
		graph.addEdge(state2, state3, newXpathEventable("/body/div[23]"));
		graph.addEdge(state2, state4, newXpathEventable("/body/div[24]"));
		graph.addEdge(state2, state5, newXpathEventable("/body/div[25]"));

		InDatabaseStateFlowGraph inDbGraph = (InDatabaseStateFlowGraph) graph;

		assertTrue(inDbGraph.buildJgraphT().edgeSet().size() == 7);

	}

}

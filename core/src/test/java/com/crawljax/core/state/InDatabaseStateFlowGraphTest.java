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
	 * This test case tests if serialization and deserialization leaves the original states
	 * unchanged. Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeStateVertex(com.crawljax.core.state.StateVertex)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeStateVertex(byte[])}.
	 */
	@Test
	public void testSereializationDoesNotAlterOriginalStates() {

		testSereializationDoesNotAlterOriginalState(index);
		testSereializationDoesNotAlterOriginalState(state2);
		testSereializationDoesNotAlterOriginalState(state3);
		testSereializationDoesNotAlterOriginalState(state4);
		testSereializationDoesNotAlterOriginalState(state5);

	}

	/**
	 * This test case tests if serialization and deserialization leaves the original states
	 * unchanged. Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeStateVertex(com.crawljax.core.state.StateVertex)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeStateVertex(byte[])}.
	 */
	private void testSereializationDoesNotAlterOriginalState(
	        StateVertex stateVertex)
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
	public void testEventableWithoutSourceAndTargetStateIsSerializable() {

		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		testSereializationDoesNotAlterOriginalEventable(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 */

	@Test
	public void testEventableWithSourceButWithoutTargetStateIsSerializable() {

		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		original.setSource(index);
		testSereializationDoesNotAlterOriginalEventable(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 */
	@Test
	public void testEventableWithSourceAndTargetStateIsSerializable() {

		System.out.println("hi");
		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		original.setSource(index);
		original.setTarget(state4);
		testSereializationDoesNotAlterOriginalEventable(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 * 
	 * @param original
	 */
	private void testSereializationDoesNotAlterOriginalEventable(Eventable original) {
		byte[] serializedEventable = InDatabaseStateFlowGraph.serializeEventable(original);
		Eventable deserializedEventable =
		        InDatabaseStateFlowGraph.deserializeEventable(serializedEventable);

		assertTrue(original.equals(deserializedEventable));
	}

	/**
	 * Test method for {@link com.crawljax.core.state.InDatabaseStateFlowGraph#buildJgraphT()}.
	 */
	@Test
	public void testJgraphtGraphHasTheExpectedNumberOfEdges() {

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

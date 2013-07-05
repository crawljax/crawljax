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
	public void testWhenStatesAreSereializedTheyRemainUnchanged() {

		testWhenAStateIsSereializedItRemainsUnchanged(index);
		testWhenAStateIsSereializedItRemainsUnchanged(state2);
		testWhenAStateIsSereializedItRemainsUnchanged(state3);
		testWhenAStateIsSereializedItRemainsUnchanged(state4);
		testWhenAStateIsSereializedItRemainsUnchanged(state5);

	}

	/**
	 * This test case tests if serialization and deserialization leaves the original states
	 * unchanged. Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeStateVertex(com.crawljax.core.state.StateVertex)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeStateVertex(byte[])}.
	 */
	private void testWhenAStateIsSereializedItRemainsUnchanged(
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
	public void testWhenAnEventableWithNoSorceNorTargetIsSereializedItRemainsUnchanged() {

		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		testWhenAnEventableIsSereializedItRemainsUnchanged(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 */

	@Test
	public void testWhenAnEventableWitSourceIsSereializedItRemainsUnchanged() {

		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		original.setSource(index);
		testWhenAnEventableIsSereializedItRemainsUnchanged(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 */
	@Test
	public void testWhenAnEventableWithSourceAndTargetIsSereializedItRemainsUnchanged() {

		System.out.println("hi");
		String xPath = "/body/div[4]";
		Eventable original = newXpathEventable(xPath);
		original.setSource(index);
		original.setTarget(state4);
		testWhenAnEventableIsSereializedItRemainsUnchanged(original);
	}

	/**
	 * Test method for
	 * {@link com.crawljax.core.state.InDatabaseStateFlowGraph#serializeEventable(com.crawljax.core.state.Eventable)}
	 * and {@link com.crawljax.core.state.InDatabaseStateFlowGraph#deserializeEventable(byte[])}.
	 * 
	 * @param original
	 */
	private void testWhenAnEventableIsSereializedItRemainsUnchanged(Eventable original) {
		byte[] serializedEventable = InDatabaseStateFlowGraph.serializeEventable(original);
		Eventable deserializedEventable =
		        InDatabaseStateFlowGraph.deserializeEventable(serializedEventable);

		assertTrue(original.equals(deserializedEventable));
	}

	/**
	 * Test method for {@link com.crawljax.core.state.InDatabaseStateFlowGraph#buildJgraphT()}.
	 */
	@Test
	public void testWhenDataBaseIsCovertedToJgraphtTheNumberOfEdgesIsValid() {

		graph.putIfAbsent(state2);
		graph.putIfAbsent(state3);
		graph.putIfAbsent(state4);
		graph.putIfAbsent(state5);

		int numberOfEdges = 0;
		graph.addEdge(index, state2, newXpathEventable("/body/div[12]"));
		numberOfEdges++;
		graph.addEdge(index, state3, newXpathEventable("/body/div[13]"));
		numberOfEdges++;
		graph.addEdge(index, state5, newXpathEventable("/body/div[15]"));
		numberOfEdges++;

		graph.addEdge(state2, index, newXpathEventable("/body/div[21]"));
		numberOfEdges++;
		graph.addEdge(state2, state3, newXpathEventable("/body/div[23]"));
		numberOfEdges++;
		graph.addEdge(state2, state4, newXpathEventable("/body/div[24]"));
		numberOfEdges++;
		graph.addEdge(state2, state5, newXpathEventable("/body/div[25]"));
		numberOfEdges++;

		InDatabaseStateFlowGraph inDbGraph = (InDatabaseStateFlowGraph) graph;

		assertTrue(inDbGraph.buildJgraphT().edgeSet().size() == numberOfEdges);

	}

}

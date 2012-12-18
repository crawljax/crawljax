package com.crawljax.core.state;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;

import com.crawljax.core.state.Eventable.EventType;

public class StateVertexTest {
	private StateVertex s;
	private String name;
	private String dom;

	String HTML = "<SCRIPT src='js/jquery-1.2.1.js' type='text/javascript'></SCRIPT> "
	        + "<SCRIPT src='js/jquery-1.2.3.js' type='text/javascript'></SCRIPT>"
	        + "<body><div id='firstdiv' class='orange'></div><div><span id='thespan'>"
	        + "<a id='thea'>test</a></span></div></body>";

	@Before
	public void setUp() throws Exception {
		name = "index";
		dom = "<body></body>";
		s = new StateVertex(name, dom);
	}

	/**
	 * Test method for {@link com.crawljax.core.state.StateVertex#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		StateVertex state = new StateVertex("foo", dom);
		StateVertex temp = new StateVertex(name, dom);

		assertEquals(temp.hashCode(), state.hashCode());
	}

	/**
	 * Test method for {@link com.crawljax.core.state.StateVertex#StateVertix(java.lang.String)}.
	 */
	@Test
	public void testStateVertixString() {
		StateVertex sv = new StateVertex(name, "");
		assertNotNull(sv);
	}

	@Test
	public void testStateVertixStringString() {
		assertNotNull(s);
	}

	/**
	 * Test method for {@link com.crawljax.core.state.StateVertex#getName()}.
	 */
	@Test
	public void testGetName() {
		assertEquals(name, s.getName());
	}

	/**
	 * Test method for {@link com.crawljax.core.state.StateVertex#getDom()}.
	 */
	@Test
	public void testGetDom() {
		assertEquals(dom, s.getDom());
		// assertEquals(newDom, s.getDomJtidied());
	}

	/**
	 * Test method for {@link com.crawljax.core.state.StateVertex#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		StateVertex stateEqual = new StateVertex("foo", dom);
		StateVertex stateNotEqual = new StateVertex("foo", "<table><div>bla</div</table>");
		StateVertex sv = new StateVertex(name, dom);
		assertTrue(stateEqual.equals(sv));

		assertFalse(stateNotEqual.equals(sv));

		assertFalse(stateEqual.equals(null));
		assertFalse(stateEqual.equals(new Eventable(new Identification(Identification.How.xpath,
		        "/body/div[3]/a"), EventType.click)));

		sv.setGuidedCrawling(true);
		assertFalse(stateEqual.equals(sv));
	}

	/**
	 * Test method for {@link com.crawljax.core.state.StateVertex#toString()}.
	 */
	@Test
	public void testToString() {
		assertNotNull(s.toString());
	}

	@Test
	public void testGetDomSize() {
		StateVertex sv = new StateVertex("test", HTML);

		int count = sv.getDomSize();
		assertEquals(242, count);
	}

	@Test
	public void testSerializability() {
		StateVertex sv = new StateVertex("testSerliazibility", HTML);

		byte[] serializedSv = SerializationUtils.serialize(sv);
		StateVertex deserializedSv = (StateVertex) SerializationUtils.deserialize(serializedSv);
		assertThat(deserializedSv, equalTo(sv));
		assertThat(deserializedSv.getName(), is(sv.getName()));
		assertThat(deserializedSv.getDom(), is(sv.getDom()));

	}
}

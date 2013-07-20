package com.crawljax.core.state;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.SerializationUtils;
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
		s = new StateVertexImpl(0, name, dom);
	}

	@Test
	public void testHashCode() {
		StateVertex state = new StateVertexImpl(1, "foo", dom);
		StateVertex temp = new StateVertexImpl(2, name, dom);

		assertEquals(temp.hashCode(), state.hashCode());
	}

	@Test
	public void testStateVertixString() {
		StateVertex sv = new StateVertexImpl(2, name, "");
		assertNotNull(sv);
	}

	@Test
	public void testStateVertixStringString() {
		assertNotNull(s);
	}

	@Test
	public void testGetName() {
		assertEquals(name, s.getName());
	}

	@Test
	public void testGetDom() {
		assertEquals(dom, s.getDom());
		// assertEquals(newDom, s.getDomJtidied());
	}

	@Test
	public void testEqualsObject() {
		StateVertex stateEqual = new StateVertexImpl(1, "foo", dom);
		StateVertex stateNotEqual = new StateVertexImpl(2, "foo", "<table><div>bla</div</table>");
		StateVertex sv = new StateVertexImpl(1, name, dom);
		assertTrue(stateEqual.equals(sv));

		assertFalse(stateNotEqual.equals(sv));

		assertFalse(stateEqual.equals(null));
		assertFalse(stateEqual.equals(new Eventable(new Identification(Identification.How.xpath,
		        "/body/div[3]/a"), EventType.click)));

	}

	@Test
	public void testToString() {
		assertNotNull(s.toString());
	}

	@Test
	public void testGetDomSize() {
		StateVertex sv = new StateVertexImpl(1, "test", HTML);

		int count = sv.getDom().getBytes().length;
		assertEquals(242, count);
	}

	@Test
	public void testSerializability() {
		StateVertex sv = new StateVertexImpl(2, "testSerliazibility", HTML);

		byte[] serializedSv = SerializationUtils.serialize(sv);
		StateVertex deserializedSv = (StateVertex) SerializationUtils.deserialize(serializedSv);
		assertThat(deserializedSv, equalTo(sv));
		assertThat(deserializedSv.getName(), is(sv.getName()));
		assertThat(deserializedSv.getDom(), is(sv.getDom()));

	}
}

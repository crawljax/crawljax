/**
 * Created Dec 19, 2007
 */
package com.crawljax.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.crawljax.util.Helper;

/**
 * @author mesbah
 * @version $Id$
 */
public class EventableTest {

	@Test
	public void testHashCode() {
		String xpath = "/body/div[3]";
		Identification id = new Identification("xpath", xpath);
		String eventType = "onclick";

		Eventable c = new Eventable(id, eventType);
		Eventable temp = new Eventable(id, eventType);

		assertEquals(temp.hashCode(), c.hashCode());

		temp = new Eventable(new Identification("id", "34"), eventType);
		assertNotSame(temp.hashCode(), c.hashCode());

		temp = new Eventable(id, "onmouseover");
		assertNotSame(temp.hashCode(), c.hashCode());
	}

	@Test
	public void testToString() {
		Eventable c = new Eventable(new Identification("xpath", "/body/div[3]"), "onclick");

		assertNotNull(c.toString());
	}

	/**
	 * Test method for {@link com.crawljax.core.state.Eventable#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Eventable c = new Eventable(new Identification("xpath", "/body/div[3]"), "onclick");
		Eventable b = new Eventable(new Identification("xpath", "/body/div[3]"), "onclick");
		Eventable d = new Eventable(new Identification("id", "23"), "onclick");
		Eventable e = new Eventable(new Identification("id", "23"), "onmouseover");
		assertTrue(c.equals(b));
		assertFalse(c.equals(d));
		assertFalse(d.equals(e));
	}

	@Test
	public void testGetInfo() {
		Eventable c = new Eventable(new Identification("xpath", "/body/div[3]"), "onclick");
		String info = " onclick xpath /body/div[3]";
		assertEquals(info, c.toString());
	}

	@Test
	public void testClickableElement() {
		String html =
		        "<body><div id='firstdiv'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";

		try {
			Document dom = Helper.getDocument(html);
			assertNotNull(dom);

			Element element = dom.getElementById("firstdiv");

			Eventable clickable = new Eventable(element, "onclick");
			assertNotNull(clickable);

			/*
			 * String infoexpected = "DIV: id=firstdiv, xpath /HTML[1]/BODY[1]/DIV[1] onclick";
			 */
			String infoexpected =
			        "ID: firstdivDIV: id=\"firstdiv\" onclick xpath " + "/HTML[1]/BODY[1]/DIV[1]";
			System.out.println(clickable);
			assertEquals(infoexpected, clickable.toString());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testEdge() {

		StateVertix s1 = new StateVertix("stateSource", "dom1");
		StateVertix s2 = new StateVertix("stateTarget", "dom2");
		StateFlowGraph sfg = new StateFlowGraph(s1);

		sfg.addState(s2);

		Eventable e = new Eventable();

		sfg.addEdge(s1, s2, e);
		assertEquals(s1, e.getSourceStateVertix());
		assertEquals(s2, e.getTargetStateVertix());

	}
}

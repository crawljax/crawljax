/**
 * Created Dec 19, 2007
 */
package com.crawljax.core.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.util.Helper;

public class EventableTest {

	private static final Logger LOG = LoggerFactory.getLogger(EventableTest.class);
	
	@Test
	public void testHashCode() {
		String xpath = "/body/div[3]";
		Identification id = new Identification(Identification.How.xpath, xpath);

		Eventable c = new Eventable(id, EventType.click);
		Eventable temp = new Eventable(id, EventType.click);

		assertEquals(temp.hashCode(), c.hashCode());

		temp = new Eventable(new Identification(Identification.How.id, "34"), EventType.click);
		assertNotSame(temp.hashCode(), c.hashCode());

		temp = new Eventable(id, EventType.hover);
		assertNotSame(temp.hashCode(), c.hashCode());
	}

	@Test
	public void testToString() {
		Eventable c =
		        new Eventable(new Identification(Identification.How.xpath, "/body/div[3]"),
		                EventType.click);

		assertNotNull(c.toString());
	}

	/**
	 * Test method for {@link com.crawljax.core.state.Eventable#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		Eventable c =
		        new Eventable(new Identification(Identification.How.xpath, "/body/div[3]"),
		                EventType.click);
		Eventable b =
		        new Eventable(new Identification(Identification.How.xpath, "/body/div[3]"),
		                EventType.click);
		Eventable d =
		        new Eventable(new Identification(Identification.How.id, "23"), EventType.click);
		Eventable e =
		        new Eventable(new Identification(Identification.How.id, "23"), EventType.hover);
		assertTrue(c.equals(b));
		assertFalse(c.equals(d));
		assertFalse(d.equals(e));
	}

	@Test
	public void testGetInfo() {
		Eventable c =
		        new Eventable(new Identification(Identification.How.xpath, "/body/div[3]"),
		                EventType.click);
		String info = " click xpath /body/div[3]";
		assertEquals(info, c.toString());
	}

	@Test
	public void testClickableElement() throws SAXException, IOException {
		String html =
		        "<body><div id='firstdiv'></div><div><span id='thespan'>"
		                + "<a id='thea'>test</a></span></div></body>";

		Document dom = Helper.getDocument(html);
		assertNotNull(dom);

		Element element = dom.getElementById("firstdiv");

		Eventable clickable = new Eventable(element, EventType.click);
		assertNotNull(clickable);

		/*
		 * String infoexpected = "DIV: id=firstdiv, xpath /HTML[1]/BODY[1]/DIV[1] onclick";
		 */
		String infoexpected = "DIV: id=\"firstdiv\" click xpath " + "/HTML[1]/BODY[1]/DIV[1]";
		assertEquals(infoexpected, clickable.toString());
	}

	@Test
	public void testEdge() throws CrawljaxException {

		StateVertex s1 = new StateVertex("stateSource", "dom1");
		StateVertex s2 = new StateVertex("stateTarget", "dom2");
		StateFlowGraph sfg = new StateFlowGraph(s1);

		sfg.addState(s2);

		Eventable e = new Eventable();

		sfg.addEdge(s1, s2, e);
		assertEquals(s1, e.getSourceStateVertex());
		assertEquals(s2, e.getTargetStateVertex());

	}

	@Test
	public void testSets() {
		Eventable c =
		        new Eventable(new Identification(Identification.How.xpath, "/body/div[3]"),
		                EventType.click);
		c.setId(1);
		Eventable b =
		        new Eventable(new Identification(Identification.How.xpath, "/body/div[3]"),
		                EventType.click);
		c.setId(2);
		Eventable d =
		        new Eventable(new Identification(Identification.How.id, "23"), EventType.click);
		c.setId(3);
		Eventable e =
		        new Eventable(new Identification(Identification.How.id, "23"), EventType.hover);
		c.setId(4);
		assertTrue(c.equals(b));
		assertEquals(c.hashCode(), b.hashCode());

		Set<Eventable> setOne = new HashSet<Eventable>();
		setOne.add(b);
		setOne.add(c);
		setOne.add(d);
		setOne.add(e);

		assertEquals(3, setOne.size());

		Set<Eventable> setTwo = new HashSet<Eventable>();
		setTwo.add(b);
		setTwo.add(c);
		setTwo.add(d);

		assertEquals(2, setTwo.size());

		Set<Eventable> intersection = new HashSet<Eventable>(setOne);
		intersection.retainAll(setTwo);

		assertEquals(2, intersection.size());

		Set<Eventable> difference = new HashSet<Eventable>(setOne);
		difference.removeAll(setTwo);

		assertEquals(1, difference.size());

	}
}

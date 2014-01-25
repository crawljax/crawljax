/**
 * Created Dec 19, 2007
 */
package com.crawljax.core.state;

import static com.crawljax.core.state.Identification.How.xpath;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.util.DomUtils;

public class EventableTest {

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
	public void EventablesWithDifferentStatesAreNotEqual() throws IllegalArgumentException,
	        IllegalAccessException, NoSuchFieldException, SecurityException {
		Identification id = new Identification(Identification.How.xpath, "/DIV");
		Eventable event1 = new Eventable(id, EventType.click);
		Eventable event2 = new Eventable(id, EventType.click);
		assertThat(event1, is(event2));

		StateVertex source = Mockito.mock(StateVertex.class);
		StateVertex target1 = Mockito.mock(StateVertex.class);
		StateVertex target2 = Mockito.mock(StateVertex.class);
		event1.setSource(source);
		event2.setSource(source);
		assertThat(event1, is(event2));
		event1.setTarget(target1);
		event2.setTarget(target2);
		assertThat(event1, is(not(event2)));
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
	@Ignore("seems redundant")
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

		Document dom = DomUtils.asDocument(html);
		assertNotNull(dom);

		Element element = dom.getElementById("firstdiv");

		Eventable clickable = new Eventable(element, EventType.click);
		assertNotNull(clickable);

		assertThat(clickable.getIdentification().getHow(), is(xpath));
		assertThat(clickable.getIdentification().getValue(), is("/HTML[1]/BODY[1]/DIV[1]"));
		assertThat(clickable.getElement().getAttributeOrNull("id"), is("firstdiv"));
	}

	@Test
	public void testEdge() throws CrawljaxException {

		StateVertex s1 = new StateVertexImpl(0, "stateSource", "dom1");
		StateVertex s2 = new StateVertexImpl(0, "stateTarget", "dom2");
		InMemoryStateFlowGraph sfg = new InMemoryStateFlowGraph(new ExitNotifier(0), new DefaultStateVertexFactory());
		sfg.putIndex(s1);

		sfg.putIfAbsent(s2);

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

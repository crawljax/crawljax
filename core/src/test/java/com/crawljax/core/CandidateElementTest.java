package com.crawljax.core;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.crawljax.forms.FormInput;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableList;

/**
 * Test class for the CandidateElement class.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public class CandidateElementTest {
	private static Document document;

	private final List<FormInput> noFormInput = ImmutableList.of();

	private CandidateElement c;

	private Element e;

	@BeforeClass
	public static void setupOnce() throws SAXException, IOException {
		document = DomUtils.asDocument("");
	}

	@Before
	public void setup() {
		e = document.createElement("test");
		c = new CandidateElement(e, "", noFormInput);
	}

	@Test
	public void testEmptyElement() throws SAXException, IOException {
		Assert.assertEquals("General String and Unique String are the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST:  xpath", c.getGeneralString().trim());
	}

	@Test
	public void testOneAttribureElement() throws SAXException, IOException {
		e.setAttribute("id", "abc");
		Assert.assertEquals("General String and Unique String are the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: id=abc xpath", c.getGeneralString().trim());
	}

	@Test
	public void testTwoAttribureElement() throws SAXException, IOException {
		e.setAttribute("id", "abc");
		e.setAttribute("class", "def");
		Assert.assertEquals("General String and Unique String are the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: class=def id=abc xpath", c
		        .getGeneralString().trim());
	}

	@Test
	public void testOneAttribureElementWithAtusa() throws SAXException, IOException {
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		Assert.assertNotSame("General String and Unique String are not the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: id=abc xpath", c.getGeneralString().trim());
		Assert.assertEquals("Excpected result", "TEST: atusa=ignore id=abc xpath", c
		        .getUniqueString().trim());
	}

	@Test
	public void testTwoAttribureElementWithAtusa() throws SAXException, IOException {
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		e.setAttribute("class", "def");
		Assert.assertNotSame("General String and Unique String are not the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: class=def id=abc xpath", c
		        .getGeneralString().trim());
		Assert.assertEquals("Excpected result", "TEST: atusa=ignore class=def id=abc xpath", c
		        .getUniqueString().trim());
	}

	@Test
	public void testMultipleAttribureElementWithAtusaOrderedAlphabetical() throws SAXException,
	        IOException {
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		e.setAttribute("class", "def");
		e.setAttribute("z", "z");
		e.setAttribute("a", "a");
		e.setAttribute("x", "a");

		Assert.assertNotSame("General String and Unique String are not the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: a=a class=def id=abc x=a z=z xpath", c
		        .getGeneralString().trim());
		Assert.assertEquals("Excpected result",
		        "TEST: a=a atusa=ignore class=def id=abc x=a z=z xpath", c.getUniqueString()
		                .trim());
	}

}

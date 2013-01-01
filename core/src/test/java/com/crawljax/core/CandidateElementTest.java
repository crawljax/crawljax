package com.crawljax.core;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.crawljax.util.Helper;

/**
 * Test class for the CandidateElement class.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id$
 */
public class CandidateElementTest {
	private static Document document;

	@BeforeClass
	public static void setup() throws SAXException, IOException {
		document = Helper.getDocument("");
	}

	@Test
	public void testEmptyElement() throws SAXException, IOException {
		Element e = document.createElement("test");
		CandidateElement c = new CandidateElement(e, "");
		Assert.assertEquals("General String and Unique String are the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST:  xpath", c.getGeneralString().trim());
	}

	@Test
	public void testOneAttribureElement() throws SAXException, IOException {
		Element e = document.createElement("test");
		e.setAttribute("id", "abc");
		CandidateElement c = new CandidateElement(e, "");
		Assert.assertEquals("General String and Unique String are the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: id=abc xpath", c.getGeneralString().trim());
	}

	@Test
	public void testTwoAttribureElement() throws SAXException, IOException {
		Element e = document.createElement("test");
		e.setAttribute("id", "abc");
		e.setAttribute("class", "def");
		CandidateElement c = new CandidateElement(e, "");
		Assert.assertEquals("General String and Unique String are the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: class=def id=abc xpath", c
		        .getGeneralString().trim());
	}

	@Test
	public void testOneAttribureElementWithAtusa() throws SAXException, IOException {
		Element e = document.createElement("test");
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		CandidateElement c = new CandidateElement(e, "");
		Assert.assertNotSame("General String and Unique String are not the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: id=abc xpath", c.getGeneralString().trim());
		Assert.assertEquals("Excpected result", "TEST: atusa=ignore id=abc xpath", c
		        .getUniqueString().trim());
	}

	@Test
	public void testTwoAttribureElementWithAtusa() throws SAXException, IOException {
		Element e = document.createElement("test");
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		e.setAttribute("class", "def");
		CandidateElement c = new CandidateElement(e, "");
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
		Element e = document.createElement("test");
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		e.setAttribute("class", "def");
		e.setAttribute("z", "z");
		e.setAttribute("a", "a");
		e.setAttribute("x", "a");

		CandidateElement c = new CandidateElement(e, "");
		Assert.assertNotSame("General String and Unique String are not the same",
		        c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Excpected result", "TEST: a=a class=def id=abc x=a z=z xpath", c
		        .getGeneralString().trim());
		Assert.assertEquals("Excpected result",
		        "TEST: a=a atusa=ignore class=def id=abc x=a z=z xpath", c.getUniqueString()
		                .trim());
	}

}

package com.crawljax.core;

import com.crawljax.forms.FormInput;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;

/**
 * Test class for the CandidateElement class.
 *
 * @author Stefan Lenselink &lt;S.R.Lenselink@student.tudelft.nl&gt;
 */
public class CandidateElementTest {
	private static Document document;

	private final List<FormInput> noFormInput = ImmutableList.of();

	private CandidateElement c;

	private Element e;

	@BeforeClass
	public static void setupOnce() throws IOException {
		document = DomUtils.asDocument("");
	}

	@Before
	public void setup() {
		e = document.createElement("test");
		c = new CandidateElement(e, "", noFormInput);
	}

	@Test
	public void testEmptyElement() {
		Assert.assertEquals("General String and Unique String are the same",
				c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Expected result", "TEST:  xpath", c
				.getGeneralString().trim());
	}

	@Test
	public void testOneAttributeElement() {
		e.setAttribute("id", "abc");
		Assert.assertEquals("General String and Unique String are the same",
				c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Expected result", "TEST: id=abc xpath", c
				.getGeneralString().trim());
	}

	@Test
	public void testTwoAttributeElement() {
		e.setAttribute("id", "abc");
		e.setAttribute("class", "def");
		Assert.assertEquals("General String and Unique String are the same",
				c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Expected result", "TEST: class=def id=abc xpath",
				c.getGeneralString().trim());
	}

	@Test
	public void testOneAttributeElementWithAtusa() {
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		Assert.assertNotSame(
				"General String and Unique String are not the same",
				c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Expected result", "TEST: id=abc xpath", c
				.getGeneralString().trim());
		Assert.assertEquals("Expected result",
				"TEST: atusa=ignore id=abc xpath", c.getUniqueString().trim());
	}

	@Test
	public void testTwoAttributeElementWithAtusa() {
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		e.setAttribute("class", "def");
		Assert.assertNotSame(
				"General String and Unique String are not the same",
				c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Expected result", "TEST: class=def id=abc xpath",
				c.getGeneralString().trim());
		Assert.assertEquals("Expected result",
				"TEST: atusa=ignore class=def id=abc xpath", c
						.getUniqueString().trim());
	}

	@Test
	public void testMultipleAttributeElementWithAtusaOrderedAlphabetical() {
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "ignore");
		e.setAttribute("class", "def");
		e.setAttribute("z", "z");
		e.setAttribute("a", "a");
		e.setAttribute("x", "a");

		Assert.assertNotSame(
				"General String and Unique String are not the same",
				c.getGeneralString(), c.getUniqueString());
		Assert.assertEquals("Expected result",
				"TEST: a=a class=def id=abc x=a z=z xpath", c
						.getGeneralString().trim());
		Assert.assertEquals("Expected result",
				"TEST: a=a atusa=ignore class=def id=abc x=a z=z xpath", c
						.getUniqueString().trim());
	}

}

package com.crawljax.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.crawljax.core.state.Identification;

/**
 * Since many of the AbstractConditions override equals and hashcode, we provide some test cases for
 * them.
 */

@RunWith(Parameterized.class)
public class ConditionTest {

	Condition self;
	Condition equivalent;
	Condition different1;
	Condition different2;

	/**
	 * @param _self
	 *            We compare equivalence against self.
	 * @param _equivalent
	 *            This condition should be equivalent.
	 * @param _different1
	 *            This one should be different -- typically different arguments
	 * @param _different2
	 *            This one should be different -- same arguments, different constructor
	 */
	public ConditionTest(Condition _self, Condition _equivalent, Condition _different1,
	        Condition _different2) {
		this.self = _self;
		this.equivalent = _equivalent;
		this.different1 = _different1;
		this.different2 = _different2;
	}

	@Parameters(name = "c{index}, {1}")
	public static Collection<Object[]> data() {
		return Arrays
		        .asList(new Object[][] {
		                {
		                        new RegexCondition("rx0"),
		                        new RegexCondition("rx0"),
		                        new RegexCondition("rx1"),
		                        new UrlCondition("rx0")
		        },
		                {
		                        new NotRegexCondition("nrx0"),
		                        new NotRegexCondition("nrx0"),
		                        new NotRegexCondition("nrx1"),
		                        Logic.and(new RegexCondition("nrx0"))
		        },
		                {
		                        new UrlCondition("u0"),
		                        new UrlCondition("u0"),
		                        new UrlCondition("u1"),
		                        new JavaScriptCondition("u0")
		        },
		                {
		                        new NotUrlCondition("u0"),
		                        new NotUrlCondition("u0"),
		                        new NotUrlCondition("u1"),
		                        Logic.and(new UrlCondition("u0"))
		        },
		                {
		                        new XPathCondition("x0"),
		                        new XPathCondition("x0"),
		                        new XPathCondition("x1"),
		                        new JavaScriptCondition("x0")
		        },
		                {
		                        new NotXPathCondition("x0"),
		                        new NotXPathCondition("x0"),
		                        new NotXPathCondition("x1"),
		                        Logic.and(new XPathCondition("x0"))
		        },
		                {
		                        new VisibleCondition(new Identification(Identification.How.xpath,
		                                "xp0")),
		                        new VisibleCondition(new Identification(Identification.How.xpath,
		                                "xp0")),
		                        new VisibleCondition(new Identification(Identification.How.tag,
		                                "xp0")),
		                        new VisibleCondition(new Identification(Identification.How.xpath,
		                                "xp1"))
		        },
		                {
		                        new NotVisibleCondition(new Identification(
		                                Identification.How.xpath, "xp")),
		                        new NotVisibleCondition(new Identification(
		                                Identification.How.xpath, "xp")),
		                        new NotVisibleCondition(new Identification(
		                                Identification.How.tag, "xp")),
		                        Logic.and(new VisibleCondition(new Identification(
		                                Identification.How.xpath, "xp")))
		        },
		                {
		                        new JavaScriptCondition("0"),
		                        new JavaScriptCondition("0"),
		                        new JavaScriptCondition("1"),
		                        new UrlCondition("0")
		        },
		                {
		                        Logic.not(new UrlCondition("u0")),
		                        Logic.not(new UrlCondition("u0")),
		                        Logic.not(new UrlCondition("u1")),
		                        Logic.and(new UrlCondition("u0"))
		        },
		                {
		                        Logic.and(new JavaScriptCondition("js0")),
		                        Logic.and(new JavaScriptCondition("js0")),
		                        Logic.and(new JavaScriptCondition("js1")),
		                        Logic.not(new JavaScriptCondition("js0"))
		        },
		                {
		                        Logic.nand(new JavaScriptCondition("js0")),
		                        Logic.nand(new JavaScriptCondition("js0")),
		                        Logic.nand(new JavaScriptCondition("js1")),
		                        Logic.and(new JavaScriptCondition("js0"))
		        },
		                {
		                        Logic.or(new JavaScriptCondition("js0")),
		                        Logic.or(new JavaScriptCondition("js0")),
		                        Logic.or(new JavaScriptCondition("js1")),
		                        Logic.and(new JavaScriptCondition("js0"))
		        },
		                {
		                        new CountCondition(2, new JavaScriptCondition("js0")),
		                        new CountCondition(2, new JavaScriptCondition("js0")),
		                        new CountCondition(2, new JavaScriptCondition("js1")),
		                        new CountCondition(1, new JavaScriptCondition("js0"))
		        }

		        });
	}

	@Test
	public void testEquality() {
		// Structurally equivalent, symmetrical.
		assertEquals(self, equivalent);
		assertEquals(equivalent, self);

		// Same type, different content, symmetrical.
		assertNotEquals(self, different1);
		assertNotEquals(different1, self);

		// Different types
		assertNotEquals(self, null);
		assertNotEquals(self, different2);

		// Reflexive.
		assertEquals(self, self);
	}

	@Test
	public void testToString() {
		assertEquals(self.toString(), equivalent.toString());
		assertNotEquals(self.toString(), different1.toString());
		assertNotEquals(self.toString(), different2.toString());
	}

	@Test
	public void testHashCode() {
		assertEquals(self.hashCode(), equivalent.hashCode());
		assertNotEquals(self.hashCode(), different1.hashCode());
		assertNotEquals(self.hashCode(), different2.hashCode());
	}
}

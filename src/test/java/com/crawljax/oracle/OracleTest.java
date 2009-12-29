package com.crawljax.oracle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.crawljax.oracle.oracles.AttributeOracle;
import com.crawljax.oracle.oracles.DateOracle;
import com.crawljax.oracle.oracles.PlainStructureOracle;
import com.crawljax.oracle.oracles.RegexOracle;
import com.crawljax.oracle.oracles.SimpleOracle;
import com.crawljax.oracle.oracles.StyleOracle;

/**
 * @author danny
 * @version $Id: OracleTest.java 6387 2009-12-29 13:20:30Z danny $
 */
public class OracleTest {

	@Test
	public void testDateOracle() {

		Oracle oracle = new DateOracle();
		String[] tests =
		        {
		        /* dates with days */
		        "<HTML>Monday 15 march 1998</HTML>|<HTML>Tuesday 13 december 2005</HTML>",
		                "<HTML>Monday 1 feb '98</HTML>|<HTML>Wednesday 15 march '00</HTML>",
		                "<HTML>Friday 10 february</HTML>|<HTML>Wednesday 3 march</HTML>",

		                /* dates only numeric */
		                "<HTML>28-12-1983</HTML>|<HTML>15-3-1986</HTML>",
		                "<HTML>28.1.1976</HTML>|<HTML>3.15.1986</HTML>",
		                "<HTML>1/1/2001</HTML>|<HTML>30/12/1988</HTML>",

		                "<HTML>28-12-1983</HTML>|<HTML>19-2-1986</HTML>",
		                "<HTML>28.1.1976</HTML>|<HTML>3.15.1986</HTML>",
		                "<HTML>1/1/2001</HTML>|<HTML>30/12/1988</HTML>",

		                "<HTML>28-12-'83</HTML>|<HTML>19-1-'86</HTML>",
		                "<HTML>28.1.'76</HTML>|<HTML>3.15.'86</HTML>",
		                "<HTML>1/1/'01</HTML>|<HTML>30/12/'88</HTML>",

		                "<HTML>2003-16-03</HTML>|<HTML>1986-3-3</HTML>",
		                "<HTML>1993.12.12</HTML>|<HTML>1997.13.09</HTML>",
		                "<HTML>2013/1/3</HTML>|<HTML>1986/3/3</HTML>",

		                /* dates with long months */
		                "<HTML>19 november 1986</HTML>|<HTML>18 june 1973</HTML>",
		                "<HTML>1th march 1986</HTML>|<HTML>28th december 2005</HTML>",
		                "<HTML>15th november</HTML>|<HTML>3th july</HTML>",

		                "<HTML>2003 March 15</HTML>|<HTML>1978 july 5</HTML>",
		                "<HTML>2003Apr15</HTML>|<HTML>1978jul5</HTML>",

		                "<HTML>March 2003</HTML>|<HTML>October 1996</HTML>",
		                "<HTML>April '02</HTML>|<HTML>August '99</HTML>",

		                "<HTML>April 19 2007</HTML>|<HTML>January 1 1994</HTML>",
		                "<HTML>April 19, 2007</HTML>|<HTML>January 1, 1994</HTML>",
		                "<HTML>April 4 '07</HTML>|<HTML>January 1 '87</HTML>",
		                "<HTML>April 19, '66</HTML>|<HTML>January 1, '88</HTML>",

		                /* time */
		                "<HTML>4:47:00 am</HTML>|<HTML>3:59:2PM</HTML>",
		                "<HTML>2:13pm</HTML>|<HTML>3:59am</HTML>",
		                "<HTML>14:17:29</HTML>|<HTML>7:34:26</HTML>",
		                "<HTML>23:15</HTML>|<HTML>03:57</HTML>" };

		for (int i = 0; i < tests.length; i++) {
			String[] test = tests[i].split("\\|");
			oracle.setOriginalDom(test[0]);
			oracle.setNewDom(test[1]);
			boolean equivalent = oracle.isEquivalent();
			if (!equivalent) {
				System.out.println(tests[i] + "\nStripped original: " + oracle.getOriginalDom()
				        + "\n" + "Stripped new:      " + oracle.getNewDom());
			}
			assertTrue(test[0] + " EQUIVALENT WITH + " + test[1], equivalent);
		}

	}

	@Test
	public void testStyleOracle() {

		Oracle oracle = new StyleOracle();
		String[] tests =
		        {
		                /* IGNORE_TAGS */
		                "<HTML><B>foo</B></HTML>|<HTML>foo</HTML>",
		                "<HTML><PRE>foo</PRE></HTML>|<HTML><STRONG>foo</STRONG></HTML>",
		                "<HTML><FONT color=\"red\">foo</FONT> bar</HTML>|<HTML>foo bar</HTML>",
		                "<HTML><FONT color=\"red\">foo</FONT> bar</HTML>|<HTML>"
		                        + "<FONT color=\"green\">foo</FONT> bar</HTML>",

		                /* IGNORE_ATTRIBUTES */
		                "<HTML><SPAN width=\"100px\">foo</SPAN></HTML>|<HTML><SPAN>foo</SPAN></HTML>",
		                "<HTML><SPAN>foo</SPAN></HTML>|<HTML><SPAN valign=\"top\">foo</SPAN></HTML>",

		                /* STYLE ATTRIBUTES */
		                "<HTML><SPAN style=\"color:  green;\">foo</SPAN></HTML>|<HTML>"
		                        + "<SPAN style=\"color:red;\">foo</SPAN></HTML>",
		                "<HTML><SPAN style=\"color: yellow\">foo</SPAN></HTML>|<HTML>"
		                        + "<SPAN>foo</SPAN></HTML>",
		                "<HTML><SPAN style=\"display:inline;color:red;\">foo</SPAN>"
		                        + "</HTML>|<HTML><SPAN style=\"display:inline; color:green;\">foo</SPAN>"
		                        + "</HTML>", };

		for (int i = 0; i < tests.length; i++) {
			// System.out.println("Test: " + tests[i]);
			String[] test = tests[i].split("\\|");
			oracle.setOriginalDom(test[0]);
			oracle.setNewDom(test[1]);
			boolean equivalent = oracle.isEquivalent();
			if (!equivalent) {
				System.out.println(tests[i] + "\nStripped original: " + oracle.getOriginalDom()
				        + "\n" + "Stripped new:      " + oracle.getNewDom());
			}
			assertTrue(test[0] + " EQUIVALENT WITH " + test[1], equivalent);
		}

		String[] wrongtests =
		        { "<HTML><SPAN style=\"display:inline;color:red;\">foo</SPAN>"
		                + "</HTML>|<HTML><SPAN style=\"display:none; color:green;\">foo</SPAN></HTML>", };

		for (String wrongtest : wrongtests) {
			String[] test = wrongtest.split("\\|");
			oracle.setOriginalDom(test[0]);
			oracle.setNewDom(test[1]);
			boolean equivalent = oracle.isEquivalent();
			if (equivalent) {
				System.out.println("Stripped original: " + oracle.getOriginalDom() + "\n"
				        + "Stripped new:      " + oracle.getNewDom());
			}
			assertFalse(test[0] + " NOT EQUIVALENT WITH " + test[1], equivalent);
		}

	}

	@Test
	public void testSimpleOracle() {
		Oracle oracle = new SimpleOracle();
		String[] tests =
		        { "<HTML>\n\n<SPAN>\n    foo\n</SPAN></HTML>|"
		                + "<HTML>\n<SPAN>\n    foo     \n\n</SPAN>\n</HTML>", };

		for (String t : tests) {
			String[] test = t.split("\\|");
			oracle.setOriginalDom(test[0]);
			oracle.setNewDom(test[1]);
			boolean equivalent = oracle.isEquivalent();
			if (!equivalent) {
				System.out.println("Stripped original: " + oracle.getOriginalDom() + "\n"
				        + "Stripped new:      " + oracle.getNewDom());
			}
			assertTrue(test[0] + " EQUIVALENT WITH " + test[1], equivalent);
		}
	}

	@Test
	public void testRegexOracle() {
		RegexOracle oracle = new RegexOracle("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
		String[] tests = { "<HTML>192.168.1.1</HTML>|<HTML>10.0.0.138</HTML>", };
		for (String t : tests) {
			String[] test = t.split("\\|");
			oracle.setOriginalDom(test[0]);
			oracle.setNewDom(test[1]);
			// oracle.compare();
			boolean equivalent = oracle.isEquivalent();
			if (!equivalent) {
				System.out.println("Stripped original: " + oracle.getOriginalDom() + "\n"
				        + "Stripped new:      " + oracle.getNewDom());
			}
			assertTrue(test[0] + " EQUIVALENT WITH " + test[1], equivalent);
		}
	}

	@Test
	public void testAttributeOracle() {
		String control = "<HTML><A href=\"foo.html\" myattr=\"true\">foo</A><HTML>";
		String test = "<HTML><A href=\"foo.html\" myattr=\"false\">foo</A><HTML>";
		AttributeOracle oracle = new AttributeOracle("myattr");
		oracle.setOriginalDom(control);
		oracle.setNewDom(test);
		boolean equivalent = oracle.isEquivalent();
		if (!equivalent) {
			System.out.println("Stripped original: " + oracle.getOriginalDom() + "\n"
			        + "Stripped new:      " + oracle.getNewDom());
		}
		assertTrue(equivalent);
	}

	@Test
	public void testPlainStructureOracle() {
		String control =
		        "<HTML><A href=\"foo.html\" jquery12421421=\"bla\" myattr=\"true\">foo</A><HTML>";
		String test = "<HTML><A></A><HTML>";
		PlainStructureOracle oracle = new PlainStructureOracle(control, test);
		boolean equivalent = oracle.isEquivalent();
		if (!equivalent) {
			System.out.println("Stripped original: " + oracle.getOriginalDom() + "\n"
			        + "Stripped new:      " + oracle.getNewDom());
		}
		assertTrue(equivalent);
	}

}

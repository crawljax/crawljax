package com.crawljax.oracle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.crawljax.oraclecomparator.Comparator;
import com.crawljax.oraclecomparator.comparators.AttributeComparator;
import com.crawljax.oraclecomparator.comparators.DateComparator;
import com.crawljax.oraclecomparator.comparators.EditDistanceComparator;
import com.crawljax.oraclecomparator.comparators.PlainStructureComparator;
import com.crawljax.oraclecomparator.comparators.RegexComparator;
import com.crawljax.oraclecomparator.comparators.ScriptComparator;
import com.crawljax.oraclecomparator.comparators.SimpleComparator;
import com.crawljax.oraclecomparator.comparators.StyleComparator;
import com.crawljax.oraclecomparator.comparators.XPathExpressionComparator;

public class OracleTest {

	private void compareTwoDomsWithComparatorEqual(String original, String newDom,
	        Comparator comparator) {
		assertTrue(comparator.isEquivalent(original, newDom));
	}

	private void compareTwoDomsWithComparatorNotEqual(String original, String newDom,
	        Comparator comparator) {
		assertFalse(comparator.isEquivalent(original, newDom));
	}

	@Test
	public void testDateOracle() {

		Comparator oracle = new DateComparator();

		/* dates with days */
		compareTwoDomsWithComparatorEqual("<HTML>Monday 15 march 1998</HTML>",
		        "<HTML>Tuesday 13 december 2005</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>Monday 1 feb '98</HTML>",
		        "<HTML>Wednesday 15 march '00</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>Friday 10 february</HTML>",
		        "<HTML>Wednesday 3 march</HTML>", oracle);

		/* dates only numeric */
		compareTwoDomsWithComparatorEqual("<HTML>28-12-1983</HTML>", "<HTML>15-3-1986</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>28.1.1976</HTML>", "<HTML>3.15.1986</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>1/1/2001</HTML>", "<HTML>30/12/1988</HTML>",
		        oracle);

		compareTwoDomsWithComparatorEqual("<HTML>28-12-1983</HTML>", "<HTML>19-2-1986</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>28.1.1976</HTML>", "<HTML>3.15.1986</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>1/1/2001</HTML>", "<HTML>30/12/1988</HTML>",
		        oracle);

		compareTwoDomsWithComparatorEqual("<HTML>28-12-'83</HTML>", "<HTML>19-1-'86</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>28.1.'76</HTML>", "<HTML>3.15.'86</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>1/1/'01</HTML>", "<HTML>30/12/'88</HTML>",
		        oracle);

		compareTwoDomsWithComparatorEqual("<HTML>2003-16-03</HTML>", "<HTML>1986-3-3</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>1993.12.12</HTML>", "<HTML>1997.13.09</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>2013/1/3</HTML>", "<HTML>1986/3/3</HTML>",
		        oracle);

		/* dates with long months */
		compareTwoDomsWithComparatorEqual("<HTML>19 november 1986</HTML>",
		        "<HTML>18 june 1973</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>1th march 1986</HTML>",
		        "<HTML>28th december 2005</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>15th november</HTML>", "<HTML>3th july</HTML>",
		        oracle);

		compareTwoDomsWithComparatorEqual("<HTML>2003 March 15</HTML>",
		        "<HTML>1978 july 5</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>2003Apr15</HTML>", "<HTML>1978jul5</HTML>",
		        oracle);

		compareTwoDomsWithComparatorEqual("<HTML>March 2003</HTML>", "<HTML>October 1996</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>April '02</HTML>", "<HTML>August '99</HTML>",
		        oracle);

		compareTwoDomsWithComparatorEqual("<HTML>April 19 2007</HTML>",
		        "<HTML>January 1 1994</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>April 19, 2007</HTML>",
		        "<HTML>January 1, 1994</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>April 4 '07</HTML>",
		        "<HTML>January 1 '87</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>April 19, '66</HTML>",
		        "<HTML>January 1, '88</HTML>", oracle);

		/* time */
		compareTwoDomsWithComparatorEqual("<HTML>4:47:00 am</HTML>", "<HTML>3:59:2PM</HTML>",
		        oracle);
		compareTwoDomsWithComparatorEqual("<HTML>2:13pm</HTML>", "<HTML>3:59am</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML>14:17:29</HTML>", "<HTML>7:34:26</HTML>", oracle);
	}

	@Test
	public void testStyleOracle() {

		Comparator oracle = new StyleComparator();
		/* IGNORE_TAGS */
		compareTwoDomsWithComparatorEqual("<HTML><B>foo</B></HTML>", "<HTML>foo</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML><PRE>foo</PRE></HTML>",
		        "<HTML><STRONG>foo</STRONG></HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML><FONT color=\"red\">foo</FONT> bar</HTML>",
		        "<HTML>foo bar</HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML><FONT color=\"red\">foo</FONT> bar</HTML>",
		        "<HTML><FONT color=\"green\">foo</FONT> bar</HTML>", oracle);

		/* IGNORE_ATTRIBUTES */
		compareTwoDomsWithComparatorEqual("<HTML><SPAN width=\"100px\">foo</SPAN></HTML>",
		        "<HTML><SPAN>foo</SPAN></HTML>", oracle);
		compareTwoDomsWithComparatorEqual("<HTML><SPAN>foo</SPAN></HTML>",
		        "<HTML><SPAN valign=\"top\">foo</SPAN></HTML>", oracle);

		/* STYLE ATTRIBUTES */
		compareTwoDomsWithComparatorEqual(
		        "<HTML><SPAN style=\"color:  green;\">foo</SPAN></HTML>",
		        "<HTML><SPAN style=\"color:red;\">foo</SPAN></HTML>", oracle);
		compareTwoDomsWithComparatorEqual(
		        "<HTML><SPAN style=\"color: yellow\">foo</SPAN></HTML>",
		        "<HTML><SPAN>foo</SPAN></HTML>", oracle);
		compareTwoDomsWithComparatorEqual(
		        "<HTML><SPAN style=\"display:inline;color:red;\">foo</SPAN></HTML>",
		        "<HTML><SPAN style=\"display:inline; color:green;\">foo</SPAN></HTML>", oracle);

		compareTwoDomsWithComparatorNotEqual(
		        "<HTML><SPAN style=\"display:inline;color:red;\">foo</SPAN></HTML>",
		        "<HTML><SPAN style=\"display:none; color:green;\">foo</SPAN></HTML>", oracle);
	}

	@Test
	public void testSimpleOracle() {
		Comparator oracle = new SimpleComparator();
		compareTwoDomsWithComparatorEqual("<HTML>\n\n<SPAN>\n    foo\n</SPAN></HTML>",
		        "<HTML>\n<SPAN>\n    foo     \n\n</SPAN>\n</HTML>", oracle);
	}

	@Test
	public void testRegexOracle() {
		Comparator oracle =
		        new RegexComparator("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
		compareTwoDomsWithComparatorEqual("<HTML>192.168.1.1</HTML>", "<HTML>10.0.0.138</HTML>",
		        oracle);
	}

	@Test
	public void testAttributeOracle() {
		String control = "<HTML><A href=\"foo.html\" myattr=\"true\">foo</A><HTML>";
		String test = "<HTML><A href=\"foo.html\" myattr=\"false\">foo</A><HTML>";
		compareTwoDomsWithComparatorEqual(control, test, new AttributeComparator("myattr"));
	}

	@Test
	public void testPlainStructureOracle() {
		String control =
		        "<HTML><A href=\"foo.html\" jquery12421421=\"bla\" myattr=\"true\">foo</A><HTML>";
		String test = "<HTML><A></A><HTML>";
		compareTwoDomsWithComparatorEqual(control, test, new PlainStructureComparator(true));
	}

	@Test
	public void testScriptComparator() {
		String control =
		        "<HTML><head><script>JavaScript();</script><title>Test</title></head><body><script>JavaScript23();</script>test</body><HTML>";
		String test = "<HTML><head><title>Test</title></head><body>test</body><HTML>";
		compareTwoDomsWithComparatorEqual(control, test, new ScriptComparator());
	}

	@Test
	public void testEditDistanceComparator() {
		String control = "<HTML><head><title>Test</title></head><body>test</body><HTML>";
		String test = "<HTML><head><title>Test</title></head><body>test</body><HTML>";
		assertTrue(control.equals(test));
		compareTwoDomsWithComparatorEqual(control, test, new EditDistanceComparator(0));
		compareTwoDomsWithComparatorEqual(control, test, new EditDistanceComparator(1));

		test = "TheIsAlotOfRubish";
		compareTwoDomsWithComparatorNotEqual(control, test, new EditDistanceComparator(1));
		compareTwoDomsWithComparatorEqual(control, test, new EditDistanceComparator(0));

		// We miss the title
		test = "<HTML><head></head><body>test</body><HTML>";
		Comparator oracle = new EditDistanceComparator(0.5);
		compareTwoDomsWithComparatorEqual(control, test, oracle);
		compareTwoDomsWithComparatorNotEqual(control, test, new EditDistanceComparator(1));
		compareTwoDomsWithComparatorEqual(control, test, new EditDistanceComparator(0));
	}

	@Test
	public void testXPathExpressionComparator() {
		String control = "<HTML><head><title>Test</title></head><body>test</body><HTML>";
		String test = "<HTML><head><title>Test</title></head><body>test</body><HTML>";
		assertTrue(control.equals(test));

		XPathExpressionComparator oracle = new XPathExpressionComparator();

		compareTwoDomsWithComparatorEqual(control, test, oracle);
		compareTwoDomsWithComparatorEqual(control, test, new XPathExpressionComparator());

		test =
		        "<HTML><head><title>Test</title></head><body>test<div id='ignoreme'>"
		                + "ignoreme</div></body><HTML>";
		compareTwoDomsWithComparatorNotEqual(control, test, oracle);
		compareTwoDomsWithComparatorNotEqual(control, test, new XPathExpressionComparator());

		oracle = new XPathExpressionComparator("//*[@id='ignoreme']");
		compareTwoDomsWithComparatorEqual(control, test, oracle);
		compareTwoDomsWithComparatorEqual(test, control, oracle);

		control =
		        "<HTML><head><title>Test</title></head><body>test<div id='ignoreme'>"
		                + "ignoreme123</div></body><HTML>";

		compareTwoDomsWithComparatorEqual(control, test, oracle);
		compareTwoDomsWithComparatorEqual(test, control, oracle);
	}
}

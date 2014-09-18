package com.crawljax.domcomparators;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RedundantWhiteSpaceStripperTest {

	@Test
	public void testMultipleSpacesToOne() {
		RedundantWhiteSpaceStripper stripper = new RedundantWhiteSpaceStripper();
		String dom = "<body>           <h1>This is           a test    for the      redundant white-space stripper</h1></body>";
		String output = stripper.apply(dom);
		assertEquals("<body> <h1>This is a test for the redundant white-space stripper</h1></body>", output);
	}
	
	@Test
	public void testMultipleReturnsToOneSpace() {
		RedundantWhiteSpaceStripper stripper = new RedundantWhiteSpaceStripper();
		String dom = "<test>\n\n\n</test>";
		String output = stripper.apply(dom);
		assertEquals("<test> </test>", output);
	}
	
	@Test
	public void testMultipleTabsToOneSpace() {
		RedundantWhiteSpaceStripper stripper = new RedundantWhiteSpaceStripper();
		String dom = "<test>\t\t\t\t\t</test>";
		String output = stripper.apply(dom);
		assertEquals("<test> </test>", output);
	}
	
	@Test
	public void testCombinationOfWhiteSpaceTypes() {
		RedundantWhiteSpaceStripper stripper = new RedundantWhiteSpaceStripper();
		String dom = "<test>\n\t<h1>\n\t\tHallo   \n</test>";
		String output = stripper.apply(dom);
		assertEquals("<test> <h1> Hallo </test>", output);
	}
}

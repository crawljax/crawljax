package com.crawljax.domcomparators;

import static org.junit.Assert.*;
import org.junit.Test;

public class HeadStripperTest {

	@Test
	public void testApply() {
		HeadStripper stripper = new HeadStripper();
		String body = "<body><h1>testApply</h1></body>";
		String input = "<html><head><title>testApply</title></head>" + body + "</html>";
		String output = stripper.apply(input);
		assertEquals("<html>" + body + "</html>", output);
	}
	
	@Test
	public void testApplyAttributesHead() {
		HeadStripper stripper = new HeadStripper();
		String body = "<body><h1>testApply</h1></body>";
		String input = "<html><head prefix=\"testing\"><title>testApply</title></head>" + body + "</html>";
		String output = stripper.apply(input);
		assertEquals("<html>" + body + "</html>", output);
	}

}

package com.crawljax.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.crawljax.util.PrettyHTML;

public class PrettyHTMLTest {

	@Test
	public void testRepeatString() {
		String testString = "Hello";
		String testStringToCompareWith;
		String repeatedString;
		
		int numRepeat = 0;
		testStringToCompareWith = "";
		repeatedString = repeatString(testString, numRepeat);
		assertEquals(repeatedString, testStringToCompareWith);
		
		numRepeat = 10;
		testStringToCompareWith = "";
		for(int i = 0; i < numRepeat; i++) {
			testStringToCompareWith += testString;
		}
		repeatedString = repeatString(testString, numRepeat);
		assertEquals(repeatedString, testStringToCompareWith);
	}
}

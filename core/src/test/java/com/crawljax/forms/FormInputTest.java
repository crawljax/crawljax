package com.crawljax.forms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
* Tests for class that has values of a Form Input.
* @author Hetherington
*
*/

public class FormInputTest {

	@Test
	public void testConstructor() {
		InputValue testVal2 = new InputValue("blabla2", false);
		assertEquals(testVal2.getValue(), "blabla2");
		assertEquals(testVal2.isChecked(), false);
	}	
	
	@Test
	public void testAccessorsAndMutators() {
		InputValue testVal = new InputValue();
		testVal.setChecked(true);
		assertEquals(testVal.isChecked(), true);
		testVal.setId(1234);
		assertEquals(testVal.getId(), 1234);
		testVal.setValue("blabla");
		assertEquals(testVal.getValue(), "blabla");
	}	
	
}
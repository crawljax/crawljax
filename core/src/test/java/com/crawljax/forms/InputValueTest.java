package com.crawljax.forms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
* Tests for class that has values of a Form Input.
* @author Hetherington
*
*/

public class InputValueTest {

	@Test
	public void testConstructor() {
		InputValue testInputVal = new InputValue("blabla2", false);
		
		assertEquals(testInputVal.getValue(), "blabla2");
		assertEquals(testInputVal.isChecked(), false);
	}	
	
	@Test
	public void testAccessorsAndMutators() {
		InputValue testInputVal = new InputValue();
		
		testInputVal.setChecked(true);
		assertEquals(testInputVal.isChecked(), true);
		testInputVal.setId(1234);
		assertEquals(testInputVal.getId(), 1234);
		testInputVal.setValue("blabla");
		assertEquals(testInputVal.getValue(), "blabla");
	}	
	
}
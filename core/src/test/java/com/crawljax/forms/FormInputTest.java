package com.crawljax.forms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FormInputTest {

	@Test
	public void testCreationAndAccess() {
		InputValue testVal = new InputValue();
		testVal.setChecked(true);
		assertEquals(testVal.isChecked(), true);
		testVal.setId(1234);
		assertEquals(testVal.getId(), 1234);
		testVal.setValue("blabla");
		assertEquals(testVal.getValue(), "blabla");
		
		InputValue testVal2 = new InputValue("blabla2", false);
		assertEquals(testVal2.getValue(), "blabla2");
		assertEquals(testVal2.isChecked(), false);
	}	
}
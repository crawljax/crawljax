package com.crawljax.forms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.crawljax.core.state.Identification;

/**
* Tests for class that has values of a Form Input.
* @author Hetherington
*
*/

public class FormInputTest {
	private long TEST_ID = 1234; 
	private String TEST_STRING_TYPE = "type";
	private String TEST_STRING_VALUE = "value";
	private String DEFAULT_TYPE_VALUE = "text";
	
	@Test
	public void testConstructor() {
		Identification testID = new Identification();
		testID.setId(TEST_ID);
		
		FormInput testFormInput = new FormInput(TEST_STRING_TYPE, testID, TEST_STRING_VALUE);
		
		assertEquals(testFormInput.getType(), TEST_STRING_TYPE);
		assertEquals(testFormInput.getIdentification().getId(), TEST_ID);
	}	
	
	@Test
	public void testAccessorsAndMutators() {
		Identification testID = new Identification();
		testID.setId(TEST_ID);
		
		FormInput testFormInput = new FormInput();
		
		assertEquals(testFormInput.getType(), DEFAULT_TYPE_VALUE);
		
		testFormInput.setId(TEST_ID);
		assertEquals(testFormInput.getId(), TEST_ID);
		
		testFormInput.setType(TEST_STRING_TYPE);
		assertEquals(testFormInput.getType(), TEST_STRING_TYPE);
		
		testFormInput.setIdentification(testID);
		assertEquals(testFormInput.getIdentification().getId(), TEST_ID);
		
		testFormInput.setMultiple(true);
		assertEquals(testFormInput.isMultiple(), true);
	}	
}
package com.crawljax.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class XMLObjectTest {

	private final static String filename = "xmlobject-save-to-file-test.xml";

	@SuppressWarnings("unchecked")
	@Test
	public void saveToFile() {
		ArrayList<String> object = new ArrayList<String>();
		object.add("Bla");
		object.add("Something else");

		try {
			XMLObject.objectToXML(object, filename);
		} catch (FileNotFoundException e) {
			fail("Error saving object");
		}
		File f = new File(filename);
		assertTrue(f.exists());

		object = null;
		try {
			object = (ArrayList<String>) XMLObject.xmlToObject(filename);
		} catch (FileNotFoundException e) {
			fail("File not found");
		}

		assertEquals(2, object.size());
		assertEquals("Bla", object.get(0));
		assertEquals("Something else", object.get(1));

		assertTrue(f.delete());
	}
}

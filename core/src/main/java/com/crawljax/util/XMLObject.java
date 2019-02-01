package com.crawljax.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * XMLObject helper.
 */
public final class XMLObject {

	private XMLObject() {

	}

	/**
	 * Converts an object to an XML file.
	 *
	 * @param object The object to convert.
	 * @param fileName  The filename where to save it to.
	 * @throws FileNotFoundException On error.
	 */
	public static void objectToXML(Object object, String fileName) throws FileNotFoundException {
		FileOutputStream fo = new FileOutputStream(fileName);
		XMLEncoder encoder = new XMLEncoder(fo);
		encoder.writeObject(object);
		encoder.close();
	}

	/**
	 * Converts an XML file to an object.
	 *
	 * @param fileName The filename where to save it to.
	 * @return The object.
	 * @throws FileNotFoundException On error.
	 */
	public static Object xmlToObject(String fileName) throws FileNotFoundException {
		FileInputStream fi = new FileInputStream(fileName);
		XMLDecoder decoder = new XMLDecoder(fi);
		Object object = decoder.readObject();
		decoder.close();
		return object;
	}

}

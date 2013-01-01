package com.crawljax.plugins.savecrawlsession;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.util.Helper;

/**
 * XMLObject helper.
 */
public final class XMLObject {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLObject.class);

	private XMLObject() {

	}

	/**
	 * Converts an object to an XML file.
	 * 
	 * @param object
	 *            The object to convert.
	 * @param fname
	 *            The filename where to save it to.
	 * @throws FileNotFoundException
	 *             On error.
	 */
	public static void objectToXML(Object object, String fname) throws FileNotFoundException {

		try {
			Helper.checkFolderForFile(fname);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		FileOutputStream fo = new FileOutputStream(new File(fname));
		XMLEncoder encoder = new XMLEncoder(fo);
		encoder.writeObject(object);
		encoder.close();
	}

	/**
	 * Converts an XML file to an object.
	 * 
	 * @param fname
	 *            The filename where to save it to.
	 * @throws FileNotFoundException
	 *             On error.
	 * @return The object.
	 */
	public static Object xmlToObject(String fname) throws FileNotFoundException {
		FileInputStream fi = new FileInputStream(fname);
		XMLDecoder decoder = new XMLDecoder(fi);
		Object object = decoder.readObject();
		decoder.close();
		return object;
	}

}

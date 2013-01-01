/**
 * 
 */
package com.crawljax.oraclecomparator.comparators;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.Helper;

/**
 * @author danny
 * @version $Id$
 */
public class ScriptComparator extends AbstractComparator {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractComparator.class
	        .getName());

	/**
	 * Default argument less constructor.
	 */
	public ScriptComparator() {
	}

	/**
	 * @param originalDom
	 *            The original DOM.
	 * @param newDom
	 *            The new DOM.
	 */
	public ScriptComparator(String originalDom, String newDom) {
		super(originalDom, newDom);
	}

	@Override
	public boolean isEquivalent() {
		try {
			Document orgDoc = Helper.getDocument(getOriginalDom());
			orgDoc = Helper.removeScriptTags(orgDoc);
			setOriginalDom(Helper.getDocumentToString(orgDoc));

			Document newDoc = Helper.getDocument(getNewDom());
			newDoc = Helper.removeScriptTags(newDoc);
			setNewDom(Helper.getDocumentToString(newDoc));
		} catch (SAXException e) {
			LOGGER.error("IOException with creating DOM document", e);
			return false;
		} catch (IOException e) {
			LOGGER.error("IOException with creating DOM document", e);
			return false;
		}
		return super.compare();
	}
}

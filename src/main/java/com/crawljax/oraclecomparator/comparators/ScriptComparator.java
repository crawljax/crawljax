/**
 * 
 */
package com.crawljax.oraclecomparator.comparators;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.Helper;

/**
 * @author danny
 * @version $Id$
 */
public class ScriptComparator extends AbstractComparator {

	private static final Logger LOGGER = Logger.getLogger(AbstractComparator.class.getName());

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

	/*
	 * (non-Javadoc)
	 * @see nl.tudelft.swerl.util.oracle.OracleAbstract#isEquivalent()
	 */
	@Override
	public boolean isEquivalent() {
		try {
			Document orgDoc = Helper.getDocument(getOriginalDom());
			orgDoc = Helper.removeScriptTags(orgDoc);
			setOriginalDom(Helper.getDocumentToString(orgDoc));

			Document newDoc = Helper.getDocument(getNewDom());
			newDoc = Helper.removeScriptTags(newDoc);
			setNewDom(Helper.getDocumentToString(newDoc));
		} catch (Exception e) {
			LOGGER.error("Error with creating DOM document", e);
		}
		return super.compare();
	}

}

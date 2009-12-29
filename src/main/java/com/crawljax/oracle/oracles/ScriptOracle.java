/**
 * 
 */
package com.crawljax.oracle.oracles;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.crawljax.oracle.AbstractOracle;
import com.crawljax.util.Helper;

/**
 * @author danny
 * @version $Id: ScriptOracle.java 6376 2009-12-29 10:51:05Z frank $
 */
public class ScriptOracle extends AbstractOracle {

	private static final Logger LOGGER = Logger.getLogger(AbstractOracle.class.getName());

	/**
	 * Default argument less constructor.
	 */
	public ScriptOracle() {
	}

	/**
	 * @param originalDom
	 *            The original DOM.
	 * @param newDom
	 *            The new DOM.
	 */
	public ScriptOracle(String originalDom, String newDom) {
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

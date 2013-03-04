/**
 * 
 */
package com.crawljax.oraclecomparator.comparators;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.crawljax.core.CrawljaxException;
import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.DomUtils;

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
			Document orgDoc = DomUtils.asDocument(getOriginalDom());
			orgDoc = DomUtils.removeScriptTags(orgDoc);
			setOriginalDom(DomUtils.getDocumentToString(orgDoc));

			Document newDoc = DomUtils.asDocument(getNewDom());
			newDoc = DomUtils.removeScriptTags(newDoc);
			setNewDom(DomUtils.getDocumentToString(newDoc));
		} catch (IOException | CrawljaxException e) {
			LOGGER.error("Exception with creating DOM document", e);
			return false;
		}
		return super.compare();
	}
}

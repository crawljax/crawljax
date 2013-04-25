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

	@Override
	public boolean isEquivalent(String oldDom, String newDom) {
		try {
			Document orgDoc = DomUtils.asDocument(oldDom);
			orgDoc = DomUtils.removeScriptTags(orgDoc);
			String normalizedOld = DomUtils.getDocumentToString(orgDoc);

			Document newDoc = DomUtils.asDocument(newDom);
			newDoc = DomUtils.removeScriptTags(newDoc);
			String normalizedNew = DomUtils.getDocumentToString(newDoc);
			return super.compare(normalizedOld, normalizedNew);
		} catch (IOException | CrawljaxException e) {
			LOGGER.error("Exception with creating DOM document", e);
			return false;
		}
	}
}

package com.crawljax.oraclecomparator.comparators;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.util.DomUtils;

public class ScriptComparator extends AbstractComparator {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractComparator.class
	        .getName());

	@Override
	public String normalize(String dom) {
		Document orgDoc;
		try {
			orgDoc = DomUtils.asDocument(dom);
			orgDoc = DomUtils.removeScriptTags(orgDoc);
			return DomUtils.getDocumentToString(orgDoc);
		} catch (IOException e) {
			LOGGER.warn("Could not perform DOM comparison", e);
			return dom;
		}

	}
}

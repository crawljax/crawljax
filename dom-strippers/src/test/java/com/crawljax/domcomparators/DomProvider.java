package com.crawljax.domcomparators;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.rules.ExternalResource;

public class DomProvider extends ExternalResource {

	private String originalDom;

	@Override
	protected void before() throws Throwable {
		originalDom = Resources.toString(
				DomProvider.class.getResource("/withEverythingDom.html"), Charsets.UTF_8);
	}

	public String newWithEverythingDom() {
		return originalDom;
	}

	public Document newWithEverythingDocument() {
		return Jsoup.parse(originalDom);
	}
}

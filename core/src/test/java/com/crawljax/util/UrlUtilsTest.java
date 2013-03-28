package com.crawljax.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class UrlUtilsTest {

	@Test
	public void getVarFromQueryString() {

		assertThat(UrlUtils.getVarFromQueryString("page", "?sub=1&userid=123&page=home&goto=0"), is("home"));
		assertThat(UrlUtils.getVarFromQueryString(null, "?sub=1&userid=123&page=home&goto=0"), is(nullValue()));
		assertThat(UrlUtils.getVarFromQueryString("page", ""), is(nullValue()));
		assertThat(UrlUtils.getVarFromQueryString("page", "?sub=1&userid=123&NotPage=home&goto=0"), is(nullValue()));
		assertThat(UrlUtils.getVarFromQueryString("page", "?sub=1&userid=123&page=home=moreStringInfo&goto=0"), is(nullValue()));
	}

	@Test
	public void isLinkExternal() {
		assertTrue(UrlUtils.isLinkExternal("http://crawljax.com", "http://google.com"));
		assertTrue(UrlUtils.isLinkExternal("http://crawljax.com", "file:///test/"));
		assertTrue(UrlUtils.isLinkExternal("http://site.crawljax.com",
		        "https://github.com/crawljax/crawljax"));

		assertFalse(UrlUtils.isLinkExternal("http://crawljax.com/download",
		        "http://crawljax.com/about"));
		// This is done intentional to capture miss formed urls as local so crawljax will process
		// them
		assertFalse("Missformed link is not external",
		        UrlUtils.isLinkExternal("http://crawljax.com", "http"));

		assertFalse("link and base are the same (http)",
		        UrlUtils.isLinkExternal("http://crawljax.com", "http://crawljax.com"));

		assertFalse("link and base are the same (https)",
		        UrlUtils.isLinkExternal("https://crawljax.com", "https://crawljax.com"));

		assertFalse("link and base are the same (file)",
		        UrlUtils.isLinkExternal("file:///tmp/index.html", "file:///tmp/index.html"));

		assertFalse("Sub dir is not external for file", UrlUtils.isLinkExternal(
		        "file:///tmp/index.html", "file:///tmp/subdir/index.html"));

		assertFalse("Sub dirs is not external for http", UrlUtils.isLinkExternal(
		        "http://crawljax.com", "http://crawljax.com/sub/dir/about.html"));

		assertFalse("Https link from http base is not external",
		        UrlUtils.isLinkExternal("http://crawljax.com", "https://crawljax.com/about.html"));
		assertFalse("Https link from https base is not external", UrlUtils.isLinkExternal(
		        "https://crawljax.com", "https://crawljax.com/about.html"));
		assertFalse("Http link from https base is not external",
		        UrlUtils.isLinkExternal("https://crawljax.com", "http://crawljax.com/about.html"));

		assertFalse("relative link from https base is not external",
		        UrlUtils.isLinkExternal("https://crawljax.com", "about.html"));
		assertFalse("relative link from http base is not external",
		        UrlUtils.isLinkExternal("http://crawljax.com", "about.html"));

		assertFalse("root link from http base is not external",
		        UrlUtils.isLinkExternal("http://crawljax.com", "/about.html"));
		assertFalse("root link from https base is not external",
		        UrlUtils.isLinkExternal("https://crawljax.com", "/about.html"));

		assertFalse("relative link from file base is not external",
		        UrlUtils.isLinkExternal("file:///tmp/index.html", "about.html"));

		assertTrue("root link from file base is external",
		        UrlUtils.isLinkExternal("file://tmp/index.html", "/about.html"));

		assertTrue("Ignore email links",
		        UrlUtils.isLinkExternal("https://example.com", "mailto:test@example.com"));

	}

	@Test
	public void getBaseUrl() {
		assertEquals("http://crawljax.com", UrlUtils.getBaseUrl("http://crawljax.com/about/"));

	}

	@Test(expected = MalformedURLException.class)
	public void whenUrlIsJavaScriptItDoesNotExtract() throws MalformedURLException {
		UrlUtils.extractNewUrl("http://example.com", "javascript:void(0)");
	}

	@Test(expected = MalformedURLException.class)
	public void whenUrlisMailItDoesNotExtract() throws MalformedURLException {
		UrlUtils.extractNewUrl("http://example.com", "mailto:test@example.com");
	}

	@Test
	public void testExtractNewUrl() throws MalformedURLException {
		String base = "http://example.com";
		URL baseWithA = new URL(base + "/a");
		assertThat(UrlUtils.extractNewUrl(base, "a"), is(baseWithA));

		assertThat(UrlUtils.extractNewUrl(base + "/example", "/a"), is(baseWithA));

		assertThat(UrlUtils.extractNewUrl(base + "/example/b", "/a"), is(baseWithA));

		assertThat(UrlUtils.extractNewUrl(base + "/example/b", "a"), is(new URL(base
		        + "/example/a")));

		assertThat(UrlUtils.extractNewUrl(base + "/example/b", "../a"), is(baseWithA));

		assertThat(UrlUtils.extractNewUrl("http://example.com", "http://test.example.com"),
		        is(new URL("http://test.example.com")));
	}

}

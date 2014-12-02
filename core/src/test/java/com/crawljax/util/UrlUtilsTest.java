package com.crawljax.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URI;

import org.junit.Test;

public class UrlUtilsTest {

	@Test
	public void getVarFromQueryString() {

		assertThat(UrlUtils.getVarFromQueryString("page", "?sub=1&userid=123&page=home&goto=0"),
		        is("home"));
		assertThat(UrlUtils.getVarFromQueryString(null, "?sub=1&userid=123&page=home&goto=0"),
		        is(nullValue()));
		assertThat(UrlUtils.getVarFromQueryString("page", ""), is(nullValue()));
		assertThat(
		        UrlUtils.getVarFromQueryString("page", "?sub=1&userid=123&NotPage=home&goto=0"),
		        is(nullValue()));
		assertThat(UrlUtils.getVarFromQueryString("page",
		        "?sub=1&userid=123&page=home=moreStringInfo&goto=0"), is(nullValue()));
	}

	@Test
	public void getBaseUrl() {
		assertEquals("http://crawljax.com", UrlUtils.getBaseUrl("http://crawljax.com/about/"));

		assertEquals("https://crawljax.com", UrlUtils.getBaseUrl("https://crawljax.com/about/"));

		assertEquals("http://crawljax.com",
		        UrlUtils.getBaseUrl("http://crawljax.com/about/history/"));

		assertEquals("http://crawljax.com", UrlUtils.getBaseUrl("http://crawljax.com/"));

		assertEquals("http://crawljax.com", UrlUtils.getBaseUrl("http://crawljax.com"));

		assertEquals("http://crawls.crawljax.com",
		        UrlUtils.getBaseUrl("http://crawls.crawljax.com/demo"));

		assertEquals("http://crawls.crawljax.com",
		        UrlUtils.getBaseUrl("http://crawls.crawljax.com"));

	}

	@Test(expected = IllegalArgumentException.class)
	public void whenUrlIsJavaScriptItDoesNotExtract() throws MalformedURLException {
		UrlUtils.extractNewUrl("http://example.com", "javascript:void(0)");
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenUrlisMailItDoesNotExtract() throws MalformedURLException {
		UrlUtils.extractNewUrl("http://example.com", "mailto:test@example.com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenUrlAboutBlankDoesNotExtract() throws MalformedURLException {
		UrlUtils.extractNewUrl("http://example.com", "about:blank");
	}

	@Test
	public void testExtractNewUrl() throws MalformedURLException {
		final String base = "http://example.com";
		URI baseWithA = URI.create(base + "/a");

		assertThat(UrlUtils.extractNewUrl(base, "a"), is(baseWithA));

		assertThat(UrlUtils.extractNewUrl(base + "/example", "/a"), is(baseWithA));

		assertThat(UrlUtils.extractNewUrl(base + "/example/b", "/a"), is(baseWithA));

		assertThat(UrlUtils.extractNewUrl(base + "/example/b", "a"),
		        is(URI.create(base + "/example/a")));

		assertThat(UrlUtils.extractNewUrl(base + "/example/b", "../a"), is(baseWithA));

		assertThat(UrlUtils.extractNewUrl(base, "http://test.example.com"),
		        is(URI.create("http://test.example.com")));

		assertThat(UrlUtils.extractNewUrl(base, "#someHash"), is(URI.create(base + "/#someHash")));

	}

	@Test
	public void testIsSameDomain() throws MalformedURLException {

		// Same URL
		assertThat(UrlUtils.isSameDomain("http://example.com", URI.create("http://example.com")),
		        is(true));

		// Different URL
		assertThat(UrlUtils.isSameDomain("http://test.com", URI.create("http://example.com")),
		        is(false));

		// Same URL with subdomain
		assertThat(UrlUtils.isSameDomain("http://test.example.com",
		        URI.create("http://example.com")), is(true));

		// Same URL but with HTTPS
		assertThat(
		        UrlUtils.isSameDomain("https://example.com", URI.create("http://example.com")),
		        is(true));

		// Same URL but with different fragment after #
		assertThat(UrlUtils.isSameDomain(
		        "https://example.com/#something=blah|somethingelse=blah1",
		        URI.create("http://example.com")), is(true));
	}

}

package com.crawljax.util;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

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

package com.crawljax.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlUtils {

	private static final Logger LOG = LoggerFactory.getLogger(UrlUtils.class);

	/**
	 * @param currentUrl
	 *            The current url
	 * @param href
	 *            The target URL, relative or not
	 * @return The new URL.
	 */
	public static URL extractNewUrl(String currentUrl, String href) throws MalformedURLException {
		if (href == null || isJavascript(href) || href.startsWith("mailto:")) {
			throw new MalformedURLException(href + " is not a HTTP url");
		} else if (href.contains("://")) {
			return new URL(href);
		} else {
			return new URL(new URL(currentUrl), href);
		}
	}

	private static boolean isJavascript(String href) {
		return href.startsWith("javascript:");
	}

	/**
	 * Internal used function to strip the basePath from a given url.
	 * 
	 * @param url
	 *            the url to examine
	 * @return the base path with file stipped
	 */
	static String getBasePath(URL url) {
		String file = url.getFile().replaceAll("\\*", "");

		try {
			return url.getPath().replaceAll(file, "");
		} catch (PatternSyntaxException pe) {
			LOG.error(pe.getMessage());
			return "";
		}

	}

	/**
	 * @param url
	 *            the URL string.
	 * @return the base part of the URL.
	 */
	public static String getBaseUrl(String url) {
		String head = url.substring(0, url.indexOf(':'));
		String subLoc = url.substring(head.length() + DomUtils.BASE_LENGTH);
		return head + "://" + subLoc.substring(0, subLoc.indexOf('/'));
	}

	/**
	 * Retrieve the var value for varName from a HTTP query string (format is
	 * "var1=val1&var2=val2").
	 * 
	 * @param varName
	 *            the name.
	 * @param haystack
	 *            the haystack.
	 * @return variable value for varName
	 */
	public static String getVarFromQueryString(String varName, String haystack) {
		if (haystack == null || haystack.length() == 0) {
			return null;
		}

		String modifiedHaystack = haystack;

		if (modifiedHaystack.charAt(0) == '?') {
			modifiedHaystack = modifiedHaystack.substring(1);
		}
		String[] vars = modifiedHaystack.split("&");

		for (String var : vars) {
			String[] tuple = var.split("=");
			if (tuple.length == 2 && tuple[0].equals(varName)) {
				return tuple[1];
			}
		}
		return null;
	}

	/**
	 * Checks if the given URL is part of the domain, or a subdomain of the given {@link URL}.
	 * 
	 * @param currentUrl
	 *            The url you want to check.
	 * @param url
	 *            The URL acting as the base.
	 * @return If the URL is part of the domain.
	 */
	public static boolean isSameDomain(String currentUrl, URL url) {
		try {
			String current = URI.create(currentUrl).getHost().toLowerCase();
			String original = url.toURI().getHost().toLowerCase();
			return current.endsWith(original);
		} catch (URISyntaxException e) {
			LOG.warn("Could not parse URI {}", currentUrl);
			return false;
		}
	}

	private UrlUtils() {

	}

}

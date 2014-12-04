package com.crawljax.util;

import java.net.URI;

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
	public static URI extractNewUrl(String currentUrl, String href) {
		if (href == null || isJavascript(href) || href.startsWith("mailto:")
		        || href.equals("about:blank")) {
			throw new IllegalArgumentException(String.format("%s is not a HTTP url", href));
		} else if (href.contains("://")) {
			return URI.create(href);
		} else {
			URI current = URI.create(currentUrl);
			if (current.getPath().isEmpty() && !href.startsWith("/")) {
				return URI.create(currentUrl).resolve("/" + href);
			}
			return URI.create(currentUrl).resolve(href);
		}
	}

	private static boolean isJavascript(String href) {
		return href.startsWith("javascript:");
	}

	/**
	 * @param url
	 *            the URL string. It must contain with ":" e.g, http: or https:
	 * @return the base part of the URL.
	 */
	public static String getBaseUrl(String url) {
		String head = url.substring(0, url.indexOf(':'));
		String subLoc = url.substring(head.length() + DomUtils.BASE_LENGTH);
		int index = subLoc.indexOf('/');
		String base;
		if (index == -1) {
			base = url;
		} else {
			base = head + "://" + subLoc.substring(0, index);
		}

		return base;
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
	 * Checks if the given URL is part of the domain, or a subdomain of the given
	 * {@link java.net.URI}.
	 *
	 * @param currentUrl
	 *            The url you want to check.
	 * @param url
	 *            The URL acting as the base.
	 * @return If the URL is part of the domain.
	 */
	public static boolean isSameDomain(String currentUrl, URI url) {
		String current = URI.create(getBaseUrl(currentUrl)).getHost().toLowerCase();
		String original = url.getHost().toLowerCase();
		return current.endsWith(original);

	}

	private UrlUtils() {

	}

}

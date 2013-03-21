package com.crawljax.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlUtils {

	private static final Logger LOG = LoggerFactory.getLogger(UrlUtils.class);

	/**
	 * Check if a given URL is outside the current domain.
	 * <p>
	 * It is insensitive to subdomains and different protocols. If a the browsers is on the file
	 * system browsing to <code>/</code> returns <code>true</code>.
	 * </p>
	 * ?
	 * 
	 * @param location
	 *            Current location.
	 * @param link
	 *            the destinations. This can be a relative or absolute link.
	 * @return Whether location and link are on the same domain. It returns <code>false</code> if
	 *         the link is <code>null</code>. It returns true if the destination {@link URL} throws
	 *         a {@link MalformedURLException}, is a <code>mailto:</code> link, is a absolute file
	 *         URL or when it's unreadable.
	 */
	public static boolean isLinkExternal(String location, String link) {
		if (link == null) {
			return false;
		}
		URL source;
		try {
			source = new URL(location);
		} catch (MalformedURLException e) {
			LOG.warn("Could not parse source URL {}", location);
			return true;
		}
		try {
			URL destination;
			if (isJavascript(link)) {
				return false;
			} else if (link.contains("://")) {
				destination = new URL(link);
			} else if (source.getProtocol().equals("file") && link.startsWith("/")) {
				return true;
			} else if (link.startsWith("mailto:")) {
				return true;
			} else {
				destination = new URL(source, link);
			}
			return !source.getHost().equals(destination.getHost());
		} catch (MalformedURLException e) {
			LOG.warn("Could not parse target URL {}", link);
			return true;
		}
	}

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
		String head = url.substring(0, url.indexOf(":"));
		String subLoc = url.substring(head.length() + DomUtils.BASE_LENGTH);
		return head + "://" + subLoc.substring(0, subLoc.indexOf("/"));
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
		if (haystack.charAt(0) == '?') {
			haystack = haystack.substring(1);
		}
		String[] vars = haystack.split("&");

		for (String var : vars) {
			String[] tuple = var.split("=");
			if (tuple.length == 2 && tuple[0].equals(varName)) {
				return tuple[1];
			}
		}
		return null;
	}

	private UrlUtils() {

	}

}

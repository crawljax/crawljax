package com.crawljax.plugins.crawloverview;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Parses a Java bean to a map containing the getter name as the key and the field value as the
 * value. All instances of {@link Collection} are converted to html lists.
 */
class BeanToReadableMap {

	public static interface Filter {

		Entry<String, String> filter(String key, String value);

	}

	private static final String CAMEL_REGEX = String.format("%s|%s|%s",
	        "(?<=[A-Z])(?=[A-Z][a-z])",
	        "(?<=[^A-Z])(?=[A-Z])",
	        "(?<=[A-Za-z])(?=[^A-Za-z])");

	private static final Filter[] EMPTY_FILTERS = new Filter[] {};

	public static ImmutableMap<String, String> toMap(Object o) {
		return toMap(o, EMPTY_FILTERS);
	}

	public static ImmutableMap<String, String> toMap(Object o, Filter... filters) {
		Builder<String, String> builder = ImmutableMap.builder();
		for (Method method : o.getClass().getMethods()) {
			if (isGetter(method)) {
				builder.put(addmethodToMap(o, method, filters));
			}
		}
		return builder.build();
	}

	private static Entry<String, String> addmethodToMap(Object o, Method method, Filter[] filters) {
		try {
			Object[] noArgs = null;
			Object result = method.invoke(o, noArgs);
			return ImmutablePair.of(asName(method), toString(result));

		} catch (Exception e) {
			throw new CrawlOverviewException("Could not parse bean " + o.toString()
			        + " because " + e.getMessage(), e);
		}
	}

	private static String toString(Object result) {
		if (result instanceof Collection<?>) {
			return asHtmlList((Collection<?>) result);
		} else {
			return escapeHtml(result.toString());
		}
	}

	private static String asName(Method method) {
		String name = method.getName().substring(3);
		return splitCamelCase(name);
	}

	private static String asHtmlList(Collection<?> result) {
		if (result.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder("<ul>");
		for (Object object : result) {
			sb.append("<li>").append(escapeHtml(object.toString())).append("</li>");
		}
		return sb.append("</ul>").toString();
	}

	private static boolean isGetter(Method method) {
		return method.getName().startsWith("get")
		        && method.getParameterTypes().length == 0
		        && !"getClass".equals(method.getName());
	}

	private static String splitCamelCase(String s) {
		return s.replaceAll(CAMEL_REGEX, " ");
	}

	private BeanToReadableMap() {

	}
}

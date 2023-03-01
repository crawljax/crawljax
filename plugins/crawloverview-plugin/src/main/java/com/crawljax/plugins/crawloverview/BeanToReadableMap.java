package com.crawljax.plugins.crawloverview;

import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.plugin.Plugins;
import com.crawljax.plugins.crawloverview.model.Serializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map.Entry;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a Java bean to a map containing the getter name as the key and the field value as the
 * value. All instances of {@link Collection} are converted to html lists.
 * <p>
 * The getter name is parsed from Camel case to a readable format. Both keys and values are HTML
 * escaped for safety.
 * </p>
 */
class BeanToReadableMap {

    private static final String CAMEL_REGEX =
            String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])");

    private static final Logger LOG = LoggerFactory.getLogger(BeanToReadableMap.class);

    private BeanToReadableMap() {}

    public static ImmutableMap<String, String> toMap(Object o) {
        Builder<String, String> builder = ImmutableMap.builder();
        for (Method method : o.getClass().getMethods()) {
            if (isGetter(method)) {
                builder.put(addMethodToMap(o, method));
            }
        }
        return builder.build();
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get")
                && method.getParameterTypes().length == 0
                && !"getClass".equals(method.getName());
    }

    private static Entry<String, String> addMethodToMap(Object o, Method method) {
        try {
            Object[] noArgs = null;
            Object result = method.invoke(o, noArgs);
            String name = asName(method);
            return ImmutablePair.of(name, toString(name, result));

        } catch (Exception e) {
            LOG.error("Could not parse bean {} because {}", o, e.getMessage(), e);
            return ImmutablePair.of(asName(method), "Unreadable entry");
        }
    }

    private static String toString(String name, Object result) {
        if (result == null) {
            return "null";
        } else if (result instanceof Collection<?>) {
            return asHtmlList((Collection<?>) result);
        } else if (result instanceof Plugin) {
            return StringEscapeUtils.escapeHtml4(result.getClass().getSimpleName());
        } else if (result instanceof CrawlRules) {
            return "<pre><code>" + StringEscapeUtils.escapeHtml4(Serializer.toPrettyJson(result)) + "</code></pre>";
        } else if (result instanceof BrowserConfiguration) {
            BrowserConfiguration config = (BrowserConfiguration) result;
            StringBuilder configAsString = new StringBuilder()
                    .append(config.getNumberOfBrowsers())
                    .append(" browsers of type ")
                    .append(config.getBrowserType());
            if (!(config.getBrowserBuilder() instanceof WebDriverBrowserBuilder)) {
                configAsString.append(" using builder ").append(config.getBrowserBuilder());
            }
            return configAsString.toString();
        } else if (result instanceof Plugins) {
            return toString(name, ((Plugins) result).pluginNames());
        } else if (name != null && name.toLowerCase().contains("runtime")) {
            return StringEscapeUtils.escapeHtml4(DurationFormatUtils.formatDurationWords((Long) result, true, true));
        } else if (result instanceof Number && ((Number) result).intValue() == 0) {
            return "&infin;";
        } else {
            return StringEscapeUtils.escapeHtml4(result.toString());
        }
    }

    private static String asName(Method method) {
        final int getPrefix = "get".length();
        String name = method.getName().substring(getPrefix);
        return splitCamelCase(name);
    }

    private static String asHtmlList(Collection<?> result) {
        if (result.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("<ul>");
        for (Object object : result) {
            sb.append("<li>").append(toString(null, object)).append("</li>");
        }
        return sb.append("</ul>").toString();
    }

    private static String splitCamelCase(String s) {
        return s.replaceAll(CAMEL_REGEX, " ");
    }
}

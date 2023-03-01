package com.crawljax.plugins.crawloverview;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;

import com.crawljax.condition.RegexCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.oraclecomparator.OracleComparator;
import com.crawljax.oraclecomparator.comparators.SimpleComparator;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BeanToReadableMapTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void test() {
        Map<String, String> map = BeanToReadableMap.toMap(new TestBean());
        assertThat(map.size(), is(4));
        assertThat(map, hasEntry("Some String", "A"));
        assertThat(map, hasEntry("Some Int", "123"));
        assertThat(map, hasEntry("String List", "<ul><li>A</li><li>B</li></ul>"));
        assertThat(map, hasEntry("Object List", "<ul><li>42</li></ul>"));
    }

    @Test
    public void testConfigMap() {
        CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor("http://example.com")
                .addPlugin(new CrawlOverview())
                .setOutputDirectory(tmpFolder.getRoot());

        builder.setMaximumRunTime(1, TimeUnit.MINUTES);
        builder.crawlRules().addCrawlCondition(new CrawlCondition("kers", new RegexCondition("test")));

        builder.crawlRules().addOracleComparator(new OracleComparator("tes", new SimpleComparator()));

        CrawljaxConfiguration config = builder.build();
        ImmutableMap<String, String> map = BeanToReadableMap.toMap(config);
        Assert.assertTrue(
                map.containsKey("Maximum Runtime") && map.get("Maximum Runtime").equals("1 minute"));
    }
}

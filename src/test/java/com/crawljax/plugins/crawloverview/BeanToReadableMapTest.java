package com.crawljax.plugins.crawloverview;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

public class BeanToReadableMapTest {

	@Test
	public void test() {
		Map<String, String> map = BeanToReadableMap.toMap(new TestBean());
		assertThat(map.size(), is(4));
		assertThat(map, hasEntry("Some String", "A"));
		assertThat(map, hasEntry("Some Int", "123"));
		assertThat(map, hasEntry("String List", "<ul><li>A</li><li>B</li></ul>"));
		assertThat(map, hasEntry("Object List", "<ul><li>42</li></ul>"));
	}
}

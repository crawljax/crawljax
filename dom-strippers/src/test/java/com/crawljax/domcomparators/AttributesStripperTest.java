package com.crawljax.domcomparators;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

public class AttributesStripperTest {
	@Rule
	public final DomProvider DOMS = new DomProvider();

	@Test
	public void testAllContentStripper() {
		String original = DOMS.newWithEverythingDom();
		assertThat(original, containsString("style=\"color: red\""));
		String stripped = new AttributesStripper().apply(original);
		assertThat(stripped, not(containsString("style=\"color: red\"")));
	}
}

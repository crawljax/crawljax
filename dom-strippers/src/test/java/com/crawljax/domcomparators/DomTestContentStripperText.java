package com.crawljax.domcomparators;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

public class DomTestContentStripperText {

	@Rule
	public final DomProvider DOMS = new DomProvider();

	@Test
	public void testAllContentStripper() {
		String original = DOMS.newWithEverythingDom();
		assertThat(original, containsString("This is section one"));
		String stripped = new DomTextContentStripper().apply(original);
		assertThat(stripped, not(containsString("This is section one")));
	}
}

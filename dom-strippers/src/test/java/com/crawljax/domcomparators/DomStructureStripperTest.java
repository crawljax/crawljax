package com.crawljax.domcomparators;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

public class DomStructureStripperTest {

	@Rule
	public final DomProvider DOMS = new DomProvider();

	@Test
	public void testAllContentStripper() {
		String original = DOMS.newWithEverythingDom();
		assertThat(original, containsString("<div>"));
		String stripped = new DomStructureStripper().apply(original);
		assertThat(stripped, not(containsString("<div>")));
		assertThat(new WhiteSpaceStripper().apply(stripped), startsWith("ThefullblowntestDOMThisissecti"));
	}
}

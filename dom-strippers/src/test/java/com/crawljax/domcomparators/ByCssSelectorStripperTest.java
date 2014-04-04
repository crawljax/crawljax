package com.crawljax.domcomparators;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.jsoup.nodes.Document;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ByCssSelectorStripperTest {

	@Parameterized.Parameters(name = "{0} = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{"elementName", "h1"},
				{"class name",".pageHeader"},
				{"id", "#section1"},
				{"childCombiner", "header > h1"},
				{"attribute", "*[style=color: green]"}
		});
	}


	@Rule
	public final DomProvider DOMS = new DomProvider();

	private final String selector;

	public ByCssSelectorStripperTest(String selectorName, String selector) {
		this.selector = selector;
	}

	@Test
	public void testRemovalOf() {
		Document original = DOMS.newWithEverythingDocument();
		assertThat(original.select(selector), not(empty()));
		Document stripped = new ByCssSelectorStripper(selector).apply(original);
		assertThat(stripped.select(selector), is(empty()));
	}

}

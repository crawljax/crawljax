// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.util;

import com.google.common.collect.Lists;
import org.custommonkey.xmlunit.Difference;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Test the usage of the Helper.getDifferences.
 *
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class XmlUnitDifferenceTest {

	@Test
	public void testEmptyDOMs() {
		String left = "";
		String right = "";
		List<Difference> l = DomUtils.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameIdenticalDOMs() {
		String left = "<abc></abc>";
		String right = "<abc></abc>";
		List<Difference> l = DomUtils.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameDOMsAttributesSame() {
		String left = "<abc><def value='bla'/></abc>";
		String right = "<abc><def value='bla'/></abc>";
		List<Difference> l = DomUtils.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameDOMsAttributesFiltered() {
		String left = "<abc><def value='bla123'/></abc>";
		String right = "<abc><def value='bla'/></abc>";
		List<Difference> l = DomUtils.getDifferences(left, right, Lists.newArrayList("value"));
		Assert.assertEquals(0, l.size());
	}

}

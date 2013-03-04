// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.util;

import java.util.List;

import org.custommonkey.xmlunit.Difference;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Test the useage of the Helper.getDifferences.
 * 
 * @author slenselink@google.com (Stefan Lenselink)
 */
public class XmlunitDifferenceTest {

	@Test
	public void testEmptyDoms() {
		String left = "";
		String right = "";
		List<Difference> l = DomUtils.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameIdenticalDoms() {
		String left = "<abc></abc>";
		String right = "<abc></abc>";
		List<Difference> l = DomUtils.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameDomsArrtibutesSame() {
		String left = "<abc><def value='bla'/></abc>";
		String right = "<abc><def value='bla'/></abc>";
		List<Difference> l = DomUtils.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameDomsArrtibutesFiltered() {
		String left = "<abc><def value='bla123'/></abc>";
		String right = "<abc><def value='bla'/></abc>";
		List<Difference> l = DomUtils.getDifferences(left, right, Lists.newArrayList("value"));
		Assert.assertEquals(0, l.size());
	}

}

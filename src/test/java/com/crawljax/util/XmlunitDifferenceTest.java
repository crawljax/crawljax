// Copyright 2010 Google Inc. All Rights Reserved.

package com.crawljax.util;

import com.google.common.collect.Lists;

import junit.framework.Assert;

import org.custommonkey.xmlunit.Difference;
import org.junit.Test;

import java.util.List;

/**
 * Test the useage of the Helper.getDifferences.
 *
 * @author slenselink@google.com (Stefan Lenselink)
 * @version $Id$
 */
public class XmlunitDifferenceTest {

	@Test
	public void testEmptyDoms() {
		String left = "";
		String right = "";
		List<Difference> l = Helper.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameIdenticalDoms() {
		String left = "<abc></abc>";
		String right = "<abc></abc>";
		List<Difference> l = Helper.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameDomsArrtibutesSame() {
		String left = "<abc><def value='bla'/></abc>";
		String right = "<abc><def value='bla'/></abc>";
		List<Difference> l = Helper.getDifferences(left, right);
		Assert.assertEquals(0, l.size());
	}

	@Test
	public void testSameDomsArrtibutesFiltered() {
		String left = "<abc><def value='bla123'/></abc>";
		String right = "<abc><def value='bla'/></abc>";
		List<Difference> l = Helper.getDifferences(left, right, Lists.newArrayList("value"));
		Assert.assertEquals(0, l.size());
	}

}

package com.crawljax.oraclecomparator.comparators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the edit distance algorithm.
 * 
 * @author mesbah
 */
public class EditDistanceTest {

	private static final Logger LOG = LoggerFactory.getLogger(EditDistanceTest.class);

	private EditDistanceComparator comparator;

	@Before
	public void setup() {
		comparator = new EditDistanceComparator();
	}

	/**
	 * Check if threshold calculation works.
	 */
	@Test
	public void testGetThreshold() {
		String x = "<form>bl</form>";
		String y = "<form>blabla</form>";
		double p = 0.8;

		double expected = (2 * Math.max(x.length(), y.length()) * (1 - p));

		assertEquals(expected, comparator.getThreshold(x, y, p), .01d);
	}

	/**
	 * Check if clone detection algorithm works correctly.
	 */
	@Test
	public void testIsClone() {
		String x = "<form>BL</form>";
		String y = "<form>blabla</form>";

		LOG.debug(StringUtils.getLevenshteinDistance(x, y) + " Thesh: "
		        + comparator.getThreshold(x, y, 0.7));
		assertTrue(comparator.isClone(x, y, 0.0));
		assertTrue(comparator.isClone(x, y, 0.5));
		assertTrue(comparator.isClone(x, y, 0.7));
		assertTrue(comparator.isClone(x, y, 0.75));
		assertTrue(comparator.isClone(x, y, 0.84));
		assertFalse(comparator.isClone(x, y, 0.89));
		assertFalse(comparator.isClone(x, y, 0.893));
		assertFalse(comparator.isClone(x, y, 0.9));
		assertFalse(comparator.isClone(x, y, 1));

		boolean arg = false;

		try {
			comparator.isClone(x, y, -2);
		} catch (IllegalArgumentException e) {
			arg = true;
		}

		assertTrue(arg);

		arg = false;

		try {
			comparator.isClone(x, y, 2);
		} catch (IllegalArgumentException e) {
			arg = true;
		}

		assertTrue(arg);
	}
}

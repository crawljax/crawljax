/**
 * Created Mar 10, 2008
 */
package com.crawljax.util;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the edit distance algorithm.
 * 
 * @author mesbah
 * @version $Id$
 */
public class EditDistanceTest extends TestCase {

	private static final Logger LOG = LoggerFactory.getLogger(EditDistanceTest.class);

	/**
	 * Check if threshold calculation works.
	 */
	public void testGetThreshold() {
		String x = "<form>bl</form>";
		String y = "<form>blabla</form>";
		double p = 0.8;

		double expected = (2 * Math.max(x.length(), y.length()) * (1 - p));

		assertEquals(expected, EditDistance.getThreshold(x, y, p));
	}

	/**
	 * Check if clone detection algorithm works correctly.
	 */
	public void testIsClone() {
		String x = "<form>BL</form>";
		String y = "<form>blabla</form>";

		LOG.debug(StringUtils.getLevenshteinDistance(x, y) + " Thesh: "
		        + EditDistance.getThreshold(x, y, 0.7));
		assertTrue(EditDistance.isClone(x, y, 0.0));
		assertTrue(EditDistance.isClone(x, y, 0.5));
		assertTrue(EditDistance.isClone(x, y, 0.7));
		assertTrue(EditDistance.isClone(x, y, 0.75));
		assertTrue(EditDistance.isClone(x, y, 0.84));
		assertFalse(EditDistance.isClone(x, y, 0.89));
		assertFalse(EditDistance.isClone(x, y, 0.893));
		assertFalse(EditDistance.isClone(x, y, 0.9));
		assertFalse(EditDistance.isClone(x, y, 1));

		boolean arg = false;

		try {
			EditDistance.isClone(x, y, -2);
		} catch (IllegalArgumentException e) {
			arg = true;
		}

		assertTrue(arg);

		arg = false;

		try {
			EditDistance.isClone(x, y, 2);
		} catch (IllegalArgumentException e) {
			arg = true;
		}

		assertTrue(arg);
	}
}

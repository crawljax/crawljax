package com.crawljax.forms;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class RandomInputValueGeneratorTest {
	private static final int NUM_RAND_CHECKS = 1000;
	private static final int LENGTH_SHORT = 1;
	private static final int LENGTH_MEDIUM = 15;
	private static final int LENGTH_LONG = 150;

	private RandomInputValueGenerator generator;

	@Before
	public void setup() {
		generator = new RandomInputValueGenerator();
	}

	@Test
	public void randomValuesAreUnique() {
		Set<String> set = new HashSet<>();
		for (int i = 0; i < NUM_RAND_CHECKS; i++) {
			assertThat(set.add(generator.getRandomString(LENGTH_MEDIUM)), is(true));
		}
	}

	@Test
	public void testLengthSpecification() {
		assertThat(generator.getRandomString(LENGTH_SHORT).length(), is(LENGTH_SHORT));
		assertThat(generator.getRandomString(LENGTH_MEDIUM).length(), is(LENGTH_MEDIUM));
		assertThat(generator.getRandomString(LENGTH_LONG).length(), is(LENGTH_LONG));
	}

	@Test(expected = IllegalArgumentException.class)
	public void randomOptionDoesntAcceptEmptyLists() {
		generator.getRandomItem(Lists.newArrayList());
	}
}

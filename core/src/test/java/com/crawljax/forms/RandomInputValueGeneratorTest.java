package com.crawljax.forms;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class RandomInputValueGeneratorTest {

	private RandomInputValueGenerator generator;

	@Before
	public void setup() {
		generator = new RandomInputValueGenerator();
	}

	@Test
	public void randomValuesAreUnique() {
		Set<String> set = new HashSet<>();
		for (int i = 0; i < 1000; i++) {
			assertThat(set.add(generator.getRandomString(15)), is(true));
		}
	}

	@Test
	public void testLengthSpecification() {
		assertThat(generator.getRandomString(1).length(), is(1));
		assertThat(generator.getRandomString(15).length(), is(15));
		assertThat(generator.getRandomString(150).length(), is(150));
	}

	@Test(expected = IllegalArgumentException.class)
	public void randomOptionDoesntAcceptEmptyLists() {
		generator.getRandomItem(Lists.newArrayList());
	}
}

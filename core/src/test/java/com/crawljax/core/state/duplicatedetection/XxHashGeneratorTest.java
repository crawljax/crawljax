package com.crawljax.core.state.duplicatedetection;

import static org.junit.Assert.*;
import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.XxHashGenerator;

public class XxHashGeneratorTest {

	@Test
	public void testGenerateHash() {
		XXHash32 xxhash = XXHashFactory.fastestInstance().hash32();
		String input = "When Chuck Norris throws exceptions, it’s across the room.";
		XxHashGenerator hg = new XxHashGenerator();
		int tested = hg.generateHash(input);
		int real = xxhash.hash(input.getBytes(), 0, input.length(), 0x9747b28c);
		assertEquals(tested, real);
	}

	@Test
	public void testGenerateHashEmpty() {
		XXHash32 xxhash = XXHashFactory.fastestInstance().hash32();
		String input = "";
		XxHashGenerator hg = new XxHashGenerator();
		int tested = hg.generateHash(input);
		int real = xxhash.hash(input.getBytes(), 0, input.length(), 0x9747b28c);
		assertEquals(tested, real);
	}

	@Test(expected = NullPointerException.class)
	public void testGenerateHashNull() {
		XxHashGenerator hg = new XxHashGenerator();
		assertNotNull(hg.toString());
		hg.generateHash(null);
	}

}
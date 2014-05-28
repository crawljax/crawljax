package com.crawljax.core.state.duplicatedetection;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

/**
 * HashGenerator using the xxhash32-algorithm (https://code.google.com/p/xxhash/)
 */
public class XxHashGenerator implements HashGenerator {
	
	private XXHash32 xxhash;

	/**
	 * Setup the xxHash-32 hash-generator
	 */
	public XxHashGenerator() {
		xxhash = XXHashFactory.fastestInstance().hash32();
	}

	@Override
	public int generateHash(String input) {
		return xxhash.hash(input.getBytes(), 0, input.length(), 0x9747b28c);
	}

}

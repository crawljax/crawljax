package com.crawljax.forms;

import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Class for generating random form data.
 *
 * @author dannyroest@gmail.com (Danny Roest)
 */
public class RandomInputValueGenerator {

	public static final double PROBABILITY_CHECK = 0.5;
	private static final int MAX_INT = 12345;
	private final Random random = new Random();

	/**
	 * @param length
	 * @return a random string
	 */
	private String generate(int length) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int index = new Random().nextInt(
					"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length());
			buf.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", index, index + 1);
		}
		return buf.toString();
	}

	/**
	 * @param length the length of the random string
	 * @return random string
	 */
	public String getRandomString(int length) {
		return generate(length);
	}
	
	public String getRandomNumber() {
		return Integer.toString(Math.abs(random.nextInt())%MAX_INT);
	}

	/**
	 * @return whether to check with p=0.5
	 */
	public boolean getCheck() {
		return random.nextDouble() > PROBABILITY_CHECK;
	}

	/**
	 * @param options the possible options (e.g. option item in select). The list must be non-empty
	 * @return a random item from the list
	 */
	public <T> T getRandomItem(List<T> options) {
		checkArgument(!options.isEmpty(), "Options must not be empty");
		if (options.size() == 1) {
			return options.get(0);
		} else {
			return options.get(random.nextInt(options.size()));
		}
	}
}

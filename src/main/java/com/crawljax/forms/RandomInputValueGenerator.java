package com.crawljax.forms;

import java.util.List;
import java.util.Random;

/**
 * Class for generating random form data.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class RandomInputValueGenerator {

	private final String textCharactersLowerCase = "abcdefghijklmnopqrstuvwxyz";
	private final String textCharactersUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final double PROBABILITY_CHECK = 0.5;

	/**
	 * @param characters
	 * @param length
	 * @return a random string
	 */
	private String generate(String characters, int length) {
		String str = new String();
		for (int i = 0; i < length; i++) {
			int index = new Random().nextInt(characters.length());
			str += characters.substring(index, index + 1);
		}
		return str;
	}

	/**
	 * @param length
	 *            the length of the random string
	 * @return random string
	 */
	public String getRandomString(int length) {
		return generate(textCharactersLowerCase + textCharactersUpperCase, length);
	}

	/**
	 * @return whether to check with p=0.5
	 */
	public boolean getCheck() {
		return new Random().nextDouble() > PROBABILITY_CHECK;
	}

	/**
	 * @param options
	 *            the possible options (e.g. option item in select)
	 * @return a random item from the list
	 */
	public Object getRandomOption(List<?> options) {
		int size = options.size();
		return options.get(new Random().nextInt(size - 1));
	}

}

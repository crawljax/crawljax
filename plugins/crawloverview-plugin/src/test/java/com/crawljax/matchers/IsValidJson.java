package com.crawljax.matchers;

import java.io.File;
import java.io.IOException;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IsValidJson {

	/**
	 * @return a {@link Matcher} that checks if the given {@link File} contains a valid JSON object.
	 */
	@Factory
	public static <T> Matcher<File> isValidJson() {
		return new TypeSafeMatcher<File>() {

			@Override
			public void describeTo(Description description) {
				description.appendText("Valid JSON String");
			}

			@Override
			protected boolean matchesSafely(File item) {
				boolean valid = false;
				try {
					JsonParser parser = new ObjectMapper().getFactory().createParser(item);
					while (parser.nextToken() != null) {
					}
					valid = true;
				} catch (IOException e) {
					throw new AssertionError(e);
				}

				return valid;
			}
		};
	}
}

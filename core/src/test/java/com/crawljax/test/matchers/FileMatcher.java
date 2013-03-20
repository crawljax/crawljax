package com.crawljax.test.matchers;

import java.io.File;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class FileMatcher extends TypeSafeMatcher<File> {

	@Override
	public boolean matchesSafely(File file) {
		return file.exists();
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("file which exists");
	}

	@Factory
	public static <T> Matcher<File> exists() {
		return new FileMatcher();
	}

}
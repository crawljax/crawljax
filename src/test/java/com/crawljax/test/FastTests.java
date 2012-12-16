package com.crawljax.test;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.ExcludeBaseTypeFilter;
import org.junit.runner.RunWith;

/**
 * Runs all the fast JUnit tests.
 */
@RunWith(ClasspathSuite.class)
// Loads all unit tests it finds on the classpath
@ExcludeBaseTypeFilter(BrowserTest.class)
// Excludes tests that inherit slow tests
public class FastTests {
}

package com.crawljax.test;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.extensions.cpsuite.ClasspathSuite.BaseTypeFilter;
import org.junit.runner.RunWith;


@RunWith(ClasspathSuite.class)
@BaseTypeFilter(BrowserTest.class)
public class IntegrationTests {

}

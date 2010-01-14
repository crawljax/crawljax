package com.crawljax.core;

import static org.junit.Assert.fail;

import org.junit.Test;

public class CrawljaxWithPropertiesFileTest {

	@Test
	public void runCrawljax() {
		CrawljaxController crawljax = new CrawljaxController();
		try {
			crawljax.run();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
}

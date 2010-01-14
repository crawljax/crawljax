package com.crawljax.core;

import static org.junit.Assert.fail;

import org.junit.Test;

public class CrawljaxWithPropertiesFileTest {

	@Test
	public void runCrawljax() {
		try {
			CrawljaxController crawljax = new CrawljaxController();
			crawljax.run();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
}

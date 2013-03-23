package com.crawljax.test;

import static org.junit.Assert.*;
import org.eclipse.jetty.util.resource.Resource;

import org.junit.Test;

public class TestBaseCrawler {
	BaseCrawler crawler;
	
	private void setup() {
		crawler = new BaseCrawler(Resource.newClassPathResource("sites"), "simple-site");
	}
	
	@Test
	public void testShowWebSite() throws Exception {
		try {
			crawler.showWebSite();
		}
		catch (Exception e) {
			
		}
	}
}

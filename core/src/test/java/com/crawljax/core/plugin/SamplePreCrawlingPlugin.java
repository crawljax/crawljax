package com.crawljax.core.plugin;


import com.crawljax.browser.EmbeddedBrowser;

public class SamplePreCrawlingPlugin implements PreCrawlingPlugin {

	@Override
	public void preCrawling(EmbeddedBrowser browser)
	{
		System.out.println("SUCCESS SamplePreCrawlingPlugin");
	}
}

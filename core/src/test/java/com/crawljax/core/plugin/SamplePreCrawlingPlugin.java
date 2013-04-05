package com.crawljax.core.plugin;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.configuration.ProxyConfiguration;

public class SamplePreCrawlingPlugin implements PreCrawlingPlugin,
		ProxyServerPlugin {

	@Override
	public void preCrawling(EmbeddedBrowser browser) {
		System.out
				.println("SUCCESS SamplePreCrawlingPlugin called preCrawling");
	}

	@Override
	public void proxyServer(ProxyConfiguration config) {
		System.out
				.println("SUCCESS SamplePreCrawlingPlugin called proxyServer");

	}
}

package com.crawljax.web;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class SocketLogFilter extends Filter<ILoggingEvent> {
	private final String crawlId;

	public SocketLogFilter(String crawlId) {
		this.crawlId = crawlId;
	}

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (event.getMDCPropertyMap().get("crawl_record").equals(crawlId)) {
			return FilterReply.ACCEPT;
		} else {
			return FilterReply.DENY;
		}
	}
}

package com.crawljax.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class SocketLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private LoggingSocket socket;
	private ch.qos.logback.classic.Logger rootLogger;

	public SocketLogAppender(LoggingSocket socket, String crawlId) {
		this.socket = socket;

		rootLogger =
		        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		setContext(rootLogger.getLoggerContext());
		this.addFilter(new SocketLogFilter(crawlId));
		this.start();
		rootLogger.addAppender(this);
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss.SSS");
		socket.sendText("log-" + s.format(new Date(eventObject.getTimeStamp())) + " ["
		        + eventObject.getThreadName() + "] " + eventObject.getLevel() + " "
		        + eventObject.getLoggerName() + " - " + eventObject.getFormattedMessage());
	}

	@Override
	public void stop() {
		if (rootLogger.isAttached(this)) {
			rootLogger.detachAppender(this);
			super.stop();
		}
	}

}

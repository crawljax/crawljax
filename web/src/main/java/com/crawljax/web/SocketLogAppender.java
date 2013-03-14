package com.crawljax.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class SocketLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private LoggingSocket socket;
	private ch.qos.logback.classic.Logger rootLogger;

	public SocketLogAppender(LoggingSocket socket) {
		this.socket = socket;

		rootLogger =
		        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		setContext(rootLogger.getLoggerContext());
		this.start();
		rootLogger.addAppender(this);
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		socket.sendText(eventObject.toString());
	}

	@Override
	public void stop() {
		rootLogger.detachAppender(this);
		super.stop();
	}

}

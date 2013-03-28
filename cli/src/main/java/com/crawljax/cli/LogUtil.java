package com.crawljax.cli;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.Encoder;

public class LogUtil {

	/**
	 * Configure file logging and stop console logging.
	 * 
	 * @param filename
	 *            Log to this file.
	 */
	@SuppressWarnings("unchecked")
	static void logToFile(String filename) {
		Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

		FileAppender<ILoggingEvent> fileappender = new FileAppender<>();
		fileappender.setContext(rootLogger.getLoggerContext());
		fileappender.setFile(filename);
		fileappender.setName("FILE");

		ConsoleAppender<?> console = (ConsoleAppender<?>) rootLogger.getAppender("STDOUT");
		fileappender.setEncoder((Encoder<ILoggingEvent>) console.getEncoder());

		fileappender.start();

		rootLogger.addAppender(fileappender);

		console.stop();
	}

	/**
	 * @param newLevel
	 *            for com.crawljax.*
	 */
	static void setCrawljaxLogLevel(Level newLevel) {
		Logger rootLogger = (Logger) LoggerFactory.getLogger("com.crawljax");
		rootLogger.setLevel(newLevel);
	}

	private LogUtil() {

	}
}

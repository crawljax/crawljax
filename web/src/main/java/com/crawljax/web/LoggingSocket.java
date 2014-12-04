package com.crawljax.web;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;

import com.crawljax.web.di.CrawljaxWebModule.OutputFolder;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSocket extends WebSocketAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingSocket.class);
	private final File outPutFolder;
	private SocketLogAppender appender;

	@Inject
	public LoggingSocket(@OutputFolder File outPutFolder) {
		this.outPutFolder = outPutFolder;
	}

	@Override
	public void onWebSocketConnect(Session session) {
		LOG.debug("Socket connected!");
		LogWebSocketServlet.sockets.add(this);
		super.onWebSocketConnect(session);
	}

	private void sendLogFile(String crawlId) {
		File log =
		        new File(outPutFolder, "crawl-records"
		                + File.separator + crawlId
		                + File.separator + "crawl.log");
		if (log.exists()) {
			try {
				LOG.debug("Reading the log file");
				String asString =
				        "log-<p>"
				                + Files.toString(log, Charsets.UTF_8).replace(
				                        System.getProperty("line.separator"), "</p><p>");
				asString = asString.substring(0, asString.length() - 4); // Remove last <p>
				LOG.debug("Sending the log file");
				sendText(asString);
			} catch (IOException e) {
				LOG.warn("Could not read the log file");
			}
		}

	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		LOG.debug("Socket disconnected with status code {} reason: {}", statusCode, reason);
		LogWebSocketServlet.sockets.remove(this);
		if (appender != null) {
			appender.stop();
		}
	}

	@Override
	public void onWebSocketText(String message) {
		LOG.debug("Received text: {}", message);

		if (message.startsWith("startlog")) {
			String crawlId = message.split("-")[1];
			sendLogFile(crawlId);
			appender = new SocketLogAppender(this, crawlId);
		}
		if (message.startsWith("stoplog")) {
			if (appender != null)
				appender.stop();
		}
	}

	public void sendText(String text) {
		try {
			getSession().getRemote().sendString(text);
		} catch (IOException e) {
			LOG.error("Could not send message {}", text, e);
		}
	}
}

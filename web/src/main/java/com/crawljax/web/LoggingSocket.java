package com.crawljax.web;

import java.io.IOException;
import java.util.Date;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingSocket extends WebSocketAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingSocket.class);
	private Session session;

	@Override
	public void onWebSocketConnect(Session session) {
		LOG.info("Socket connected!");
		this.session = session;
		sendText("Welcome to this socket @ " + new Date());
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		LOG.info("Socket disconnected with status code {} reason: {}", statusCode, reason);
		this.session = null;
	}

	@Override
	public void onWebSocketText(String message) {
		LOG.info("Received text: {}", message);
	}

	public void sendText(String text) {
		try {
			session.getRemote().sendString(text);
		} catch (IOException e) {
			LOG.error("Could not send message {}", text, e);
		}
	}
}

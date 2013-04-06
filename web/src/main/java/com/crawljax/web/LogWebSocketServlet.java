package com.crawljax.web;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.google.inject.Singleton;

/**
 * Socket that serves log entries.
 */
@Singleton
public class LogWebSocketServlet extends WebSocketServlet {

	public static final Set<LoggingSocket> sockets = new CopyOnWriteArraySet<LoggingSocket>();
	private static final long serialVersionUID = 4421543809294793344L;

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.register(LoggingSocket.class);
	}

	public static void sendToAll(String text) {
		if (!sockets.isEmpty())
			for (LoggingSocket socket : sockets) {
				socket.sendText(text);
			}
	}
}

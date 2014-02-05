package com.crawljax.web;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
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
	private final Provider<LoggingSocket> logSocketProvider;

	@Inject
	public LogWebSocketServlet(Provider<LoggingSocket> logSocketProvider) {
		this.logSocketProvider = logSocketProvider;
	}

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.setCreator(new WebSocketCreator() {

			@Override
			public Object createWebSocket(UpgradeRequest req, UpgradeResponse resp) {
				return logSocketProvider.get();
			}
		});
	}

	public static void sendToAll(String text) {
		if (!sockets.isEmpty())
			for (LoggingSocket socket : sockets) {
				socket.sendText(text);
			}
	}
}

package com.crawljax.web;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.google.inject.Singleton;

/**
 * Socket that serves log entries.
 */
@Singleton
public class LogWebSocketServlet extends WebSocketServlet {

	private static final long serialVersionUID = 4421543809294793344L;

	@Override
	public void configure(WebSocketServletFactory factory) {
		System.out.println("config");
		factory.register(LoggingSocket.class);
	}

}

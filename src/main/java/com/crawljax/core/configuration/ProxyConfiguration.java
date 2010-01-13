package com.crawljax.core.configuration;

/**
 * Class for passing proxy settings to Crawljax' browser factory. It is returned by the
 * ProxyServerPlugin interface.
 * 
 * @author dannyroest@gmail.com (Danny Roest)
 * @version $Id$
 */
public class ProxyConfiguration {

	/**
	 * Type of proxy this is.
	 */
	public enum ProxyType {
		HTTP_PROXY;
	}

	public static final int DEFAULT_PORT = 1234;

	private int port = DEFAULT_PORT;
	private String hostname = "localhost";
	private ProxyType type = ProxyType.HTTP_PROXY;

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @return the type
	 */
	public ProxyType getType() {
		return type;
	}

}

package com.crawljax.core.configuration;

/**
 * Class for passing proxy settings to Crawljax' browser builder. It is returned by the
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
		/**
		 * Don't use a proxy at all.
		 */
		NOTHING(0),
		/**
		 * Use a manually configured proxy (using your ProxyConfiguration object).
		 */
		MANUAL(1),
		/**
		 * Use an automatically configured proxy (using a pax file for example).
		 */
		AUTOMATIC(4),
		/**
		 * Use the proxy that is configured for your computer (system wide).
		 */
		SYSTEM_DEFAULT(5);

		private int value;

		private ProxyType(int value) {
			this.value = value;
		}

		/**
		 * Converts the enum to an int (used by Firefox internally).
		 * 
		 * @return The int representation of the enum value.
		 */
		public int toInt() {
			return value;
		}
	}

	public static final int DEFAULT_PORT = 1234;

	private int port = DEFAULT_PORT;
	private String hostname = "localhost";
	private ProxyType type = ProxyType.MANUAL;

	/**
	 * @return The port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            The port where the proxy is running on.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return The hostname.
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname
	 *            The hostname of the proxy.
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @param type
	 *            The proxy type. Currently only ProxyType.HTTP_PROXY is supported.
	 */
	public void setType(ProxyType type) {
		this.type = type;
	}

	/**
	 * @return The type.
	 */
	public ProxyType getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString() + ':' + hostname + ':' + port;
	}
}

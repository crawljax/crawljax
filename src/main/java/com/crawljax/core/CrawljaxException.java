/**
 * Created Jun 27, 2008
 */
package com.crawljax.core;

import java.util.Properties;

/**
 * @author mesbah
 * @version $Id$
 */
public class CrawljaxException extends Exception {

	private static final long serialVersionUID = 8597985648361590779L;
	private static final Properties SYSTEM_PROPERTIES = System.getProperties();
	static {
		SYSTEM_PROPERTIES.remove("java.class.path");
	}
	private static final String SYSTEMINFO =
	        "\nAdditional system information:\n" + SYSTEM_PROPERTIES;

	/**
	 * Constructs a <code>ContractorException</code> with null as its detail message.
	 */
	public CrawljaxException() {
		super(SYSTEMINFO);
	}

	/**
	 * Constructs a new <code>CrawljaxException</code> with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public CrawljaxException(final String message) {
		super(message + SYSTEMINFO);
	}

	/**
	 * Constructs a new <code>CrawljaxException</code> with the specified detail message and cause.
	 * 
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            the cause (A null value is permitted, and indicates that the cause is nonexistent
	 *            or unknown).
	 */
	public CrawljaxException(final String message, final Throwable cause) {
		super(message + SYSTEMINFO, cause);
	}

	/**
	 * Constructs a new <code>CrawljaxException</code> with the specified cause and a detail message
	 * of <code>(cause==null ? null :
	 * cause.toString())</code>
	 * 
	 * @param cause
	 *            the cause (A null value is permitted, and indicates that the cause is nonexistent
	 *            or unknown).
	 */
	public CrawljaxException(final Throwable cause) {
		super(SYSTEMINFO, cause);
	}
}

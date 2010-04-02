/**
 * Created Jun 27, 2008
 */
package com.crawljax.core;

/**
 * @author mesbah
 * @version $Id$
 */
public class CrawljaxException extends Exception {

	private static final long serialVersionUID = 8597985648361590779L;
	private static final String systemInfo =
	        "\nAdditional system information:\n" + System.getProperties();

	/**
	 * Constructs a <code>ContractorException</code> with null as its detail message.
	 */
	public CrawljaxException() {
		super(systemInfo);
	}

	/**
	 * Constructs a new <code>CrawljaxException</code> with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public CrawljaxException(final String message) {
		super(message + systemInfo);
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
		super(message + systemInfo, cause);
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
		super(systemInfo, cause);
	}
}

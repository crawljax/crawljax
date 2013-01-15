package com.crawljax.core;

/**
 * General exception for Crawljax.
 */
public class CrawljaxException extends RuntimeException {

	private static final long serialVersionUID = 8597985648361590779L;

	/**
	 * Constructs a <code>ContractorException</code> with null as its detail message.
	 */
	public CrawljaxException() {
		super();
	}

	/**
	 * Constructs a new <code>CrawljaxException</code> with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public CrawljaxException(final String message) {
		super(message);
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
		super(message, cause);
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
		super(cause);
	}
}

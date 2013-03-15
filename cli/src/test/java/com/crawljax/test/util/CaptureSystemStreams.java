package com.crawljax.test.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.rules.ExternalResource;

/**
 * Changes the {@link System#out} and {@link System#err} to a captured stream so we can inspect it
 * in a test. In the {@link #after()} method the streams are switched back to the default stream.
 */
public class CaptureSystemStreams extends ExternalResource {

	private ByteArrayOutputStream captureErrorStream = new ByteArrayOutputStream();
	private ByteArrayOutputStream captureOutStream = new ByteArrayOutputStream();
	private PrintStream originalErrorStream;
	private PrintStream originalOutStream;

	public CaptureSystemStreams() {
	}

	@Override
	protected void before() throws Throwable {
		originalErrorStream = System.err;
		originalOutStream = System.out;
		System.setErr(new PrintStream(captureErrorStream));
		System.setOut(new PrintStream(captureOutStream));
	}

	@Override
	protected void after() {
		PrintStream tempErrStream = System.err;
		System.setErr(originalErrorStream);
		tempErrStream.close();

		PrintStream tempOutStream = System.out;
		System.setOut(originalOutStream);
		tempOutStream.close();
	}

	public String getConsoleOutput() {
		return captureOutStream.toString();
	}

	public String getErrorOutput() {
		return captureErrorStream.toString();
	}
}

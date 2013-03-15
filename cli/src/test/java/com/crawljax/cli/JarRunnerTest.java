package com.crawljax.cli;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.util.CaptureSystemStreams;
import com.google.common.collect.ObjectArrays;

public class JarRunnerTest {

	@Rule
	public final CaptureSystemStreams streams = new CaptureSystemStreams();

	@Rule
	public final TemporaryFolder tmpFolder = new TemporaryFolder();

	@Test
	public void whenNoArgsCommitedItPrintsHelp() {
		new JarRunner(new String[0]);
		assertHelpWasPrinted();
	}

	@Test
	public void whenBrowserSpecifiedItIsConfigured() {
		new JarRunner(defaultArgsPlus("-b " + BrowserType.chrome.name()));
		assertThat(streams.getErrorOutput(), isEmptyString());
	}

	private String[] defaultArgsPlus(String string) {
		return ObjectArrays.concat(string.split(" "), defaultArgs(), String.class);
	}

	private String[] defaultArgs() {
		return new String[] { "http://nu.nl", tmpFolder.getRoot().getPath() };
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenUnkownBrowserSpecifiedConfigFails() {
		new JarRunner(defaultArgsPlus("-b nonExistingBrowser"));
	}

	@Test
	public void whenVersionRequestedItPrintsVersion() {
		new JarRunner(defaultArgsPlus("-v"));
		assertThat(streams.getConsoleOutput(), startsWith("Version = "));
	}

	@Test
	public void testSpecifyDepth() {
		assertThat(configForArgs("-d 123").getMaximumDepth(), is(123));
	}

	@Test(expected = NumberFormatException.class)
	public void whenDepthNotANumberPrintError() {
		configForArgs("-d abc");
	}

	public CrawljaxConfiguration configForArgs(String args) {
		JarRunner runner = new JarRunner(defaultArgsPlus(args));
		return runner.getConfig();
	}

	@Test
	public void testSpecifyMaxStates() {
		assertThat(configForArgs("-s 123").getMaximumStates(), is(123));
	}

	@Test
	public void testOverrideOutputDir() throws IOException {
		tmpFolder.newFile();
		configForArgs("-o");
	}

	@Test(expected = IllegalStateException.class)
	public void whenNotOverrideOutputDirStopOnNonEmptyDir() throws IOException {
		tmpFolder.newFile();
		new JarRunner(defaultArgs());
	}

	@Test
	public void testCustomBrowserConfig() {
		BrowserConfiguration config =
		        configForArgs("-p 123 -b " + BrowserType.chrome).getBrowserConfig();
		assertThat(config.getNumberOfBrowsers(), is(123));
		assertThat(config.getBrowsertype(), is(BrowserType.chrome));
	}

	private void assertHelpWasPrinted() {
		String helpMessage = "usage: " + JarRunner.HELP_MESSAGE;
		assertThat(streams.getConsoleOutput(), startsWith(helpMessage));
	}

	@After
	public void after() {
		assertThat(streams.getErrorOutput(), isEmptyString());
	}
}

package com.crawljax.cli;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawlElement;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.test.util.CaptureSystemStreams;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;

public class JarRunnerTest {

	@Rule
	public final CaptureSystemStreams streams = new CaptureSystemStreams();

	@Rule
	public final TemporaryFolder tmpFolder = new TemporaryFolder();

	@Test
	public void whenNoArgsCommitedItPrintsHelp() {
		new JarRunner(new String[0]);
		assertHelpWasPrinted(true);
	}

	private void assertHelpWasPrinted(boolean missingArguments) {
		String helpMessage = "usage: " + ParameterInterpeter.HELP_MESSAGE;
		if (missingArguments) {
			helpMessage =
			        JarRunner.MISSING_ARGUMENT_MESSAGE + System.lineSeparator() + helpMessage;
		}
		assertThat(streams.getConsoleOutput(), startsWith(helpMessage));
	}

	@Test
	public void whenOneArgIsInsertItAlsoPrintsHelp() {
		new JarRunner("http://nu.nl -a".split(" "));
		assertHelpWasPrinted(true);
	}

	@Test
	public void whenHelpArgumentSpecifiedPrintHelp() {
		new JarRunner("-h".split(" "));
		assertHelpWasPrinted(false);
	}

	@Test
	public void whenBrowserSpecifiedItIsConfigured() {
		new JarRunner(defaultArgsPlus("-b " + BrowserType.CHROME.name()));
		assertThat(streams.getErrorOutput(), isEmptyString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenRemoteBrowserSpecifiedWithoutUrlItStops() {
		new JarRunner(defaultArgsPlus("-b " + BrowserType.REMOTE.name()));
	}

	@Test
	public void whenRemoteBrowserSpecifiedWithUrlItResumes() {
		JarRunner runner =
		        new JarRunner(defaultArgsPlus("-b " + BrowserType.REMOTE.name()
		                + " -" + ParameterInterpeter.BROWSER_REMOTE_URL + " localhost:9000"));
		assertThat(streams.getErrorOutput(), isEmptyString());
		BrowserConfiguration config = runner.getConfig().getBrowserConfig();
		assertThat(config.getBrowsertype(), is(BrowserType.REMOTE));
		assertThat(config.getRemoteHubUrl(), is("localhost:9000"));

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
		new JarRunner(defaultArgsPlus("-version"));
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
		        configForArgs("-p 123 -b " + BrowserType.CHROME).getBrowserConfig();
		assertThat(config.getNumberOfBrowsers(), is(123));
		assertThat(config.getBrowsertype(), is(BrowserType.CHROME));
	}

	@Test
	public void testCrawlHiddenAnchors() {
		assertThat(configForArgs("-a").getCrawlRules().isCrawlHiddenAnchors(), is(true));
	}

	@Test
	public void testCustomClicks() {
		CrawlRules crawlRules = configForArgs("--click a,b,c").getCrawlRules();
		ImmutableList<CrawlElement> includedElements =
		        crawlRules.getPreCrawlConfig().getIncludedElements();
		assertThat(includedElements, hasSize(3));
	}

	@Test
	public void testWithMaxCrawlTime() {
		assertThat(configForArgs("-t 123").getMaximumRuntime(),
		        is(TimeUnit.MINUTES.toMillis(123)));
	}

	@Test
	public void testWaitAfterReload() {
		CrawlRules crawlRules =
		        configForArgs("-" + ParameterInterpeter.WAIT_AFTER_RELOAD + " 123")
		                .getCrawlRules();
		assertThat(crawlRules.getWaitAfterReloadUrl(), is(123L));
	}

	@Test
	public void testWaitAfterEvent() {
		CrawlRules crawlRules =
		        configForArgs("-" + ParameterInterpeter.WAIT_AFTER_EVENT + " 123")
		                .getCrawlRules();
		assertThat(crawlRules.getWaitAfterEvent(), is(123L));
	}

	@After
	public void after() {
		assertThat(streams.getErrorOutput(), isEmptyString());
	}
}

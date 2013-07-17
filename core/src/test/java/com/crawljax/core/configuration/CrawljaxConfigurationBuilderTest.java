package com.crawljax.core.configuration;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;

public class CrawljaxConfigurationBuilderTest {

	@Test(expected = IllegalArgumentException.class)
	public void negativeMaximumStatesIsNotAllowed() throws Exception {
		testBuilder().setMaximumStates(-1).build();
	}

	private CrawljaxConfigurationBuilder testBuilder() {
		return CrawljaxConfiguration.builderFor("http://localhost");
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDepthIsNotAllowed() throws Exception {
		testBuilder().setMaximumDepth(-1).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeRuntimeIsNotAllowed() throws Exception {
		testBuilder().setMaximumRunTime(-1L, TimeUnit.SECONDS).build();
	}

	@Test
	public void noArgsBuilderWorksFine() throws Exception {
		testBuilder().build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void ifOutputIsFileNotFolderReject() throws Exception {
		File file = File.createTempFile(getClass().getSimpleName(), "tmp");
		assertThat(file.exists(), is(true));
		testBuilder().setOutputDirectory(file).build();
	}

}

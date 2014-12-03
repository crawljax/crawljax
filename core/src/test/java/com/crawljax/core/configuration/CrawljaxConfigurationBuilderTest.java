package com.crawljax.core.configuration;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.Is;
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

	@Test(expected = IllegalStateException.class)
	public void ifCannotCreateOutputFolderReject() throws Exception {
		File file = new File("/this/should/not/be/writable");
		testBuilder().setOutputDirectory(file).build();
	}

	@Test
	public void whenSpecifyingBasicAuthTheUrlShouldBePreserved() {
		String url = "https://example.com/test/?a=b#anchor";
		CrawljaxConfiguration conf =
		        CrawljaxConfiguration.builderFor(url).setBasicAuth("username", "password")
		                .build();
		assertThat(conf.getBasicAuthUrl().toString(),
		        Is.is("https://username:password@example.com/test/?a=b#anchor"));
	}

}

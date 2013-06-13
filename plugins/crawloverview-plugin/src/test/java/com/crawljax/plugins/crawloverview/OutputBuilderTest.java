package com.crawljax.plugins.crawloverview;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class OutputBuilderTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();
	private OutputBuilder builder;
	private File outputFolder;

	@Before
	public void setup() {
		outputFolder = folder.getRoot();
		builder = new OutputBuilder(outputFolder);
	}

	@Test
	public void testNewScreenShotFileIsWritable() throws IOException {
		FileWriter fwriter = new FileWriter(builder.newScreenShotFile("test"));
		fwriter.write("blabla");
		fwriter.close();
	}

	@Test
	public void whenDomPersistedTheLoadFunctionReturnsTheSameDom() {
		String dom = "Some DOM string";
		builder.persistDom("test-state", dom);
		assertThat(builder.getDom("test-state"), is(dom));
	}

	@Test
	public void whenNullPersistedTheDomIsPersistedAsEmpty() {
		builder.persistDom("test-state", null);
		assertThat(builder.getDom("test-state"), isEmptyString());
	}

}

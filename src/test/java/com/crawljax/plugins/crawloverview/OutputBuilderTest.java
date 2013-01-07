package com.crawljax.plugins.crawloverview;

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

	@Before
	public void setup() {
		builder = new OutputBuilder(folder.getRoot());
	}

	@Test
	public void testNewScreenShotFileIsWritable() throws IOException {
		FileWriter fwriter = new FileWriter(builder.newScreenShotFile("test"));
		fwriter.write("blabla");
		fwriter.close();
	}

}

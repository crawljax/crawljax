package com.crawljax.plugins.crawloverview;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class OutputBuilderTest {

	private static final String THUMB_HASH = "b0bd23b784b853ff760c8fb9becd25d8";

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
	public void makingAThumbnailDoesntThrowException() throws Exception {
		URI file = OutputBuilderTest.class.getResource("/screenshot.png").toURI();
		File screenShot = new File(file);
		builder.makeThumbNail(screenShot, "test-state");
		String thumbName = OutputBuilder.SCREENSHOT_FOLDER_NAME + File.separatorChar
		        + "test-state_small.jpg";
		File thumb = new File(outputFolder, thumbName);
		assertThat("File exists", thumb.exists(), is(true));
		String hash = Files.hash(thumb, Hashing.md5()).toString();
		assertThat("Thumb hash doesn't match", hash, is(THUMB_HASH));
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

package com.crawljax.plugins.crawloverview;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class ImageWriterTest {

	private static final String THUMB_HASH = "b0bd23b784b853ff760c8fb9becd25d8";
	private static final String FULL_HASH = "66cea3363d36400684b98eb78d38b5b9";

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void makingAThumbnailDoesntThrowException() throws Exception {
		URI file = ImageWriterTest.class.getResource("/screenshot.png").toURI();
		File screenShot = new File(file);
		File fullScreenShot = folder.newFile();
		File thumbnail = folder.newFile();

		ImageWriter.writeScreenShotAndThumbnail(ImageIO.read(screenShot), fullScreenShot,
		        thumbnail);

		assertThat("Thumbnail exists", thumbnail.exists(), is(true));
		String hash = Files.hash(thumbnail, Hashing.md5()).toString();
		assertThat("Thumb hash doesn't match", hash, is(THUMB_HASH));

		assertThat("Screenshot exists", fullScreenShot.exists(), is(true));
		hash = Files.hash(fullScreenShot, Hashing.md5()).toString();

		assertThat("Screenshot hash doesn't match", hash, is(FULL_HASH));
	}
}

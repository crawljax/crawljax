package com.crawljax.stateabstractions.visual;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.crawljax.stateabstractions.visual.imagehashes.DHash;

public class VisualDHashTest {

	final static DHash DHASH = new DHash();

	static {
		OpenCVLoad.load();
	}

	@Test
	public void testDHash() {

		String file = VisualDHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
		String hash = DHASH.getDHash(file);
		assertThat(hash, is("1011011111111110110111101100111011011110010111100101110100011101"));

		file = VisualDHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
		hash = DHASH.getDHash(file);
		assertThat(hash, is("0010101111000100110011100100111101010011000100011010000101010111"));

		file = VisualDHashTest.class.getResource("/screenshots/oracle.png").getPath();
		hash = DHASH.getDHash(file);
		assertThat(hash, is("1110000000000000000100001101000010010000000100000000000000010001"));

		file = VisualDHashTest.class.getResource("/screenshots/test.png").getPath();
		hash = DHASH.getDHash(file);
		assertThat(hash, is("1110000000000000000100001101100010010000000100000000000000010001"));
	}

	@Test
	public void testDHashIdenticalImages() {

		String file = VisualDHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
		String file2 = VisualDHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
		assertTrue(DHASH.imagesPerceptuallySimilar(file, file2));

		file = VisualDHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
		file2 = VisualDHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
		assertTrue(DHASH.imagesPerceptuallySimilar(file, file2));

		file = VisualDHashTest.class.getResource("/screenshots/oracle.png").getPath();
		file2 = VisualDHashTest.class.getResource("/screenshots/oracle.png").getPath();
		assertTrue(DHASH.imagesPerceptuallySimilar(file, file2));

		file = VisualDHashTest.class.getResource("/screenshots/test.png").getPath();
		file2 = VisualDHashTest.class.getResource("/screenshots/test.png").getPath();
		assertTrue(DHASH.imagesPerceptuallySimilar(file, file2));
	}

	@Test
	public void testDHashSimilarImages() {

		String file = VisualDHashTest.class.getResource("/screenshots/oracle.png").getPath();
		String file2 = VisualDHashTest.class.getResource("/screenshots/test.png").getPath();
		assertTrue(DHASH.imagesPerceptuallySimilar(file, file2));

	}

	@Test
	public void testDHashDifferentImages() {

		String file = VisualDHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
		String file2 = VisualDHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
		assertFalse(DHASH.imagesPerceptuallySimilar(file, file2));

	}

}

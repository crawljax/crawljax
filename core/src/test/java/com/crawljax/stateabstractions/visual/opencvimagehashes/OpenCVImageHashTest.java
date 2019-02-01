package com.crawljax.stateabstractions.visual.opencvimagehashes;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opencv.core.Mat;

import com.crawljax.stateabstractions.visual.imagehashes.AverageImageHash;
import com.crawljax.stateabstractions.visual.imagehashes.BlockMeanImageHash;
import com.crawljax.stateabstractions.visual.imagehashes.ColorMomentImageHash;
import com.crawljax.stateabstractions.visual.imagehashes.MarrHildrethImageHash;
import com.crawljax.stateabstractions.visual.imagehashes.PerceptualImageHash;
import com.crawljax.stateabstractions.visual.imagehashes.RadialVarianceImageHash;
import com.crawljax.stateabstractions.visual.imagehashes.VisHash;

@RunWith(Parameterized.class)
public class OpenCVImageHashTest {

	@Parameters(name = "{index}: {0}")
	public static List<Object> data() {
		return Arrays.asList(new Object[] { new BlockMeanImageHash(0.1),
		        new AverageImageHash(0.2), new ColorMomentImageHash(0.1),
		        new MarrHildrethImageHash(0.3), new PerceptualImageHash(0.1),
		        new RadialVarianceImageHash(0.1) });
	}

	private final VisHash visHash;

	public OpenCVImageHashTest(VisHash visHashInput) {
		visHash = visHashInput;
	}

	@Test
	public void testOpenCVImageHashIdenticalImages() {

		String file =
		        OpenCVImageHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
		String file2 =
		        OpenCVImageHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
		Mat hash1 = visHash.getHash(file);
		Mat hash2 = visHash.getHash(file2);
		double diff = visHash.compare(hash1, hash2);
		assertTrue(diff == 0.0);

		file = OpenCVImageHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
		file2 = OpenCVImageHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
		hash1 = visHash.getHash(file);
		hash2 = visHash.getHash(file2);
		diff = visHash.compare(hash1, hash2);
		assertTrue(diff == 0.0);

		file = OpenCVImageHashTest.class.getResource("/screenshots/oracle.png").getPath();
		file2 = OpenCVImageHashTest.class.getResource("/screenshots/oracle.png").getPath();
		hash1 = visHash.getHash(file);
		hash2 = visHash.getHash(file2);
		diff = visHash.compare(hash1, hash2);
		assertTrue(diff == 0.0);

		file = OpenCVImageHashTest.class.getResource("/screenshots/test.png").getPath();
		file2 = OpenCVImageHashTest.class.getResource("/screenshots/test.png").getPath();
		hash1 = visHash.getHash(file);
		hash2 = visHash.getHash(file2);
		diff = visHash.compare(hash1, hash2);
		assertTrue(diff == 0.0);

	}

	@Test
	public void testOpenCVImageHashSimilarImages() {

		String file =
		        OpenCVImageHashTest.class.getResource("/screenshots/PetClinicND1.png").getPath();
		String file2 =
		        OpenCVImageHashTest.class.getResource("/screenshots/PetClinicND2.png").getPath();

		Mat hash1 = visHash.getHash(file);
		Mat hash2 = visHash.getHash(file2);
		double diff = visHash.compare(hash1, hash2);
		assertTrue(diff < visHash.maxThreshold);
	}

	@Test
	public void testOpenCVImageHashDifferentImages() {

		String file =
		        OpenCVImageHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
		String file2 =
		        OpenCVImageHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
		Mat hash1 = visHash.getHash(file);
		Mat hash2 = visHash.getHash(file2);
		double diff = visHash.compare(hash1, hash2);
		assertTrue(diff > 0.0);
	}
}

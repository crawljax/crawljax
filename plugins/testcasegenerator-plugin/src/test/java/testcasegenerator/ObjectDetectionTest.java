package testcasegenerator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_java;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.crawljax.plugins.testcasegenerator.visualdiff.ObjectDetection;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.IPageObjectFactory;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.MD5PageObjectFactory;

public class ObjectDetectionTest {

	IPageObjectFactory pageObjectFactory = null;

	@Before
	public void setUp() {
		pageObjectFactory = new MD5PageObjectFactory();
		Loader.load(opencv_java.class);
	}

	@Test
	public void testObjectDetection() {

		/* Run the detection algorithm. */
		String name = "townshoes";
		ObjectDetection detection = new ObjectDetection(pageObjectFactory, "src/test/resources/" + name + ".png");

		detection.detectObjects();

		/* Write the annotated file to disk. */
		String folderName = "target/output/";
		String fileName = folderName + "townshoes-objects" + Math.random() + ".png";
		try {
			ObjectDetection.directoryCheck(folderName);
			Imgcodecs.imwrite(fileName, detection.getAnnotated());
			File created = new File(fileName);
			assertTrue(created.exists());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testWebPage() {

		/* Run the detection algorithm. */
		String name = "state12";
		ObjectDetection detection = new ObjectDetection(pageObjectFactory, "src/test/resources/" + name + ".jpg");
		detection.detectObjects();

		/* Write the annotated file to disk. */
		String folderName = "target/output/";
		String fileName = folderName + "state12" + Math.random() + ".png";
		try {
			ObjectDetection.directoryCheck(folderName);
			Imgcodecs.imwrite(fileName, detection.getAnnotated());
			File created = new File(fileName);
			assertTrue(created.exists());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testSubtract() {

		Mat image1 = Imgcodecs.imread("src/test/resources/state12.png");
		Mat image2 = Imgcodecs.imread("src/test/resources/state1.png");

		Mat endResult = new Mat();
		Core.subtract(image1, image2, endResult);

		String folderName = "target/output/diff/";
		String fileName = folderName + "diffs" + Math.random() + ".png";
		try {
			ObjectDetection.directoryCheck(folderName);
			Imgcodecs.imwrite(fileName, endResult);
			File created = new File(fileName);
			assertTrue(created.exists());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
}

package testcasegenerator;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.opencv.imgcodecs.Imgcodecs;

import com.crawljax.plugins.testcasegenerator.visualdiff.ObjectDetection;
import com.crawljax.plugins.testcasegenerator.visualdiff.ObjectDiff;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.AveragePageObjectFactory;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.IPageObjectFactory;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.MD5PageObjectFactory;

public class ObjectDiffTest {

	@Test
	public void testMD5Hash() {

		IPageObjectFactory pageObjectFactory = new MD5PageObjectFactory();

		/* Run the detection algorithm. */
		ObjectDetection srcDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/townshoes-src.png");
		ObjectDetection dstDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/townshoes-dst.png");

		srcDetection.detectObjects();
		dstDetection.detectObjects();

		/* Do the visual diff. */
		ObjectDiff diff = new ObjectDiff(srcDetection.getPage(), dstDetection.getPage());
		diff.diff();

		/* Write the annotated file to disk. */
		String folderName = "target/output/";
		String srcAnnotatedFileName = folderName + "townshoes-src-md5-annotated.png";
		String dstAnnotatedFileName = folderName + "townshoes-dst-md5-annotated.png";
		try {
			ObjectDetection.directoryCheck(folderName);
			Imgcodecs.imwrite(srcAnnotatedFileName, diff.annotateOldPage());
			Imgcodecs.imwrite(dstAnnotatedFileName, diff.annotateNewPage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testAvgColorHash() {

		IPageObjectFactory pageObjectFactory = new AveragePageObjectFactory();

		/* Run the detection algorithm. */
		ObjectDetection srcDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/townshoes-src.png");
		ObjectDetection dstDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/townshoes-dst.png");

		srcDetection.detectObjects();
		dstDetection.detectObjects();

		/* Do the visual diff. */
		ObjectDiff diff = new ObjectDiff(srcDetection.getPage(), dstDetection.getPage());
		diff.diff();

		/* Write the annotated file to disk. */
		String folderName = "target/output/";
		String srcAnnotatedFileName = folderName + "townshoes-src-avg-annotated.png";
		String dstAnnotatedFileName = folderName + "townshoes-dst-avg-annotated.png";
		try {
			ObjectDetection.directoryCheck(folderName);
			Imgcodecs.imwrite(srcAnnotatedFileName, diff.annotateOldPage());
			Imgcodecs.imwrite(dstAnnotatedFileName, diff.annotateNewPage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testRoverDriver() {

		IPageObjectFactory pageObjectFactory = new AveragePageObjectFactory();

		/* Run the detection algorithm. */
		ObjectDetection srcDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/mct-rover-weather.png");
		ObjectDetection dstDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/mct-rover-weather_new.png");

		srcDetection.detectObjects();
		dstDetection.detectObjects();

		/* Do the visual diff. */
		ObjectDiff diff = new ObjectDiff(srcDetection.getPage(), dstDetection.getPage(), false);
		diff.diff();

		/* Write the annotated file to disk. */
		String folderName = "target/output/";
		String srcAnnotatedFileName = folderName + "mct-rover-weather-src-annotated.png";
		String dstAnnotatedFileName = folderName + "mct-rover-weather-dst-annotated.png";
		try {
			ObjectDetection.directoryCheck(folderName);
			Imgcodecs.imwrite(srcAnnotatedFileName, diff.annotateOldPage());
			Imgcodecs.imwrite(dstAnnotatedFileName, diff.annotateNewPage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testCrawljaxTestsite() {

		IPageObjectFactory pageObjectFactory = new AveragePageObjectFactory();

		/* Run the detection algorithm. */
		ObjectDetection srcDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/crawljax-src.jpg");
		ObjectDetection dstDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/crawljax-dst.jpg");

		srcDetection.detectObjects();
		dstDetection.detectObjects();

		/* Do the visual diff. */
		ObjectDiff diff = new ObjectDiff(srcDetection.getPage(), dstDetection.getPage(), false);
		diff.diff();

		/* Write the annotated file to disk. */
		String folderName = "target/output/";
		String srcAnnotatedFileName = folderName + "crawljax-src-annotated.png";
		String dstAnnotatedFileName = folderName + "crawljax-dst-annotated.png";
		try {
			ObjectDetection.directoryCheck(folderName);
			Imgcodecs.imwrite(srcAnnotatedFileName, diff.annotateOldPage());
			Imgcodecs.imwrite(dstAnnotatedFileName, diff.annotateNewPage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

	@Test
	public void testHuaweiMobile() {

		IPageObjectFactory pageObjectFactory = new AveragePageObjectFactory();

		/* Run the detection algorithm. */
		ObjectDetection srcDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/huawei-mobile_old.png");
		ObjectDetection dstDetection = new ObjectDetection(pageObjectFactory, "src/test/resources/huawei-mobile_new.png");

		srcDetection.detectObjects();
		dstDetection.detectObjects();

		/* Do the visual diff. */
		ObjectDiff diff = new ObjectDiff(srcDetection.getPage(), dstDetection.getPage(), false);
		diff.diff();

		/* Write the annotated file to disk. */
		String folderName = "target/output/";
		String srcAnnotatedFileName = folderName + "huawei-mobile-src-annotated.png";
		String dstAnnotatedFileName = folderName + "huawei-mobile-dst-annotated.png";
		try {
			ObjectDetection.directoryCheck(folderName);
			Imgcodecs.imwrite(srcAnnotatedFileName, diff.annotateOldPage());
			Imgcodecs.imwrite(dstAnnotatedFileName, diff.annotateNewPage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}


}

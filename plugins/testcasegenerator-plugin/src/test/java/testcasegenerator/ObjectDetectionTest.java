package testcasegenerator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.crawljax.plugins.testcasegenerator.visualdiff.ObjectDetection;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.IPageObjectFactory;
import com.crawljax.plugins.testcasegenerator.visualdiff.pageobjects.MD5PageObjectFactory;
import com.crawljax.stateabstractions.visual.OpenCVLoad;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.opencv.imgcodecs.Imgcodecs;

public class ObjectDetectionTest {

    IPageObjectFactory pageObjectFactory = null;

    @Before
    public void setUp() {
        pageObjectFactory = new MD5PageObjectFactory();
        OpenCVLoad.load();
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
}

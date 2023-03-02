package com.crawljax.stateabstractions.visual;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.crawljax.stateabstractions.visual.imagehashes.DHash;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Test;

public class VisualDHashTest {

    static final DHash DHASH = new DHash();

    static {
        OpenCVLoad.load();
    }

    @Test
    public void testDHash() throws IOException {

        String file =
                VisualDHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
        String hash = DHASH.getDHash(ImageIO.read(new File(file)));
        assertThat(hash, is("1011011111111110110111101100111011011110010111100101110100011101"));

        file = VisualDHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
        hash = DHASH.getDHash(ImageIO.read(new File(file)));
        assertThat(hash, is("0010101111000100110011100100110101010011000100011010000101010011"));

        file = VisualDHashTest.class.getResource("/screenshots/oracle.png").getPath();
        hash = DHASH.getDHash(ImageIO.read(new File(file)));
        assertThat(hash, is("1110000000000000000100001101000010010000000100000000000000010001"));

        file = VisualDHashTest.class.getResource("/screenshots/test.png").getPath();
        hash = DHASH.getDHash(ImageIO.read(new File(file)));
        assertThat(hash, is("1110000000000000000100001101100010010000000100000000000000010001"));
    }

    @Test
    public void testDHashIdenticalImages() throws IOException {

        String file =
                VisualDHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
        String file2 =
                VisualDHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
        assertTrue(DHASH.imagesPerceptuallySimilar(ImageIO.read(new File(file)), ImageIO.read(new File(file2))));

        file = VisualDHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
        file2 = VisualDHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
        assertTrue(DHASH.imagesPerceptuallySimilar(ImageIO.read(new File(file)), ImageIO.read(new File(file2))));

        file = VisualDHashTest.class.getResource("/screenshots/oracle.png").getPath();
        file2 = VisualDHashTest.class.getResource("/screenshots/oracle.png").getPath();
        assertTrue(DHASH.imagesPerceptuallySimilar(ImageIO.read(new File(file)), ImageIO.read(new File(file2))));

        file = VisualDHashTest.class.getResource("/screenshots/test.png").getPath();
        file2 = VisualDHashTest.class.getResource("/screenshots/test.png").getPath();
        assertTrue(DHASH.imagesPerceptuallySimilar(ImageIO.read(new File(file)), ImageIO.read(new File(file2))));
    }

    @Test
    public void testDHashSimilarImages() throws IOException {

        String file =
                VisualDHashTest.class.getResource("/screenshots/oracle.png").getPath();
        String file2 =
                VisualDHashTest.class.getResource("/screenshots/test.png").getPath();
        assertTrue(DHASH.imagesPerceptuallySimilar(ImageIO.read(new File(file)), ImageIO.read(new File(file2))));
    }

    @Test
    public void testDHashDifferentImages() throws IOException {

        String file =
                VisualDHashTest.class.getResource("/screenshots/bookscene.jpg").getPath();
        String file2 =
                VisualDHashTest.class.getResource("/screenshots/bookobject.jpg").getPath();
        assertFalse(DHASH.imagesPerceptuallySimilar(ImageIO.read(new File(file)), ImageIO.read(new File(file2))));
    }
}

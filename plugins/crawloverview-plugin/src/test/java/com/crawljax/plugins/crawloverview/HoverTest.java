package com.crawljax.plugins.crawloverview;

import static com.crawljax.plugins.crawloverview.CandidateElementMatcher.element;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.State;

public class HoverTest {

	private static final int MIN_HEIGHT = 500;

	private static final int MIN_WIDHT = 1024;

	private static final Logger LOG = LoggerFactory.getLogger(HoverTest.class);

	private static OutPutModel result;

	private static boolean resolutionBigEnough;

	@ClassRule
	public static final RunHoverCrawl HOVER_CRAWL = new RunHoverCrawl();

	@BeforeClass
	public static void runHoverTest() throws Exception {
		result = HOVER_CRAWL.getResult();
		resolutionBigEnough = calculateResolution(RunHoverCrawl.getOutDir());
	}

	private static boolean calculateResolution(File outFile) throws IOException {
		File indexScreensShot = new File(outFile, "screenshots/index.jpg");
		assertThat(indexScreensShot.exists(), is(true));
		BufferedImage img = ImageIO.read(indexScreensShot);
		boolean enough = img.getWidth() > MIN_WIDHT && img.getHeight() > MIN_HEIGHT;
		LOG.debug("Images size is {} x {}. Good enough = {}", img.getWidth(), img.getHeight(),
		        enough);
		return enough;
	}

	@Test
	public void verifyIndexHoversCorrect() {
		State state = result.getStates().get("index");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat("Number of hovers", candidates, hasSize(3));
		Assume.assumeTrue(resolutionBigEnough);
		assertThat(candidates, hasItem(element(new Point(48, 118), new Dimension(52, 16))));
		assertThat(candidates, hasItem(element(new Point(48, 137), new Dimension(51, 16))));
		assertThat(candidates, hasItem(element(new Point(48, 156), new Dimension(200, 16))));
	}

	@Test
	public void verifyPageAHoversCorrect() {
		State state = getStateByFileName("a.html");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(1));
		Assume.assumeTrue(resolutionBigEnough);
		assertThat(candidates, hasItem(element(new Point(58, 147), new Dimension(89, 16))));
	}

	@Test
	public void verifyPageBHoversCorrect() {
		State state = getStateByFileName("b.html");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(1));
		Assume.assumeTrue(resolutionBigEnough);
		assertThat(candidates, hasItem(element(new Point(60, 168), new Dimension(51, 16))));
	}

	@Test
	public void verifyPageCHoversCorrect() {
		State state = getStateByFileName("c.html");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(2));
		// The dimensions can't be checked because they are dynamic.
	}

	private State getStateByFileName(String name) {
		for (State state : result.getStates().values()) {
			if (state.getUrl().endsWith(name)) {
				return state;
			}
		}
		fail("State with file name " + name + " wasn't found in " + result.getStates());
		return null;
	}

}

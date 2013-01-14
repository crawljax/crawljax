package com.crawljax.plugins.crawloverview;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Factory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import com.crawljax.crawljax_plugins_plugin.SampleCrawler;
import com.crawljax.plugins.crawloverview.model.CandidateElementPosition;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.State;

public class HoverTest {

	private static class ElementMatcher extends CustomMatcher<CandidateElementPosition> {

		private CandidateElementPosition actual;

		public ElementMatcher(CandidateElementPosition actual) {
			super("A " + CandidateElementPosition.class.getName() + " with coordinates");
			this.actual = actual;
		}

		@Override
		public boolean matches(Object item) {
			if (item instanceof CandidateElementPosition) {
				CandidateElementPosition element = (CandidateElementPosition) item;
				return element.getLeft() == actual.getLeft()
				        && element.getTop() == actual.getTop()
				        && element.getWidth() == actual.getWidth()
				        && element.getHeight() == actual.getHeight();
			} else {
				return false;
			}
		}

	}

	@Factory
	public static ElementMatcher element(Point point, Dimension size) {
		return new ElementMatcher(new CandidateElementPosition(null, point, size));
	}

	private static OutPutModel result;

	@BeforeClass
	public static void runHoverTest() throws Exception {
		SampleCrawler hoverSiteCrawl = new SampleCrawler("hover-test-site") {
		};
		hoverSiteCrawl.setup();
		hoverSiteCrawl.getCrawlSpec().setDepth(0);
		File outFile = new File("/tmp/crawlout");
		FileUtils.deleteQuietly(outFile);
		CrawlOverview plugin = new CrawlOverview(outFile);
		hoverSiteCrawl.getConfig().addPlugin(plugin);
		hoverSiteCrawl.crawl();
		result = plugin.getResult();
	}

	@Test
	public void verifyIndexHoversCorrect() {
		State state = result.getStates().get("index");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(2));
		assertThat(candidates, hasItem(element(new Point(8, 118), new Dimension(52, 16))));
		assertThat(candidates, hasItem(element(new Point(64, 118), new Dimension(51, 16))));
	}

	@Test
	public void verifyPageAHoversCorrect() {
		State state = getStateByFileName("a.html");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(1));
		assertThat(candidates, hasItem(element(new Point(58, 147), new Dimension(89, 16))));
	}

	@Test
	public void verifyPageBHoversCorrect() {
		State state = getStateByFileName("b.html");
		assertThat(state, is(notNullValue()));
		List<CandidateElementPosition> candidates = state.getCandidateElements();
		assertThat(candidates, hasSize(1));
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

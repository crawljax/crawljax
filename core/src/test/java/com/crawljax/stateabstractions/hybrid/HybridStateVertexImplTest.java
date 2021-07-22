package com.crawljax.stateabstractions.hybrid;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.fragmentation.Fragment;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.util.DomUtils;
import com.crawljax.vips_selenium.VipsRectangle;
import com.crawljax.vips_selenium.VipsSelenium;

import junit.framework.Assert;

public class HybridStateVertexImplTest {

	public static StateVertex getState(int id, String url, FragmentManager manager) {
		VipsSelenium vips = new VipsSelenium(url);
		// VipsSeleniumParser parser = new VipsSeleniumParser(vips);
		List<VipsRectangle> rectangles = vips.startSegmentation();
		String domString = DomUtils.getDocumentToString(vips.dom);
		HybridStateVertexFactory factory = new HybridStateVertexFactory(0, CrawljaxConfiguration.builderFor(url),
				false);
		StateVertex state = factory.newStateVertex(id, url, "test" + id, domString, domString, null);
		// StateVertexImpl state = new StateVertexImpl(0,url, "test", domString,
		// domString);
		state.setDocument(vips.dom);
		((HybridStateVertexImpl) state).setImage(vips.viewport);
		state.addFragments(rectangles, vips.driver);
		for (Fragment fragment : state.getFragments()) {
			manager.addFragment(fragment, false);
			boolean useful = FragmentManager.usefulFragment(fragment);
			System.out.println(fragment.getId() + " is Useful : " + useful);
		}
		vips.cleanup();

		return state;
	}

	@Ignore
	@Test
	public void testDynamicFragments() {
		String url1 = "http://localhost:9966/petclinic/owners/2.html";
		String url2 = "http://localhost:9966/petclinic/owners/10.html";
		FragmentManager fragmentManager = new FragmentManager(null);

		StateVertex state1 = getState(0, url1, fragmentManager);

		StateVertex state2 = getState(1, url2, fragmentManager);

		StateComparision comp1 = fragmentManager.areND2(state1, state2);

		StateComparision comp = fragmentManager.cacheStateComparision(state2, state1, true);

		Assert.assertEquals(StateComparision.NEARDUPLICATE2, comp);
	}

}

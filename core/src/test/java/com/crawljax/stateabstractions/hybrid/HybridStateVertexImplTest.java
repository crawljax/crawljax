package com.crawljax.stateabstractions.hybrid;

import java.io.File;
import java.util.List;

import com.crawljax.browser.BrowserProvider;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.fragmentation.FragmentationPlugin;
import org.junit.Ignore;
import org.junit.Rule;
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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

@Ignore
public class HybridStateVertexImplTest {
	@Rule
	public final BrowserProvider provider = new BrowserProvider();
	private FragGenStateVertexFactory factory;

	public StateVertex getState(int id, String url, WebDriver driver, FragmentManager manager) {
		VipsSelenium vips = new VipsSelenium(url, driver);
		// VipsSeleniumParser parser = new VipsSeleniumParser(vips);
		List<VipsRectangle> rectangles = vips.startSegmentation();
		String domString = DomUtils.getDocumentToString(vips.dom);
		factory = new FragGenStateVertexFactory(0, CrawljaxConfiguration.builderFor(url),
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

	public StateVertex getStateNew(int id, String url, EmbeddedBrowser browser, FragmentManager manager){
		factory = new FragGenStateVertexFactory(0, CrawljaxConfiguration.builderFor(url),
				false);
		StateVertex state = factory.newStateVertex(0, browser.getCurrentUrl(), "state" + id,
				browser.getStrippedDom(), browser.getStrippedDomWithoutIframeContent(), browser);
		FragmentationPlugin.fragmentState(state, manager, browser, new File("testOutput"), true);
		return state;
	}

	@Ignore
	@Test
	public void testDynamicFragments() {
		String url1 = "http://localhost:9966/petclinic/owners/2.html";
		String url2 = "http://localhost:9966/petclinic/owners/10.html";
		FragmentManager fragmentManager = new FragmentManager(null);
		BrowserProvider provider = new BrowserProvider();
		RemoteWebDriver driver  = provider.newBrowser();
		StateVertex state1 = getState(0, url1, driver, fragmentManager);

		StateVertex state2 = getState(1, url2, driver, fragmentManager);

		StateComparision comp1 = fragmentManager.areND2(state1, state2);

		StateComparision comp = fragmentManager.cacheStateComparision(state2, state1, true);

		Assert.assertEquals(StateComparision.NEARDUPLICATE2, comp);
	}

	@Test
	public void testMDHFrag() throws InterruptedException {
		System.setProperty("test.browser", "CHROME");
		String url= "https://localhost:9448/mdh-tss/";
		EmbeddedBrowser browser = provider.newEmbeddedBrowser();
		WebDriver driver = browser.getWebDriver();
		driver.navigate().to(url);
		driver.findElement(By.tagName("body")).sendKeys("thisisunsafe");
		UtilsSelenium.sendKeys(driver, driver.findElements(By.tagName("body")).get(0), "thisisunsafe");

		UtilsSelenium.click(driver, driver.findElement(By.xpath("/html/body/div/div/div/app-root/gdpr-page/ibm-modal/ibm-overlay/section/div/ibm-modal-footer/footer/button[1]")));

		Thread.sleep(5000);
//		BufferedImage viewPort = browser.getScreenShotAsBufferedImage(500);
		getStateNew(0, url, browser, new FragmentManager(null));
		browser.close();
	}
}

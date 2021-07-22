package com.crawljax.vips_selenium;

import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StatePair.StateComparision;
import com.crawljax.core.state.StateVertex;
import com.crawljax.fragmentation.Fragment;
import com.crawljax.fragmentation.FragmentManager;
import com.crawljax.fragmentation.FragmentationPlugin;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexFactory;

public class Test {
	
	public static void navigate(WebDriver driver, String url) {
		
		driver.navigate().to(url);
		
		// phoenix
//		
//		driver.findElement(By.xpath("//*[@id=\"sign_in_form\"]/button")).click();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		driver.findElement(By.id("add_new_board")).click();
	}
	
	
	
	public static void navigate2(WebDriver driver, String url) {
		driver.navigate().to(url);

		// MRBS
		driver.findElement(By.xpath("//*[@id=\"logon_box\"]/form/div/input[3]")).click();;
		
		waitAfterClick(1000);
		
		driver.findElement(By.id("NewUserName")).sendKeys("administrator");
		driver.findElement(By.id("NewUserPassword")).sendKeys("secret");
		driver.findElement(By.xpath("//*[@id=\"logon_submit\"]/input")).click();

		waitAfterClick(1000);
	}
	
	
	public static StateVertex getState(int id , String url, FragmentManager manager, EmbeddedBrowser browser) throws InterruptedException {
		HybridStateVertexFactory factory = new HybridStateVertexFactory(0, CrawljaxConfiguration.builderFor(url), false);

		String domString = browser.getStrippedDom();
		StateVertex state = factory.newStateVertex(id, url, "test" + id, domString, domString, null);

		FragmentationPlugin.fragmentState(state, manager, browser, new File("testOutput"), false);
		
		return state;
	}
	

	
	public static void navigate_mantis(WebDriver driver, String url) {
		
		
		driver.findElement(By.xpath("/html/body/table[2]/tbody/tr/td[1]/a[8]")).click();
	}



	private static void login_mantis(WebDriver driver) {
		driver.findElement(By.name("username")).sendKeys("administrator");
		driver.findElement(By.name("password")).sendKeys("root");
		driver.findElement(By.xpath("/html/body/div[3]/form/table/tbody/tr[6]/td/input")).click();
		waitAfterClick(1000);
	}
	
	public static void mantisComp(String url, EmbeddedBrowser browser, FragmentManager manager) throws InterruptedException {
		
		url = "http://localhost:3000/mantisbt";

		WebDriver driver = browser.getWebDriver();
		
		driver.navigate().to(url);

		login_mantis(driver);

		
		
		StateVertex state1 = getState(0, url, manager, browser);
		
		String url2 = "http://localhost:3000/mantisbt";

		navigate2(driver, url2);
		StateVertex state2 = getState(1, url2, manager, browser);

		StateComparision comp = manager.cacheStateComparision(state2, state1, true);
		
		StateComparision comp2 = manager.areND2(state2, state1);
		System.out.println("using different : " + comp + " : using mapping : " + comp2);
	}
	
	public static void compAddressbook(String url, EmbeddedBrowser browser, FragmentManager manager) throws InterruptedException {
		url = "http://amesbah-macpro.ece.ubc.ca:8888/addressbook/addressbook-mod/addressbook/index.php";
		WebDriver driver = browser.getWebDriver();
		driver.navigate().to(url);
		
		login_addressbook(driver);
		long start = System.currentTimeMillis();
//		for(int i=0; i<100; i++) {
		StateVertex state1 = getState(0, url, manager, browser);
		
		for(Fragment fragment: state1.getFragments()) {
			System.out.println(fragment.getId() + " : " + FragmentManager.usefulFragment(fragment));
		}
//		}
		long end = System.currentTimeMillis();
		System.out.println("Time taken : " + (end-start));
	}
	
	public static void login_addressbook(WebDriver driver) {
		// addressbook
		driver.findElement(By.name("user")).sendKeys("admin");
		driver.findElement(By.name("pass")).sendKeys("admin");
		driver.findElement(By.xpath("//*[@id=\"LoginForm\"]/input[3]")).click();
//		
	}



	public static void main(String args[]) throws InterruptedException {
		String url = "http://localhost:9966/petclinic/owners/2.html";
//		String url = "http://amesbah-macpro.ece.ubc.ca:8888/addressbook/addressbook-mod/addressbook/index.php";
//		String url = "http://amesbah-macpro.ece.ubc.ca:8888/claroline/claroline-1.11.10/claroline/index.php";
//		String url = "http://localhost:4000";
//		String url = "http://localhost:3000/mrbs/web";
//		String url = "https://www.google.com/search?ei=WraKXdSCIZLC-gTehakQ&q=web+testing&oq=web+testing&gs_l=psy-ab.3..0i7i30l8j0i67j0i7i30.1321.1489..1827...0.3..0.75.201.3......0....1..gws-wiz.......0i71.wSZPJ0j9Owc&ved=0ahUKEwjUl_yn3erkAhUSoZ4KHd5CCgIQ4dUDCAs&uact=5";
		
		CrawljaxConfigurationBuilder configBuilder = CrawljaxConfiguration.builderFor(url);
		BrowserConfiguration browserConfiguration = new BrowserConfiguration(BrowserType.CHROME, 1,
             //   new BrowserOptions(BrowserOptions.MACBOOK_PRO_RETINA_PIXEL_DENSITY));
				new BrowserOptions());
		configBuilder.setBrowserConfig(browserConfiguration);
		WebDriverBrowserBuilder builder = new WebDriverBrowserBuilder(configBuilder.build(), null);
		
		FragmentManager manager = new FragmentManager(null);

	
		EmbeddedBrowser browser = builder.get();
		
//		navigate(browser.getWebDriver(), url);
		compAddressbook(url, browser, manager);
		
//		getState(0, url, manager, browser);
		
//		comp(url, browser);
//		mantisComp(url, browser);
		
		browser.close();

		
//		
//		manager.cacheStateComparision(state2, state1);
	}

	private static void comp(String url, EmbeddedBrowser browser, FragmentManager manager) throws InterruptedException {
		WebDriver driver = browser.getWebDriver();
		
		navigate(driver, url);
				
		StateVertex state1 = getState(0, url, manager, browser);
		
		String url2 = "http://localhost:3000/mrbs/web";

		
//		String url2 = "http://localhost:9966/petclinic/owners/10.html";
//		String url2 = "http://amesbah-macpro.ece.ubc.ca:8888/addressbook/addressbook-mod/addressbook/group.php";
//		String url2 = "http://amesbah-macpro.ece.ubc.ca:8888/addressbook/addressbook-mod/addressbook/index.php";
//
//		
		navigate2(driver, url2);
		StateVertex state2 = getState(1, url2, manager, browser);
//
		StateComparision comp = manager.cacheStateComparision(state2, state1, true);
		
		StateComparision comp2 = manager.areND2(state2, state1);
		System.out.println("using different : " + comp + " : using mapping : " + comp2);
	}



	public static void phoenix_login(WebDriver driver) {
		driver.findElement(By.xpath("//*[@id=\"sign_in_form\"]/button")).click();
		waitAfterClick(1000);
		
//		driver.findElement(By.id("add_new_board")).click();
//		waitAfterClick(1000);
	
//		driver.findElement(By.xpath("//*[@id=\"board_name\"]")).sendKeys("new board");
//		driver.findElement(By.xpath("//*[@id=\"new_board_form\"]/button")).click();
	}



	public static void phoenix_opencard(WebDriver driver) {
		driver.findElement(By.xpath("/html/body/main/div/div/div/div/section/div/div[1]/div")).click();
		waitAfterClick(1000);
		
		driver.findElement(By.xpath("/html/body/main/div/div/div/div/div/div/div/div[1]/div/div/div")).click();
		waitAfterClick(1000);
	}



	public static void phoenix_addComment(WebDriver driver) {
		driver.findElement(By.xpath("//*[@id=\"authentication_container\"]/div/div/div[2]/div/div/div[1]/div/form/div[2]/textarea")).sendKeys("comment1");
		driver.findElement(By.xpath("/html/body/main/div/div/div/div/div[2]/div/div/div[1]/div/form/div[2]/button")).click();
		
		
		waitAfterClick(1000);
	}



	private static void waitAfterClick(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

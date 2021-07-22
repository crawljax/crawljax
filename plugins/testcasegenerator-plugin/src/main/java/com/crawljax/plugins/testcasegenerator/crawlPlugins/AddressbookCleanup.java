package com.crawljax.plugins.testcasegenerator.crawlPlugins;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnUrlFirstLoadPlugin;

public class AddressbookCleanup implements OnUrlFirstLoadPlugin {
	
	

	@Override
	public void onUrlFirstLoad(CrawlerContext context) {
		WebDriver driver = context.getBrowser().getWebDriver();
		String url = driver.getCurrentUrl();
		// Clean addressbook case : selenium test case 
		
		// login
		driver.findElement(By.name("user")).sendKeys("admin");
		driver.findElement(By.name("pass")).sendKeys("admin");
		driver.findElement(By.xpath("//*[@id=\"LoginForm\"]/input[3]")).click();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//clear addresses
		driver.findElement(By.id("MassCB")).click();
		driver.findElement((By.xpath("//*[@id=\"content\"]/form[2]/div[2]/input"))).click();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.switchTo().alert().accept();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.findElement(By.xpath("//*[@id=\"nav\"]/ul/li[1]/a"));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//clear groups
		driver.findElement((By.xpath("//*[@id=\"nav\"]/ul/li[3]/a"))).click();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<WebElement> groups = driver.findElements(By.xpath("//*[@id=\"content\"]/form/input"));
		
		for(WebElement group: groups) {
			if(!group.getAttribute("type").equalsIgnoreCase("checkbox"))
				continue;
			group.click();
		}
		
		driver.findElement(By.name("delete")).click();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//logout
		driver.findElement(By.xpath("//*[@id=\"top\"]/form/a")).click();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// reload the url 
		driver.get(url);
		
		
	}

}

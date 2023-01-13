package com.crawljax.stateabstractions.hybrid;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;

public class UtilsSelenium {
    public static Wait<WebDriver> getWebDriverFluentWait(WebDriver driver, int timeout) {
        return new FluentWait<WebDriver>(driver)
                .withTimeout(Duration.ofMillis(timeout))
                .pollingEvery(Duration.ofMillis(100))
                .ignoring(NoSuchElementException.class);
    }

    public static WebElement elementToBeClickable(WebDriver driver, WebElement webElement, int timeout) {
        try {
            return getWebDriverFluentWait(driver, timeout)
                    .until(ExpectedConditions.elementToBeClickable(webElement));
        } catch (Exception e) {
            return null;
        }
    }

    public static WebElement elementTobeVisible(WebDriver driver, WebElement webElement, int timeout) {
        try {
            return getWebDriverFluentWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOf(webElement));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void click(WebDriver driver, WebElement webElement){
        if(elementTobeVisible(driver, webElement, 500)!=null){
            System.out.println("Error waiting for visibility");
        }
        webElement.click();
    }

    public static void sendKeys(WebDriver driver, WebElement webElement, String toSend){
        if(elementTobeVisible(driver, webElement, 500)!=null){
            System.out.println("Error waiting for visibility");
        }
        webElement.sendKeys(toSend);
    }

    public static void selectByValue(WebDriver driver, WebElement webElement, String toSend){
        if(elementTobeVisible(driver, webElement, 500)!=null){
            System.out.println("Error waiting for visibility");
        }
        new Select(webElement).selectByValue(toSend);
    }

    public static void selectByVisibleText(WebDriver driver, WebElement webElement, String toSend){
        if(elementTobeVisible(driver, webElement, 500)!=null){
            System.out.println("Error waiting for visibility");
        }
        new Select(webElement).selectByVisibleText(toSend);
    }

}

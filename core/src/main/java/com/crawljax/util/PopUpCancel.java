package com.crawljax.util;

import java.io.IOException;

import org.openqa.selenium.WebDriver;

/*Utility class to cancel pop up windows. By default all pop ups are cancelled. The user can call
 * setPopUpCancelMode() to decide what pop ups too close
 * ALL 
 * AUTH for authentication windows
 * DOWNLOAD
 * NONE 
 */

public final class PopUpCancel{
	
	private static String mode = "ALL";
	private final static String fileName = "closePopUps.exe";
	private final static String exePath = fileName;

	public static void setMode(String newMode) {

		switch(mode)
		{
			case("ALL"):
			case("AUTH"):
			case("DOWNLOAD"):
			case("NONE"):
				mode = newMode;
				break;	
			default: 
				System.err.println("Given invalid mod to PopUpCanceller. Default mode of ALL selected");
				break;
		}
	}
	
	
	public static void ClosePopUps(WebDriver browser, String currentWindow) {
		if(!mode.equals("NONE")) {
			String commands[] = new String[]{exePath, getPopUpType() };

			try{
				Runtime.getRuntime().exec(commands);
			} catch (IOException e) {
				System.err.println("Error closePopUps.exe not found");
			}
			browser.switchTo().window(currentWindow);
		}
	}
	
	private static String getPopUpType() {

		String type = "";
		switch(mode)
		{
			case("ALL"):
				type = "";
				break;
			case("AUTH"):
				type = "Authentication Required";
				break;
			case("DOWNLOAD"):
				type = "Opening";
				break;		
		}
		
		return type;
	}

}
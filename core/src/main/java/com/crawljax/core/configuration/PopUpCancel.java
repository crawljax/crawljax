package com.crawljax.core.configuration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebDriver;

/*Class to cancel pop up windows. By default all pop up cancels ALL. The user can call
 * setPopUpCancelMode() to decide what pop ups to close
 * ALL 
 * AUTH for authentication windows
 * DOWNLOAD
 * NONE 
 * 
 * closePopUps.exe is located in \src\main\resources
 * It will search for any mozilla dialog boxes with specific titles and close them, checking every period
 */

public class PopUpCancel{

	private final static String CLOSE_ALL = "ALL";
	private final static String CLOSE_NONE = "NONE";
	private final static String CLOSE_AUTHENTICATION = "AUTHENTICATION";
	private final static String CLOSE_DOWNLOAD = "DOWNLOAD";
	private static int timerPeriod = 500;
	private static String fileName = "/closePopUps.exe";
	private static File temporaryExe = null;
	private static String mode = CLOSE_ALL;
	private static String exePath = null;
	private static Process process = null;
	private static int deleteProcessTimeOut = 500;
	
	//unique identifier for firefox dialog boxes
	private final static String fireFoxDialogID = "ahk_class MozillaDialogClass"; 

	/** Create temporary file to store exe file into using streams
	 * @return
	 */
	private static String getPopUpCancelExe()
	{	
		String path = null;
		InputStream input = null;
		OutputStream output = null;

		try{
			input = PopUpCancel.class.getResourceAsStream(fileName);
			temporaryExe = File.createTempFile(PopUpCancel.class.getName(), "");
			output = new FileOutputStream(temporaryExe);
			output = new BufferedOutputStream(output);
			IOUtils.copy(input, output);

		}
		catch(Exception ex){}		
		finally{ //in case copy fails
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			path = temporaryExe.getAbsolutePath();
		}

		return path;
	}

	/** User selects what to cancel, unless file was not found
	 * @param newMode
	 */
	public static void setMode(String newMode) {
		switch(mode)
		{
		case(CLOSE_ALL):
		case(CLOSE_AUTHENTICATION):
		case(CLOSE_DOWNLOAD):
		case(CLOSE_NONE):
			mode = newMode; 
			break;	
		default: 
			System.err.println("Given invalid mode to PopUpCanceller. Mode of NONE selected");
			mode = CLOSE_NONE;
			break;
		} 
	}

	/** Set the timer delay to close pop ups
	 * @param delay
	 */
	public static void setTimer(int delay)
	{
		timerPeriod = delay;
	}

	/** Actual closing of the pop-ups performed here if mode set
	 */
	public static void ClosePopUps() {

		if(!mode.equals(CLOSE_NONE)) {

			try{
				//if file hasn't been made before or has been deleted
				if(exePath == null) 
					exePath = getPopUpCancelExe();
				String commands[] = new String[]{exePath, getPopUpTitle(), String.valueOf(timerPeriod) };
				process = Runtime.getRuntime().exec(commands);

			} catch (Exception ex) {
				System.err.println("Error closePopUps.exe not found. PopUpCancel disabled");
				mode = CLOSE_NONE;
			}
		}

	}

	/** Returns the distinct window title for the pop up window type specified by mode
	 * @return
	 */
	private static String getPopUpTitle() {

		
		String type = fireFoxDialogID;
		
		switch(mode)
		{
		case(CLOSE_ALL):
			break;
		case(CLOSE_AUTHENTICATION):
			type = "Authentication Required" + " " + fireFoxDialogID;
			break;
		case(CLOSE_DOWNLOAD):
			type = "Opening" + " " + fireFoxDialogID;
			break;		
		}
		return type;
	}

	/**
	 *  Kill process and remove the temporary file
	 */
	public static void deleteTemp(){

		try{
			if(process != null)
			{
				process.destroy();
				process.waitFor();
				if(!temporaryExe.delete()){
					System.err.println("PopUpCanceler clean up failed (temp file was not deleted");
				}
			}
			exePath = null;	
		}catch(NullPointerException ex){
			//Means user didn't want to cancel pop ups
		}
		catch(InterruptedException ex){
			System.err.println("deleting process was interrupted. " +
					"File may not have been deleted properly or ended");
		}
	}
	
	/** How long we should wait before we try to kill the exe (default 500 ms)
	 * @param timerVal
	 */
	public static void setKillProcessTimeOut(int timerVal)
	{
		deleteProcessTimeOut = timerVal;
	}

	public static String getFilePath()
	{
		return exePath;
	}

}
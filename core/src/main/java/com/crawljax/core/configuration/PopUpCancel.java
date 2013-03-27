package com.crawljax.core.configuration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;

/*
 * Runs AutoHotKey compiled closePopUps.exe located in class path to cancel specified pop ups
 */

public class PopUpCancel {

	// unique identifier for firefox dialog boxes
	private final static String fireFoxDialogID = "ahk_class MozillaDialogClass";
	private final static String ChromeDialogID = "ahk_class #32770";

	private final static String CLOSE_ALL = "ALL";
	private final static String CLOSE_NONE = "NONE";
	private final static String CLOSE_AUTHENTICATION = "AUTHENTICATION";
	private final static String CLOSE_DOWNLOAD = "DOWNLOAD";

	private static BrowserType browserType = BrowserType.firefox;
	private static Process process = null;
	private static File temporaryExe = null;
	private static int killProcessTimeOut = 500;
	private static int timerPeriod = 500;
	private static String fileName = "/closePopUps.exe";
	private static String mode = CLOSE_ALL;
	private static String exePath = null;

	// PopUpCancel.class.getProtectionDomain().getCodeSource().getLocation().getPath() + fileName;

	/**
	 * Create temporary file to store exe file into using streams
	 * 
	 * @return string path of temporary file
	 */
	private static String getPopUpCancelExe()
	{
		String path = null;
		InputStream input = null;
		OutputStream output = null;

		try {
			input = PopUpCancel.class.getResourceAsStream(fileName);
			temporaryExe = File.createTempFile(PopUpCancel.class.getName(), "");
			output = new FileOutputStream(temporaryExe);
			output = new BufferedOutputStream(output);
			IOUtils.copy(input, output);

		} catch (Exception ex) {
		} finally { // in case copy fails
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			path = temporaryExe.getAbsolutePath();
		}

		return path;
	}

	/**
	 * User selects what to cancel, unless file was not found
	 * 
	 * @param newMode
	 */
	public static void setMode(String newMode) {

		switch (mode)
		{
			case (CLOSE_ALL):
			case (CLOSE_AUTHENTICATION):
			case (CLOSE_DOWNLOAD):
			case (CLOSE_NONE):
				mode = newMode;
				break;
			default:
				System.err.println("Given invalid mode to PopUpCanceller. Mode of NONE selected");
				mode = CLOSE_NONE;
				break;
		}
	}

	/**
	 * Set the timer delay to close pop ups
	 * 
	 * @param delay
	 */
	public static void setTimer(int delay) {
		timerPeriod = delay;
	}

	/**
	 * Run closePopUps.exe to close pop ups
	 */
	public static void ClosePopUps() {

		if (!mode.equals(CLOSE_NONE)) {

			try {

				if (exePath == null)
					exePath = getPopUpCancelExe();

				// The window class ID and title name is passed as parameters to the exe
				String commands[] =
				        new String[] { exePath, getPopUpTitle(), String.valueOf(timerPeriod) };
				process = Runtime.getRuntime().exec(commands);

			} catch (Exception ex) {
				System.err.println("Error" + fileName + " not found. PopUpCancel disabled");
				mode = CLOSE_NONE;
			}
		}

	}

	public static void setBrowserType(BrowserType ibrowserType) {
		browserType = ibrowserType;
	}

	/**
	 * Returns the distinct window title for the pop up window type specified by mode
	 * 
	 * @return
	 */
	private static String getPopUpTitle() {

		String windowID = fireFoxDialogID;
		;
		String windowName = "Opening";

		switch (browserType) {
			case chrome:
				windowID = ChromeDialogID;
				windowName = "Save As";
				break;
			case firefox:
				windowID = fireFoxDialogID;
				windowName = "Opening";
				break;
		}

		switch (mode)
		{
			case (CLOSE_ALL):
				break;
			case (CLOSE_AUTHENTICATION):
				windowID = "Authentication Required" + " " + windowID;
				break;
			case (CLOSE_DOWNLOAD):
				windowID = windowName + " " + windowID;
				break;
		}
		return windowID;
	}

	/**
	 * How long we should wait before we try to kill the exe (default 500 ms)
	 * 
	 * @param timerVal
	 */
	public static void setKillProcessTimeOut(int timerVal)
	{
		killProcessTimeOut = timerVal;
	}

	/**
	 * Get path of exe
	 * 
	 * @return
	 */
	public static String getFilePath()
	{
		return exePath;
	}

	public static void killExe() {

		try {
			if (process != null)
			{
				process.destroy();
				Thread thread = (new Thread(new ProcessCleanUp(process)));
				thread.start();
				thread.join(killProcessTimeOut);

				if (!temporaryExe.delete()) {
					System.err
					        .println("PopUpCanceler clean up failed (temp file was not deleted");
				}
			}
			exePath = null;
		} catch (NullPointerException ex) {
			// Means user didn't want to cancel pop ups
		} catch (InterruptedException ex) {
			System.err.println("deleting process was interrupted. " +
			        "File may not have been deleted properly or ended");
		}
	}

	private static class ProcessCleanUp implements Runnable
	{
		Process process;

		public ProcessCleanUp(Process process)
		{
			this.process = process;
		}

		@Override
		public void run() {
			try {
				this.process.waitFor();
			} catch (InterruptedException e) {
				System.err.println(fileName + " did not gracefully exit");
			}
		}

	}

	public static String getMode() {
		return mode;
	}

}
package com.crawljax.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public final class FSUtils {

	/**
	 * Checks the existence of the directory. If it does not exist, the method creates it.
	 *
	 * @param dir the directory to check.
	 * @throws IOException if fails.
	 */
	public static void directoryCheck(String dir) throws IOException {
		final File file = new File(dir);

		if (!file.exists()) {
			FileUtils.forceMkdir(file);
		}
	}

	/**
	 * Checks whether the folder exists for fileName, and creates it if necessary.
	 *
	 * @param fileName folder name.
	 * @throws IOException an IO exception.
	 */
	public static void checkFolderForFile(String fileName) throws IOException {

		if (fileName.lastIndexOf(File.separator) > 0) {
			String folder = fileName.substring(0, fileName.lastIndexOf(File.separator));
			directoryCheck(folder);
		}
	}
}

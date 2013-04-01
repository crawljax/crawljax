package com.crawljax.core.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class CreateJarFile {
	public static <T> void createJar(File outputFile, Class<T> inputClass, boolean isValidPlugin) {
		try {
			URL classUrl = inputClass.getResource(inputClass.getSimpleName() + ".class");
			if (classUrl == null)
				return;

			File inputFile = new File(classUrl.getPath());
			byte buffer[] = new byte[1024];

			FileOutputStream stream = new FileOutputStream(outputFile);
			JarOutputStream out = new JarOutputStream(stream, new Manifest());
			if (inputFile == null || !inputFile.exists() || inputFile.isDirectory())
			{
				out.close();
				throw new IOException("Input file not valid.");
			}
			else {
				System.out.println("Creating JAR from " + inputClass.getName());
				int folderStructureLength =
				        inputClass.getName().length() - inputClass.getSimpleName().length();
				String stuff = inputClass.getName().substring(0, folderStructureLength);
				String folderName = stuff.replace(".", "/");
				JarEntry jarAddF = new JarEntry(folderName + inputFile.getName());
				jarAddF.setTime(inputFile.lastModified());
				out.putNextEntry(jarAddF);

				// Write file to archive
				FileInputStream in = new FileInputStream(inputFile);
				while (true) {
					int nRead = in.read(buffer, 0, buffer.length);
					if (nRead <= 0)
						break;
					out.write(buffer, 0, nRead);
				}
				in.close();
			}
			out.close();
			stream.close();
			System.out.println("Adding completed OK");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error: " + ex.getMessage());
		}
	}
}
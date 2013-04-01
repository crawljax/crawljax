package com.crawljax.core.plugin;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DynamicImportTest {
	static private final String TEST_FOLDER_ADDRESS = "../TestFolder";
	static private final String TEST_SUBFOLDER_ADDRESS = "../TestFolder/TestSubFolder";

	@AfterClass
	public static void CleanUpTests()
	{
		removeDirectory(new File(TEST_FOLDER_ADDRESS));
	}

	// as a developer, I want every folder to give me 0...n plugins, so my code doesn't have to
	// handle a null edge case
	@Test
	public void ImportFromExistingFolderTest()
	{
		File tempDir = SetupTestFolderStructure(new File(TEST_FOLDER_ADDRESS));
		boolean FolderCreated = tempDir.exists();
		assertEquals(true, FolderCreated);
		List<Plugin> classPathPlugins = PluginImporter.getPluggedServices(Plugin.class, tempDir);
		assertEquals(0, classPathPlugins.size());
	}

	// as a crawljax user, I want my plugin to be picked up from a folder, so that I can pickup
	// plugins from somewhere other than the drive root
	@Test
	public void ImportFromNonEmptyFolderTest()
	{
		File tempDir = SetupTestFolderStructure(new File(TEST_FOLDER_ADDRESS));
		// File tempDir = new File(TEST_FOLDER_ADDRESS);
		boolean FolderCreated = tempDir.exists();
		assertEquals(true, FolderCreated);
		CreateJarFile.createJar(new File(tempDir.getPath() + "/testjar.jar"),
		        SamplePreCrawlingPlugin.class, true);
		assertEquals(1, tempDir.list().length);
		List<Plugin> classPathPlugins = PluginImporter.getPluggedServices(Plugin.class, tempDir);
		assertEquals(1, classPathPlugins.size());

	}

	// as a crawljax user, I want to not have non plugin code included, so crawljax doesn't have to
	// sort through irrelevant code
	@Test
	public void ImportUselessJarsFromFolderTest()
	{
		File tempDir = SetupTestFolderStructure(new File(TEST_FOLDER_ADDRESS));
		boolean FolderExists = tempDir.exists();
		assertEquals(true, FolderExists);
		CreateJarFile.createJar(new File(tempDir.getPath() + "/testjar.jar"),
		        SampleNonPluginClass.class, false);
		assertEquals(1, tempDir.listFiles().length);
		List<Plugin> classPathPlugins = PluginImporter.getPluggedServices(Plugin.class, tempDir);
		assertEquals(0, classPathPlugins.size());

	}

	// as a developer, I want the system to abort if it doesn't find a folder, so that the system
	// doesn't crash
	@Test
	public void ImportFromNonExistentFolder()
	{
		File tempDir = new File(TEST_FOLDER_ADDRESS);
		removeDirectory(tempDir);
		boolean FolderExists = tempDir.exists();

		assertEquals(false, FolderExists);
		List<Plugin> classPathPlugins = PluginImporter.getPluggedServices(Plugin.class, tempDir);
		assertEquals(0, classPathPlugins.size());
	}

	// as a user, I want the system to grab plugins from all subdirectories, so that no special
	// folder structure is necessary
	@Test
	public void ImportFromSubdirectories()
	{
		File tempDir = SetupTestFolderStructure(new File(TEST_FOLDER_ADDRESS));
		File tempSubDir = SetupTestFolderStructure(new File(TEST_SUBFOLDER_ADDRESS));
		tempSubDir.mkdir();

		boolean FolderExists = tempDir.exists() | tempSubDir.exists();
		assertEquals(true, FolderExists);

		CreateJarFile.createJar(new File(tempSubDir.getPath() + "/testjar2.jar"),
		        SamplePreCrawlingPlugin.class, true);

		List<Plugin> classPathPlugins = PluginImporter.getPluggedServices(Plugin.class, tempDir);

		assertEquals(1, classPathPlugins.size());
	}

	// as a user, if a plugin is present in both folder and jar form, only one should be included
	@Test
	public void ExcludeDuplicatePlugins()
	{
		File tempDir = SetupTestFolderStructure(new File(TEST_FOLDER_ADDRESS));
		boolean FolderExists = tempDir.exists();
		assertEquals(true, FolderExists);
		CreateJarFile.createJar(new File(tempDir.getPath() + "/testjar.jar"),
		        SamplePreCrawlingPlugin.class, false);
		CreateJarFile.createJar(new File(tempDir.getPath() + "/testjar2.jar"),
		        SamplePreCrawlingPlugin.class, false);
		assertEquals(2, tempDir.listFiles().length);
		List<Plugin> classPathPlugins = PluginImporter.getPluggedServices(Plugin.class, tempDir);
		assertEquals(1, classPathPlugins.size());
	}

	static File SetupTestFolderStructure(File pDir)
	{
		// clean the test folder structure completely so tests are independent
		removeDirectory(pDir);
		File tempDir = new File(pDir.getPath());
		tempDir.mkdir();

		return tempDir;
	}

	static void removeDirectory(final File folder) {
		if (folder.isDirectory()) {
			File[] list = folder.listFiles();
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					File tempFile = list[i];
					if (tempFile.isDirectory())
						removeDirectory(tempFile);
					tempFile.delete();
				}
			}
			folder.delete();
		}
	}

}

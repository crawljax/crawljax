package com.crawljax.core.plugin;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DynamicImportTest {
	static private final String TEST_FOLDER_ADDRESS = "../TestFolder";
	static private final String TEST_SUBFOLDER_ADDRESS = "../TestFolder/TestSubFolder";
	@AfterClass public static void CleanUpTests()
	{
		removeDirectory(new File(TEST_FOLDER_ADDRESS));
	}
	//as a developer, I want every folder to give me 0...n plugins, so my code doesn't have to handle a null edge case
	@Test
	public void ImportFromExistingFolderTest()
	{
		File tempDir = SetupTestFolderStructure(new File(TEST_FOLDER_ADDRESS));
		boolean FolderCreated = tempDir.exists();
		assertEquals(true, FolderCreated);
		PluginImporter lImporter = new PluginImporter(ClassLoaderHelper.buildClassLoader(true, tempDir));
		List<Plugin> classPathPlugins = lImporter.getPluggedServices(Plugin.class);
		assertEquals(0,classPathPlugins.size());
	}
	//as a crawljax user, I want to not have non plugin code included, so crawljax doesn't have to sort through irrelevant code
	@Test
	public void ImportUselessJarsFromFolderTest()
	{
		File tempDir = SetupTestFolderStructure(new File(TEST_FOLDER_ADDRESS));
		boolean FolderExists = tempDir.exists();
		assertEquals(true, FolderExists);
		//create 1 blank jar in that folder
		assertEquals(1, tempDir.listFiles().length);
		//attempt to withdraw plugins from that folder
		//assert that zero plugins were returned
		
	}
		
	//as a developer, I want the system to abort if it doesn't find a folder, so that the system doesn't crash
	@Test (expected=NullPointerException.class)
	public void ImportFromNonExistentFolder()
	{
		File tempDir = new File(TEST_FOLDER_ADDRESS);
		removeDirectory(tempDir);
		boolean FolderExists = tempDir.exists();
		//assert that the folder is not real
		assertEquals(false, FolderExists);
		
		PluginImporter lImporter = new PluginImporter(ClassLoaderHelper.buildClassLoader(true, tempDir));
		List<Plugin> classPathPlugins = lImporter.getPluggedServices(Plugin.class);
		
		//assert that the warning came up that no plugins were found in that folder ?? how do you do this
		
		//assert that zero plugins were returned
		assertEquals(0, classPathPlugins.size());
	}
	
	//as a user, I want the system to grab plugins from all subdirectories, so that no special folder structure is necessary
	@Test 
	public void ImportFromSubdirectories()
	{
		File tempDir = SetupTestFolderStructure(new File(TEST_FOLDER_ADDRESS));
		File tempSubDir = SetupTestFolderStructure(new File(TEST_SUBFOLDER_ADDRESS));
		tempSubDir.mkdir();
		
		boolean FolderExists = tempDir.exists() | tempSubDir.exists();
		assertEquals(true, FolderExists);
		
		//make 2 jars here, one in testfolder, the other in testsubfolder
		//each jar should have a manifest with the right info, but we don't need a class?
		//how could I create these test plugins dynamically?
		
		PluginImporter lImporter = new PluginImporter(ClassLoaderHelper.buildClassLoader(true, tempDir));
		List<Plugin> classPathPlugins = lImporter.getPluggedServices(Plugin.class);
		
		assertEquals(2, classPathPlugins.size());
	}
	//as a user, if a plugin is present in both folder and jar form, only one should be included
	@Test
	public void ExcludeDuplicatePlugins()
	{
		//make it fail for now, not sure how to do this (or if we need to do it, will ask the Prof
		//to see if this is a relevant user story)
		assertEquals(1,0);
	}
	
	static File SetupTestFolderStructure(File pDir)
	{
		//clean the test folder structure completely so tests are independent
		removeDirectory(pDir);
		File tempDir = new File(TEST_FOLDER_ADDRESS);
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

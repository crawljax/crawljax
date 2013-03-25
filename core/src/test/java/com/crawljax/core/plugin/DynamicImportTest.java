package com.crawljax.core.plugin;

import java.io.File;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class DynamicImportTest {
	
	//as a developer, I want every folder to give me 0...n plugins, so my code doesn't have to handle a null edge case
	@Test
	public void ImportFromExistingFolderTest()
	{
		File tempDir = new File("../TestFolder");
		tempDir.mkdir();
		boolean FolderCreated = tempDir.exists();
		assertEquals(true, FolderCreated);
		PluginImporter lImporter = new PluginImporter(ClassLoaderHelper.buildClassLoader(true, tempDir));
		List<Plugin> classPathPlugins = lImporter.getPluggedServices(Plugin.class);
		assertEquals(0,classPathPlugins.size());
		tempDir.delete();
	}
	//as a crawljax user, I want to not have non plugin code included, so crawljax doesn't have to sort through irrelevant code
	@Test
	public void ImportUselessJarsFromFolderTest()
	{
		File tempDir = new File("../TestFolder");
		tempDir.mkdir();
		boolean FolderCreated = tempDir.exists();
		//create 1 blank jar in that folder
		assertEquals(1, tempDir.listFiles().length);
		//attempt to withdraw plugins from that folder
		//assert that zero plugins were returned
		//delete jars
		//delete folder
		
		tempDir.delete();
		
	}
	//as a developer, I want the system to abort if it doesn't find a folder, so that the system doesn't crash
	@Test //(expected=NullPointerException.class)
	public void ImportFromNonExistentFolder()
	{
		File tempDir = new File("../TestFolder");
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
		File tempDir = new File("../TestFolder");
		tempDir.mkdir();
		File tempSubDir = new File("../TestFolder/TestSubFolder");
		tempSubDir.mkdir();
		
		boolean FolderExists = tempDir.exists() | tempSubDir.exists();
		assertEquals(true, FolderExists);
		
		//make 2 jars here, one in testfolder, the other in testsubfolder
		//each jar should have a manifest with the right info, but we don't need a class?
		//how could I create these test plugins dynamically?
		
		PluginImporter lImporter = new PluginImporter(ClassLoaderHelper.buildClassLoader(true, tempDir));
		List<Plugin> classPathPlugins = lImporter.getPluggedServices(Plugin.class);
		
		assertEquals(2, classPathPlugins.size());
		tempSubDir.delete();
		tempDir.delete();
	}
	//as a user, if a plugin is present in both folder and jar form, only one should be included
	@Test
	public void ExcludeDuplicatePlugins()
	{
		//make it fail for now, not sure how to do this (or if we need to do it, will ask the Prof
		//to see if this is a relevant user story)
		assertEquals(1,0);
	}
	

}

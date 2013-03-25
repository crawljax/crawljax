package com.crawljax.core.plugin;

import org.junit.Test;

public class DynamicImportTest {
	
	//as a developer, I want every folder to give me 0...n plugins, so my code doesn't have to handle a null edge case
	@Test
	void ImportFromExistingFolderTest()
	{
		//createfolder
		//assert that folder exists
		//attempt to withdraw plugins from that folder
		//assert that zero plugins were returned
		//clean up folder
	}
	//as a crawljax user, I want to not have non plugin code included, so crawljax doesn't have to sort through irrelevant code
	@Test
	void ImportUselessJarsFromFolderTest()
	{
		//createfolder
		//create blank jars in that folder
		//attempt to withdraw plugins from that folder
		//assert that zero plugins were returned
		//delete jars
		//delete folder
		
	}
	//as a developer, I want the system to abort if it doesn't find a folder, so that the system doesn't crash
	@Test
	void ImportFromNonExistentFolder()
	{
		//make random folder path
		//assert that the folder is not real
		//attempt to withdraw plugins from that folder
		//assert that the warning came up that no plugins were found in that folder
		//assert that zero plugins were returned
	}
	
	
	

}

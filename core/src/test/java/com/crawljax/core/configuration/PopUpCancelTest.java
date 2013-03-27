package com.crawljax.core.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PopUpCancelTest {

	@Test
	public void testSetMode() {

		PopUpCancel.setMode("DOWNLOAD");
		assertEquals(PopUpCancel.getMode(), "DOWNLOAD");
		PopUpCancel.setMode("ALL");
		assertEquals(PopUpCancel.getMode(), "ALL");

	}

	@Test
	public void testClosePopUps() {

		PopUpCancel.setMode("DOWNLOAD");
		assertTrue(PopUpCancel.getFilePath() == null);
		PopUpCancel.ClosePopUps();

		// If ClosePopUps runs successfully, the exePath is assigned a path
		assertTrue(PopUpCancel.getFilePath() != null);

	}

	@Test
	public void testKillExe() {

		PopUpCancel.setMode("DOWNLOAD");
		PopUpCancel.ClosePopUps();
		assertTrue(PopUpCancel.getFilePath() != null);
		PopUpCancel.killExe();

		// If killExe runs successfully, the exePath became null
		assertTrue(PopUpCancel.getFilePath() == null);

	}

}
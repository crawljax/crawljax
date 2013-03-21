package com.crawljax.core;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.crawljax.forms.FormInput;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableList;

/**
 * Test for the CandidateElementManager.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 */
public class CandidateElementManagerTest {
	private static Document document;

	private final List<FormInput> noFormInput = ImmutableList.of();

	@BeforeClass
	public static void setup() throws SAXException, IOException {
		document = DomUtils.asDocument("");
	}

	@Test
	public void testContainsElement() {
		CandidateElementManager manager = new CandidateElementManager(null, null);
		Element e = document.createElement("test");

		e.setAttribute("id", "abc");
		CandidateElement c = new CandidateElement(e, "", noFormInput);
		Assert.assertFalse(
		        "CandidateElemnt.GeneralString not yet checked in CandidateElementManager",
		        manager.isChecked(c.getGeneralString()));
		Assert.assertFalse(
		        "CandidateElemnt.UniqueString not yet checked in CandidateElementManager",
		        manager.isChecked(c.getUniqueString()));
		Assert.assertTrue("CandidateElement correctly added", manager.markChecked(c));
		Assert.assertTrue("CandidateElemnt.GeneralString checked in CandidateElementManager",
		        manager.isChecked(c.getGeneralString()));
		Assert.assertTrue("CandidateElemnt.UniqueString checked in CandidateElementManager",
		        manager.isChecked(c.getUniqueString()));

		e.setAttribute("id", "def");
		CandidateElement c2 = new CandidateElement(e, "", noFormInput);
		Assert.assertFalse(
		        "CandidateElemnt.GeneralString not yet checked in CandidateElementManager",
		        manager.isChecked(c2.getGeneralString()));
		Assert.assertFalse(
		        "CandidateElemnt.UniqueString not yet checked in CandidateElementManager",
		        manager.isChecked(c2.getUniqueString()));
		Assert.assertTrue("CandidateElement correctly added", manager.markChecked(c2));
		Assert.assertTrue("CandidateElemnt.GeneralString checked in CandidateElementManager",
		        manager.isChecked(c2.getGeneralString()));
		Assert.assertTrue("CandidateElemnt.UniqueString checked in CandidateElementManager",
		        manager.isChecked(c2.getUniqueString()));

		Assert.assertFalse("CandidateElement allready added", manager.markChecked(c2));
		Assert.assertTrue("CandidateElemnt.GeneralString checked in CandidateElementManager",
		        manager.isChecked(c2.getGeneralString()));
		Assert.assertTrue("CandidateElemnt.UniqueString checked in CandidateElementManager",
		        manager.isChecked(c2.getUniqueString()));

	}

	@Test
	public void testContainsElementAtusa() {
		CandidateElementManager manager = new CandidateElementManager(null, null);
		Element e = document.createElement("test");
		e.setAttribute("id", "abc");
		e.setAttribute("atusa", "def");

		CandidateElement c = new CandidateElement(e, "", noFormInput);
		Assert.assertFalse(
		        "CandidateElemnt.GeneralString not yet checked in CandidateElementManager",
		        manager.isChecked(c.getGeneralString()));
		Assert.assertFalse(
		        "CandidateElemnt.UniqueString not yet checked in CandidateElementManager",
		        manager.isChecked(c.getUniqueString()));
		Assert.assertTrue("CandidateElement correctly added", manager.markChecked(c));
		Assert.assertTrue("CandidateElemnt.GeneralString checked in CandidateElementManager",
		        manager.isChecked(c.getGeneralString()));
		Assert.assertTrue("CandidateElemnt.UniqueString checked in CandidateElementManager",
		        manager.isChecked(c.getUniqueString()));

		e.setAttribute("atusa", "ghi");
		CandidateElement c2 = new CandidateElement(e, "", noFormInput);
		Assert.assertTrue("CandidateElemnt.GeneralString checked in CandidateElementManager",
		        manager.isChecked(c2.getGeneralString()));
		Assert.assertFalse(
		        "CandidateElemnt.UniqueString not yet checked in CandidateElementManager",
		        manager.isChecked(c2.getUniqueString()));
		Assert.assertTrue("CandidateElement correctly added", manager.markChecked(c2));
		Assert.assertTrue("CandidateElemnt.GeneralString checked in CandidateElementManager",
		        manager.isChecked(c2.getGeneralString()));
		Assert.assertTrue("CandidateElemnt.UniqueString checked in CandidateElementManager",
		        manager.isChecked(c2.getUniqueString()));
	}

	/**
	 * This does not 100% guarantee that thread-interleaving happens but its better than not testing
	 * at all.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testConcurrentIncrement() throws InterruptedException {
		final CandidateElementManager manager = new CandidateElementManager(null, null);
		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					for (int j = 0; j < 10; j++) {
						manager.increaseElementsCounter();
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}

		// Wait for all Threads are finished
		Thread.sleep(10 * 100);

		Assert.assertEquals("100 Elements should be checked", 100,
		        manager.numberOfExaminedElements());
	}
}

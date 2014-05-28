package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.*;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.XxHashGenerator;
import com.crawljax.core.state.duplicatedetection.XxHashGeneratorFactory;

public class XxHashGeneratorFactoryTest {

	@Test
	public void testGetInstance() {
		XxHashGeneratorFactory factory = new XxHashGeneratorFactory();
		assertEquals(factory.getInstance().getClass(),XxHashGenerator.class);
	}
}

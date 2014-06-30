package com.crawljax.core.state.duplicatedetection;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class DuplicateDetectionModuleTest {

	@Test
	public void testDuplicateDetectionModule() {
		DuplicateDetectionModule ddm = new DuplicateDetectionModule();
		System.out.println(ddm);
		NearDuplicateDetection ndd = ddm.provideNearDuplicateDetection(new XxHashGenerator());
		assertTrue(ndd.getDefaultThreshold() > 0);
		assertFalse(ndd.getFeatures().isEmpty());
	}
	
	@Test
	public void testDuplicateDetectionModuleCustomNdd() {
		List<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection defaultNdd = new NearDuplicateDetectionBroder(5, ImmutableList.copyOf(features));
		DuplicateDetectionModule ddm = new DuplicateDetectionModule(defaultNdd);
		NearDuplicateDetection ndd = ddm.provideNearDuplicateDetection(new XxHashGenerator());
		assertEquals(ndd, defaultNdd);
	}
}

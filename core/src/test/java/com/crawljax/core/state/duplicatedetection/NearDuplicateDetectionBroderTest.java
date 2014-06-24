package com.crawljax.core.state.duplicatedetection;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class NearDuplicateDetectionBroderTest {

	private List<FeatureType> features = new ArrayList<FeatureType>();

	@Test(expected = DuplicateDetectionException.class)
	public void testMissingFeatures() {
		HashGenerator hasher = new XxHashGenerator();
		new NearDuplicateDetectionBroder(2.0 / 6.0, ImmutableList.copyOf(features), hasher);
	}

	@Test(expected = DuplicateDetectionException.class)
	public void testFeaturesIsNull() {
		HashGenerator hasher = new XxHashGenerator();
		new NearDuplicateDetectionBroder(2.0 / 6.0, null, hasher);
	}

	@Test
	public void testToHighThreshold() {
		HashGenerator hasher = new XxHashGenerator();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		new NearDuplicateDetectionBroder(5, ImmutableList.copyOf(features), hasher);
	}

	@Test
	public void testToLowThreshold() {
		HashGenerator hasher = new XxHashGenerator();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection broder = new NearDuplicateDetectionBroder(1, ImmutableList.copyOf(features), hasher);
		broder.setDefaultThreshold(-1);
	}

	@Test
	public void testSetThresholdCorrect() {
		HashGenerator hasher = new XxHashGenerator();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection broder = new NearDuplicateDetectionBroder(1, ImmutableList.copyOf(features), hasher);
		broder.setDefaultThreshold(0.3);
		assertEquals(0.3, broder.getDefaultThreshold(), 0.0001);
	}

	@Test
	public void testSetFeaturesCorrect() {
		HashGenerator hasher = new XxHashGenerator();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.WORDS));
		NearDuplicateDetection broder = new NearDuplicateDetectionBroder(1, ImmutableList.copyOf(features), hasher);

		List<FeatureType> newFeatures = new ArrayList<FeatureType>();
		newFeatures.add(new FeatureShingles(1, FeatureShingles.SizeType.CHARS));
		broder.setFeatures(ImmutableList.copyOf(newFeatures));

		List<String> listOfFeatures = broder.getFeatures().asList().get(0).getFeatures("Test");
		assertEquals("T", listOfFeatures.get(0));
		assertEquals("e", listOfFeatures.get(1));
		assertEquals("s", listOfFeatures.get(2));
		assertEquals("t", listOfFeatures.get(3));
	}
}

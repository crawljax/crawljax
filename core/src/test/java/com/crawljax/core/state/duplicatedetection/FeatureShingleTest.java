package com.crawljax.core.state.duplicatedetection;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.FeatureShingles.ShingleType;
import com.google.common.collect.ImmutableList;

public class FeatureShingleTest {

	@Test
	public void testShingleChars() throws FeatureException {
		FeatureType shingleChars = new FeatureShingles(3, FeatureShingles.ShingleType.CHARS);
		String doc = "Test ha";
		List<String> features = shingleChars.getFeatures(doc);

		boolean tes = features.contains("Tes");
		assertTrue("'Tes' does not exist in the list of features", tes);
		boolean est = features.contains("est");
		assertTrue("'est' does not exist in the list of features", est);
		boolean st = features.contains("st ");
		assertTrue("'st ' does not exist in the list of features", st);
		boolean th = features.contains("t h");
		assertTrue("'t h' does not exist in the list of features", th);
		boolean ha = features.contains(" ha");
		assertTrue("' ha' does not exist in the list of features", ha);

		assertEquals(features.size(), 5);
	}

	@Test
	public void testShingleCharNotExist() throws FeatureException {
		FeatureType shingleChars = new FeatureShingles(3, FeatureShingles.ShingleType.CHARS);
		String doc = "Test ha";
		List<String> features = shingleChars.getFeatures(doc);

		boolean tes = features.contains("ha ");
		assertFalse("'ha ' does exists in the list of features, but should not", tes);
	}

	@Test
	public void testShingleWords() throws FeatureException {
		FeatureType shingleWords = new FeatureShingles(2, FeatureShingles.ShingleType.WORDS);
		String doc = "This is a test";
		List<String> features = shingleWords.getFeatures(doc);

		boolean thisis = features.contains("Thisis");
		assertTrue("'Thisis' does not exist in the list of features", thisis);
		boolean isa = features.contains("isa");
		assertTrue("'isa' does not exist in the list of features", isa);
		boolean atest = features.contains("atest");
		assertTrue("'atest' does not exist in the list of features", atest);

		assertEquals(features.size(), 3);
	}

	@Test
	public void testShingleWordNotExist() throws FeatureException {
		FeatureType shingleWords = new FeatureShingles(2, FeatureShingles.ShingleType.WORDS);
		String doc = "This is a test";
		List<String> features = shingleWords.getFeatures(doc);

		boolean test = features.contains("test");
		assertFalse("'test' exist in the list of features, but should not exists", test);
	}

	@Test
	public void testShingleSentencesSizeOne() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(1, FeatureShingles.ShingleType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);

		boolean first = features.contains("This is a test");
		assertTrue("'This is a test' does not exist in the list of features", first);
		boolean second = features.contains("Yes");
		assertTrue("'Yes' does not exist in the list of features", second);
		boolean third = features.contains("No more inspiration right now.");
		assertTrue("'No more inspiration right now.' does not exist in the list of features",
		        third);
		
		assertNotNull(shingleSentences.toString());
		assertEquals(features.size(), 3);
	}

	@Test
	public void testShingleSentencesSizeTwo() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(2, FeatureShingles.ShingleType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);

		boolean first = features.contains("This is a testYes");
		assertTrue("'This is a testYes' does not exist in the list of features", first);
		boolean second = features.contains("YesNo more inspiration right now.");
		assertTrue("'YesNo more inspiration right now.' does not exist in the list of features",
		        second);

		assertEquals(features.size(), 2);
	}

	@Test
	public void testShingleSentencesNotExist() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(2, FeatureShingles.ShingleType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);

		boolean first = features.contains("No more inspiration right now.");
		assertFalse(
		        "'No more inspiration right now.' exists in the list of features, but should not",
		        first);
	}

	@Test
	public void testFeatureSizeOnBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(7, FeatureShingles.ShingleType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "This is some text for the test.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test(expected = FeatureException.class)
	public void testFeatureSizeWordsOffBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(8, FeatureShingles.ShingleType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "This is some text for the test.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test
	public void testFeatureSizeCharsOnBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(14, FeatureShingles.ShingleType.CHARS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "A simple test.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test(expected = FeatureException.class)
	public void testFeatureSizeCharsOffBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(15, FeatureShingles.ShingleType.CHARS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "A simple test.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test
	public void testFeatureSizeSentencesOnBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.ShingleType.SENTENCES));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "This is some text for the test. Will it work.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test(expected = FeatureException.class)
	public void testFeatureSizeSentencesOffBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(FeatureShingles.withSize(3, FeatureShingles.ShingleType.SENTENCES));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "This is some text for the test. Will it work.";
		ndd.generateFingerprint(strippedDom);
	} 
	
	@Test
	public void testFeatureShingleWithRegex() {
		String doc = "1_2_3_4_5"; 
		FeatureShingles feature = FeatureShingles.withSize(3, "_");
		List<String> elements = feature.getFeatures(doc);
		assertEquals(elements.get(0),"123");
		assertEquals(elements.get(1),"234");
		assertEquals(elements.get(2),"345"); 
		assertEquals(elements.size(), 3);
	}
}
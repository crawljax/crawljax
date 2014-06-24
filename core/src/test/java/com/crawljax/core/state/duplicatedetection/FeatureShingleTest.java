package com.crawljax.core.state.duplicatedetection;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class FeatureShingleTest {

	@Test
	public void testShingleChars() throws FeatureException {
		FeatureType shingleChars = new FeatureShingles(3, FeatureShingles.SizeType.CHARS);
		String doc = "Test ha";
		List<String> features = shingleChars.getFeatures(doc);

		boolean tes = features.remove("Tes");
		assertTrue("'Tes' does not exist in the list of features", tes);
		boolean est = features.remove("est");
		assertTrue("'est' does not exist in the list of features", est);
		boolean st = features.remove("st ");
		assertTrue("'st ' does not exist in the list of features", st);
		boolean th = features.remove("t h");
		assertTrue("'t h' does not exist in the list of features", th);
		boolean ha = features.remove(" ha");
		assertTrue("' ha' does not exist in the list of features", ha);

		assertEquals(features.size(), 0);
	}

	@Test
	public void testShingleCharNotExist() throws FeatureException {
		FeatureType shingleChars = new FeatureShingles(3, FeatureShingles.SizeType.CHARS);
		String doc = "Test ha";
		List<String> features = shingleChars.getFeatures(doc);

		boolean tes = features.remove("ha ");
		assertFalse("'ha ' does exists in the list of features, but should not", tes);
	}

	@Test
	public void testShingleWords() throws FeatureException {
		FeatureType shingleWords = new FeatureShingles(2, FeatureShingles.SizeType.WORDS);
		String doc = "This is a test";
		List<String> features = shingleWords.getFeatures(doc);

		boolean thisis = features.remove("Thisis");
		assertTrue("'Thisis' does not exist in the list of features", thisis);
		boolean isa = features.remove("isa");
		assertTrue("'isa' does not exist in the list of features", isa);
		boolean atest = features.remove("atest");
		assertTrue("'atest' does not exist in the list of features", atest);

		assertEquals(features.size(), 0);
	}

	@Test
	public void testShingleWordNotExist() throws FeatureException {
		FeatureType shingleWords = new FeatureShingles(2, FeatureShingles.SizeType.WORDS);
		String doc = "This is a test";
		List<String> features = shingleWords.getFeatures(doc);

		boolean test = features.remove("test");
		assertFalse("'test' exist in the list of features, but should not exists", test);
	}

	@Test
	public void testShingleSentencesSizeOne() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(1, FeatureShingles.SizeType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);

		boolean first = features.remove("This is a test");
		assertTrue("'This is a test' does not exist in the list of features", first);
		boolean second = features.remove("Yes");
		assertTrue("'Yes' does not exist in the list of features", second);
		boolean third = features.remove("No more inspiration right now.");
		assertTrue("'No more inspiration right now.' does not exist in the list of features",
		        third);

		assertEquals(features.size(), 0);
	}

	@Test
	public void testShingleSentencesSizeTwo() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(2, FeatureShingles.SizeType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);

		boolean first = features.remove("This is a testYes");
		assertTrue("'This is a testYes' does not exist in the list of features", first);
		boolean second = features.remove("YesNo more inspiration right now.");
		assertTrue("'YesNo more inspiration right now.' does not exist in the list of features",
		        second);

		assertEquals(features.size(), 0);
	}

	@Test
	public void testShingleSentencesNotExist() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(2, FeatureShingles.SizeType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);

		boolean first = features.remove("No more inspiration right now.");
		assertFalse(
		        "'No more inspiration right now.' exists in the list of features, but should not",
		        first);
	}

	@Test
	public void testFeatureSizeOnBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(7, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "This is some text for the test.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test(expected = FeatureException.class)
	public void testFeatureSizeWordsOffBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(8, FeatureShingles.SizeType.WORDS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "This is some text for the test.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test
	public void testFeatureSizeCharsOnBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(14, FeatureShingles.SizeType.CHARS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "A simple test.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test(expected = FeatureException.class)
	public void testFeatureSizeCharsOffBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(15, FeatureShingles.SizeType.CHARS));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "A simple test.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test
	public void testFeatureSizeSentencesOnBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureShingles.SizeType.SENTENCES));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "This is some text for the test. Will it work.";
		ndd.generateFingerprint(strippedDom);
	}

	@Test(expected = FeatureException.class)
	public void testFeatureSizeSentencesOffBoundary() throws FeatureException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(3, FeatureShingles.SizeType.SENTENCES));

		HashGenerator hasher = new XxHashGenerator();
		NearDuplicateDetection ndd = new NearDuplicateDetectionCrawlhash(3, ImmutableList.copyOf(features), hasher);
		String strippedDom = "This is some text for the test. Will it work.";
		ndd.generateFingerprint(strippedDom);
	}
}

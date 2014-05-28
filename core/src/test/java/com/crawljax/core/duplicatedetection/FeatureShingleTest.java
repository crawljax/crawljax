package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.*;

public class FeatureShingleTest {

	@Test
	public void testShingleChars() throws FeatureException {
		FeatureType shingleChars = new FeatureShingles(3, FeatureShingles.SizeType.CHARS);
		String doc = "Test ha";
		List<String> features = shingleChars.getFeatures(doc);
		
		boolean tes = features.remove("Tes");
		assertTrue(tes);
		boolean est = features.remove("est");
		assertTrue(est);
		boolean st = features.remove("st ");
		assertTrue(st);
		boolean th = features.remove("t h");
		assertTrue(th);
		boolean ha = features.remove(" ha");
		assertTrue(ha);
		
		assertTrue(features.isEmpty());
	}
	
	@Test
	public void testShingleCharNotExist() throws FeatureException {
		FeatureType shingleChars = new FeatureShingles(3, FeatureShingles.SizeType.CHARS);
		String doc = "Test ha";
		List<String> features = shingleChars.getFeatures(doc);
		
		boolean tes = features.remove("ha ");
		assertFalse(tes);
	}
	
	@Test
	public void testShingleWords() throws FeatureException {
		FeatureType shingleWords = new FeatureShingles(2, FeatureShingles.SizeType.WORDS);
		String doc = "This is a test";
		List<String> features = shingleWords.getFeatures(doc);
		
		boolean thisis = features.remove("Thisis");
		assertTrue(thisis);
		boolean isa = features.remove("isa");
		assertTrue(isa);
		boolean atest = features.remove("atest");
		assertTrue(atest);
		
		assertTrue(features.isEmpty());
	}
	
	@Test
	public void testShingleWordNotExist() throws FeatureException {
		FeatureType shingleWords = new FeatureShingles(2, FeatureShingles.SizeType.WORDS);
		String doc = "This is a test";
		List<String> features = shingleWords.getFeatures(doc);
		
		boolean test = features.remove("test");
		assertFalse(test);
	}
	
	@Test
	public void testShingleSentencesSizeOne() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(1, FeatureShingles.SizeType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);
		
		boolean first = features.remove("This is a test");
		assertTrue(first);
		boolean second = features.remove("Yes");
		assertTrue(second);
		boolean third = features.remove("No more inspiration right now.");
		assertTrue(third);
		
		assertTrue(features.isEmpty());
	}
	
	@Test
	public void testShingleSentencesSizeTwo() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(2, FeatureShingles.SizeType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);
		
		boolean first = features.remove("This is a testYes");
		assertTrue(first);
		boolean second = features.remove("YesNo more inspiration right now.");
		assertTrue(second);
		
		assertTrue(features.isEmpty());
	}
	
	@Test
	public void testShingleSentencesNotExist() throws FeatureException {
		FeatureType shingleSentences = new FeatureShingles(2, FeatureShingles.SizeType.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);
		
		boolean first = features.remove(" No more inspiration right now");
		assertFalse(first);
	}
	
	@Test
	public void testFeatureSizeOnBoundary() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(7, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "This is some text for the test.";
		ndd.generateHash(strippedDom);
	}
	
	@Test (expected = FeatureShinglesException.class)
	public void testFeatureSizeWordsOffBoundary() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(8, FeatureSizeType.WORDS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "This is some text for the test.";
		ndd.generateHash(strippedDom);
	}
	
	@Test
	public void testFeatureSizeCharsOnBoundary() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(14, FeatureSizeType.CHARS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "A simple test.";
		ndd.generateHash(strippedDom);
	}
	
	@Test (expected = FeatureShinglesException.class)
	public void testFeatureSizeCharsOffBoundary() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(15, FeatureSizeType.CHARS));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "A simple test.";
		ndd.generateHash(strippedDom);
	}
	
	@Test
	public void testFeatureSizeSentencesOnBoundary() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(2, FeatureSizeType.SENTENCES));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "This is some text for the test. Will it work.";
		ndd.generateHash(strippedDom);
	}
	
	@Test (expected = FeatureShinglesException.class)
	public void testFeatureSizeSentencesOffBoundary() throws FeatureShinglesException {
		ArrayList<FeatureType> features = new ArrayList<FeatureType>();
		features.add(new FeatureShingles(3, FeatureSizeType.SENTENCES));
		
		NearDuplicateDetectionCrawlHash32 ndd = new NearDuplicateDetectionCrawlHash32(3, features);
		String strippedDom = "This is some text for the test. Will it work.";
		ndd.generateHash(strippedDom);
	}
}

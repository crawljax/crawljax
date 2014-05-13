package com.crawljax.core.duplicatedetection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureShinglesException;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.Type;

public class FeatureShingleTest {

	@Test
	public void testShingleChars() throws FeatureShinglesException {
		FeatureType shingleChars = new FeatureShingles(3, Type.CHARS);
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
	public void testShingleCharNotExist() throws FeatureShinglesException {
		FeatureType shingleChars = new FeatureShingles(3, Type.CHARS);
		String doc = "Test ha";
		List<String> features = shingleChars.getFeatures(doc);
		
		boolean tes = features.remove("ha ");
		assertFalse(tes);
	}
	
	@Test
	public void testShingleWords() throws FeatureShinglesException {
		FeatureType shingleWords = new FeatureShingles(2, Type.WORDS);
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
	public void testShingleWordNotExist() throws FeatureShinglesException {
		FeatureType shingleWords = new FeatureShingles(2, Type.WORDS);
		String doc = "This is a test";
		List<String> features = shingleWords.getFeatures(doc);
		
		boolean test = features.remove("test");
		assertFalse(test);
	}
	
	@Test
	public void testShingleSentencesSizeOne() throws FeatureShinglesException {
		FeatureType shingleSentences = new FeatureShingles(1, Type.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);
		
		boolean first = features.remove("This is a test");
		assertTrue(first);
		boolean second = features.remove(" Yes");
		assertTrue(second);
		boolean third = features.remove(" No more inspiration right now");
		assertTrue(third);
		
		assertTrue(features.isEmpty());
	}
	
	@Test
	public void testShingleSentencesSizeTwo() throws FeatureShinglesException {
		FeatureType shingleSentences = new FeatureShingles(2, Type.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);
		
		boolean first = features.remove("This is a test Yes");
		assertTrue(first);
		boolean second = features.remove(" Yes No more inspiration right now");
		assertTrue(second);
		
		assertTrue(features.isEmpty());
	}
	
	@Test
	public void testShingleSentencesNotExist() throws FeatureShinglesException {
		FeatureType shingleSentences = new FeatureShingles(2, Type.SENTENCES);
		String docSen = "This is a test. Yes. No more inspiration right now.";
		List<String> features = shingleSentences.getFeatures(docSen);
		
		boolean first = features.remove(" No more inspiration right now");
		assertFalse(first);
	}
}

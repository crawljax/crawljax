package com.crawljax.core.state.duplicatedetection;

import java.util.LinkedList;
import java.util.List;

public class FeatureShingles implements FeatureType {

	private List<String> binaryWords;
	private Type type;
	private int count;
	
	public FeatureShingles(int count, Type value) {
		this.type = value;
		this.count = count;
	}

	
	@Override
	public List<String> getFeatures(String doc) throws FeatureShinglesException {
		binaryWords = new LinkedList<String>();
		String[] features = this.getFeatures(type, doc);
		for(int i = 0; i < features.length; i++) {
			binaryWords.add(features[i]);
		}
		return binaryWords;
	}
	
	private String[] getFeatures(Type type, String doc) throws FeatureShinglesException {
		if (type == Type.CHARS) {
			return this.getChars(doc);
		} else if (type == Type.WORDS) {
			return this.getWords(doc);
		} else if (type == Type.SENTENCES) {
			return this.getSentences(doc);
		} else {
			throw new FeatureShinglesException("The type of the feature selection is not possible"); 
		}
	}
	
	private String[] getChars(String doc) throws FeatureShinglesException {
		if (doc.length() < this.count) {
			throw new FeatureShinglesException("The size of the chars per feature is to big for this document. The document has only a size of " + doc.length() + " char(s).");
		}
		String[] resultFeatures = new String[doc.length() - count + 1];
		for (int j=0; j<resultFeatures.length; j++) {
			String feature = "";
			for (int i=0; i<count; i++) {
				feature += doc.charAt(j + i);
			}
			resultFeatures[j] = feature;
		}
		return resultFeatures;
	}
	
	private String[] getWords(String doc) throws FeatureShinglesException {
		String[] words = doc.split(" ");
		if (words.length < this.count) {
			throw new FeatureShinglesException("The size of the words per feature is to big for this document. The document has only a size of " + words.length + " word(s).");
		}
		return generateFeatures(doc, words);
	}
	
	private String[] getSentences(String doc) throws FeatureShinglesException {
		String[] sentences = doc.split("\\.");
		if (sentences.length < this.count) {
			throw new FeatureShinglesException("The size of the sentences per feature is to big for this document. The document has only a size of " + sentences.length + " sentence(s).");
		}
		return generateFeatures(doc, sentences);
	}
	
	private String[] generateFeatures(String doc, String[] wordsOrSentences) {
		String[] resultFeatures = new String[wordsOrSentences.length - count + 1];
		for (int j=0; j<resultFeatures.length; j++) {
			String feature = "";
			for (int i=0; i<count; i++) {
				feature += wordsOrSentences[j + i];
			}
			resultFeatures[j] = feature;
		}
		return resultFeatures;
	}
}
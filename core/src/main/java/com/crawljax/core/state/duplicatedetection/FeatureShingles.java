package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FeatureShingles represents the features based on collecting shingles from a document. An example
 * of a 2-shingle-collection of the string "abcd" => {"ab","bc","cd"}.
 * More information: http://en.wikipedia.org/wiki/W-shingling
 */
public class FeatureShingles implements FeatureType {

	/**
	 * Specifies the type of chunks used to be shingled.
	 */
	public enum SizeType {
		CHARS, WORDS, SENTENCES
	}

	private SizeType type;
	private int size;

	/**
	 * @param size
	 *            represents the size of a single shingle in the number of chunks.
	 * @param type
	 *            the SizeType to be used for the shingles.
	 */
	public FeatureShingles(int size, SizeType type) {
		this.type = type;
		this.size = size;
	}

	/**
	 * Given a doc, the relevant set of shingles is generated, using the predefined SizeType.
	 * 
	 * @param doc
	 *            the document of the shingles
	 * @return shingles as an array
	 * @throws FeatureException
	 *             Unknown feature-type or document to small for feature-generation.
	 */
	public List<String> getFeatures(String doc) throws FeatureException {
		String[] features = this.getFeatures(type, doc);
		return new ArrayList<String>(Arrays.asList(features));
	}

	/**
	 * Given a type and doc, the relevant set of shingles is generated.
	 * 
	 * @param type
	 *            the type of string the shingles should be generated over.
	 * @param doc
	 *            the document of the shingles
	 * @return shingles as an array
	 * @throws FeatureException
	 *             Unknown feature-type or document to small for feature-generation.
	 */
	private String[] getFeatures(SizeType type, String doc) throws FeatureException {
		switch (type) {
			case CHARS:
				return this.getChars(doc);
			case WORDS:
				return this.getWords(doc);
			case SENTENCES:
				return this.getSentences(doc);
			default:
				throw new FeatureException("Unkown size-type " + type
				        + " provided for the Shingle-feature.");
		}
	}

	/**
	 * Splits the document into chars.
	 * 
	 * @param doc
	 *            the document to be split. Must contain more elements than the feature-type-size.
	 * @return array containing chars.
	 * @throws FeatureException
	 *             the doc is too small to represent in this feature-type and size.
	 */
	private String[] getChars(String doc) throws FeatureException {
		String[] chars = doc.split("(?!^)");
		if (doc.length() < this.size) {
			throw new FeatureException(
			        "The size of the chars per feature is too large for this document. "
			                + "The document has a size of " + doc.length()
			                + " char(s), feature needs " + size);
		}
		return generateFeatures(chars);
	}

	/**
	 * Splits the document into words.
	 * 
	 * @param doc
	 *            the document to be split. Must contain more elements than the feature-type-size.
	 * @return array containing words.
	 * @throws FeatureException
	 *             the doc is too small to represent in this feature-type and size.
	 */
	private String[] getWords(String doc) throws FeatureException {
		String[] words = doc.split(" ");
		if (words.length < this.size) {
			throw new FeatureException(
			        "The size of the words per feature is too large for this document. "
			                + "The document has a size of " + words.length
			                + " word(s), feature needs " + size);
		}
		return generateFeatures(words);
	}

	/**
	 * Splits the document into sentences.
	 * 
	 * @param doc
	 *            the document to be split. Must contain more elements than the feature-type-size.
	 * @return array containing sentences.
	 * @throws FeatureException
	 *             the doc is too small to represent in this feature-type and size.
	 */
	private String[] getSentences(String doc) throws FeatureException {
		String[] sentences = doc.split("(\\!|\\?|\\. )");
		if (sentences.length < this.size) {
			throw new FeatureException(
			        "The size of the sentences per feature is too large for this document. "
			                + "The document has a size of " + sentences.length
			                + " sentence(s), feature needs " + size);
		}
		return generateFeatures(sentences);
	}

	/**
	 * Generates shingles from the String-array.
	 * 
	 * @param originalStrings
	 *            the original DOM split-up in the parts to be shingled.
	 * @return shingles generated from originalStrings
	 */
	private String[] generateFeatures(String[] originalStrings) {
		String[] resultFeatures = new String[originalStrings.length - size + 1];
		for (int j = 0; j < resultFeatures.length; j++) {
			String feature = "";
			// Append this.size strings to each other to form a shingle
			for (int i = 0; i < size; i++) {
				feature += originalStrings[j + i];
			}
			resultFeatures[j] = feature;
		}
		return resultFeatures;
	}

	public String toString() {
		return "FeatureShingles[" + this.size + ", " + this.type + "]";		
	}
}
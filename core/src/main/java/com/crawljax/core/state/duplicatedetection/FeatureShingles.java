package com.crawljax.core.state.duplicatedetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FeatureShingles represents the features based on collecting shingles from a document. An example
 * of a 2-shingle-collection of the string <code>"abcd" => {"ab","bc","cd"}</code>.
 * <a href="http://en.wikipedia.org/wiki/W-shingling">More information.</a>
 */
public class FeatureShingles implements FeatureType {

	/**
	 * Specifies the type of chunks used to be shingled.
	 */
	public enum SizeType {
		CHARS, WORDS, SENTENCES
	}

	private final SizeType type;
	private final int size;

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
	private String[] getChars(String doc) {
		return getShinglesByRegEx(doc, "(?!^)");
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
	private String[] getWords(String doc) {
		return getShinglesByRegEx(doc, " ");
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
	private String[] getSentences(String doc) {
		return getShinglesByRegEx(doc, "(\\!|\\?|\\. )");
	}

	/**
	 * Splits the document in shingles based on a particular regular expression.
	 * 
	 * @param doc
	 *            the original document.
	 * @param regex
	 *            the regular expression that is used to split the doc.
	 * @return shingles of the doc split using the regex.
	 */
	private String[] getShinglesByRegEx(String doc, String regex) {
		String[] shingles = doc.split(regex);
		if (shingles.length < size) {
			throw new FeatureException(
			        "The size of the chosen feature is too large for this document. "
			                + "The document has a size of " + shingles.length
			                + " shingles, feature needs at least " + size);
		}
		return generateFeatures(shingles);
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
		StringBuilder feature = null;
		for (int j = 0; j < resultFeatures.length; j++) {
			feature = new StringBuilder(originalStrings[j].length() * size);
			// Append this.size strings to each other to form a shingle
			for (int i = 0; i < size; i++) {
				feature += originalStrings[j + i];
			}
			resultFeatures[j] = feature;
		}
		return resultFeatures;
	}

	public String toString() {
		return "FeatureShingles[" + size + ", " + type + "]";
	}
}
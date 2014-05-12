package com.crawljax.core.state.duplicatedetection;

import java.util.LinkedList;
import java.util.List;

public class FeatureShinglesChars implements FeatureType {

	int shingleSize;
	
	public FeatureShinglesChars(int shingleSize) {
		this.shingleSize = shingleSize;
	}
	
	@Override
	public List<String> generateFeatures(String doc) {
		List<String> binaryWords = new LinkedList<String>();
		for(int i = 0; i < doc.length() - 1; i += 1) {
			StringBuilder bui = new StringBuilder();
			bui.append(doc.charAt(i)).append(doc.charAt(i + 1)); // add 2 successive characters to  list
			binaryWords.add(bui.toString());
		}
		return binaryWords;
	}

}

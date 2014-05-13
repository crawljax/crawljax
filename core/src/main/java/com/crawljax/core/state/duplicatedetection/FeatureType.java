package com.crawljax.core.state.duplicatedetection;

import java.util.List;


public interface FeatureType {
	
	List<String> getFeatures(String doc) throws FeatureShinglesException;

}

package com.crawljax.core.state.duplicatedetection;

import java.util.List;

public interface FeatureSelection {
	
	public List<String> generateFeatures(String doc);

}

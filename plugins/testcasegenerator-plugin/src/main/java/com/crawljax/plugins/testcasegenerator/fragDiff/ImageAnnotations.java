package com.crawljax.plugins.testcasegenerator.fragDiff;

import java.util.ArrayList;

import com.crawljax.core.state.StateVertex;

public class ImageAnnotations {

	private ArrayList<ImageAnnotation> annotations;
	private int state;

	public ImageAnnotations(StateVertex stateVertex) {
		this.setState(stateVertex.getId());
		this.annotations = new ArrayList<ImageAnnotation>();
	}

	public void add(ImageAnnotation imageAnnotation) {
		this.annotations.add(imageAnnotation);
	}

	public ArrayList<ImageAnnotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(ArrayList<ImageAnnotation> annotations) {
		this.annotations = annotations;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
}

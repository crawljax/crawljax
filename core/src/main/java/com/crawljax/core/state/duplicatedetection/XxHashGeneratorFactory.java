package com.crawljax.core.state.duplicatedetection;

/**
 * HashGeneratorFactory for building xxHashGenerators.
 */
public class XxHashGeneratorFactory implements HashGeneratorFactory {

	@Override
	public HashGenerator getInstance() {
		return new XxHashGenerator();
	}

}

package com.crawljax.core.configuration;

import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Reader for InputSpecification. For internal use only!
 * 
 * @author Danny
 * @version $Id$
 */
public class InputSpecificationReader {

	private InputSpecification inputSpecification;

	/**
	 * Wrap around inputSpecification.
	 * 
	 * @param inputSpecification
	 *            The inputSpecification to wrap around.
	 */
	public InputSpecificationReader(InputSpecification inputSpecification) {
		this.inputSpecification = inputSpecification;
	}

	/**
	 * @return The configuration of the inputspecification.
	 */
	public PropertiesConfiguration getConfiguration() {
		return inputSpecification.getConfiguration();
	}

}

package com.crawljax.core;

import com.crawljax.core.state.StateVertex;

/**
 * Gets thrown when Crawljax cannot get to a target {@link StateVertex}.
 */
@SuppressWarnings("serial")
public class StateUnreachableException extends CrawljaxException {

	private StateVertex target;

	public StateUnreachableException(StateVertex state, String reason) {
		super("Cannot reach state " + state.getName() + " because " + reason);
		this.target = state;
	}

	public StateVertex getTarget() {
		return target;
	}

}

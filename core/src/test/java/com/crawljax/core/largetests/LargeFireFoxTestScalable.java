/**
 * 
 */
package com.crawljax.core.largetests;

import com.crawljax.core.state.StateFlowGraph.StateFlowGraphType;

/**
 * @author arz
 */
public class LargeFireFoxTestScalable extends LargeFirefoxTest {

	@Override
	StateFlowGraphType getGraphType() {
		return StateFlowGraphType.SCALABLE;
	}

}

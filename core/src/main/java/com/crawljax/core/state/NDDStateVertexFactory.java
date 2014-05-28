package com.crawljax.core.state;

public class NDDStateVertexFactory extends StateVertexFactory {

	@Override
	public StateVertex newStateVertex(int id, String url, String name,
			String dom, String strippedDom) {
		return new StateVertexNDD(id, url, name, dom, strippedDom);
	}

}

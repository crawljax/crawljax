package com.crawljax.core.state;

import com.crawljax.core.state.duplicatedetection.Fingerprint;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.google.inject.Inject;

public class NDDStateVertexFactory extends StateVertexFactory {
	
	private NearDuplicateDetection nearDuplicateDetection;

	@Inject
	public void setNearDuplicateDetection(NearDuplicateDetection nearDuplicateDetection) {
		this.nearDuplicateDetection = nearDuplicateDetection;
	}

	@Override
	public StateVertex newStateVertex(int id, String url, String name,
			String dom, String strippedDom) {
		Fingerprint fingerprint = nearDuplicateDetection.generateHash(dom);
		return new StateVertexNDD(id, url, name, dom, strippedDom, fingerprint);
	}

}

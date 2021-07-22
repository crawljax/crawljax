package com.crawljax.stateabstractions.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.stateabstractions.dom.DOMConfiguration.Mode;
import com.crawljax.stateabstractions.dom.simhash.BitHashRabin;
import com.crawljax.stateabstractions.dom.simhash.HammingDistance;
import com.crawljax.stateabstractions.dom.simhash.SimHash;

/**
 * The default factory that creates State vertexes with a
 * {@link Object#hashCode()} and {@link Object#equals(Object)} function based on
 * the Stripped dom.
 */
public class SimHashStateVertexFactory extends StateVertexFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(SimHashStateVertexFactory.class.getName());
	private static double threshold = 0.046; // K=3 as discussed in the Manku et.al. paper
	private static Mode mode;

	public SimHashStateVertexFactory(double treshold, Mode mode1) {
		threshold= treshold;
		mode = mode1;
	}
	
	public SimHashStateVertexFactory(Mode mode1) {
		mode = mode1;
	}

	@Override
	public StateVertex newStateVertex(int id, String url, String name, String dom, String strippedDom,
			EmbeddedBrowser browser) {
		return new SimHashStateVertexImpl(id, url, name, dom, strippedDom, threshold, mode);
	}

	@Override
	public String toString() {
		return "DOMSimHash_" + mode.toString() + "_" + threshold;
	}
	
	public static List<String> tokenizeContent(String content){
		List<String> tokens = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(content, ", ");
		if(tokenizer.hasMoreTokens())
			tokens.add(tokenizer.nextToken());
		return tokens;
	}

	public static String getSimHash(List<String> tokens) {
		SimHash simHashObj = new SimHash();
		for(String token: tokens) {
			BitHashRabin bHR = new BitHashRabin(token);
			simHashObj.add(bHR);
        }
        String simHash = simHashObj.getStringFingerprint().replaceAll(" ","");
        return simHash;
	}
	
	public static int calcuateSimHashDistance(String simHash1, String simHash2) {
		int distance = HammingDistance.hamming(simHash1, simHash2);
		return distance;
	}
}

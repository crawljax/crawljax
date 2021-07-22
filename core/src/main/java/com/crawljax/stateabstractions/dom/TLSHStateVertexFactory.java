package com.crawljax.stateabstractions.dom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexFactory;
import com.crawljax.stateabstractions.dom.DOMConfiguration.Mode;
import com.crawljax.oraclecomparator.comparators.EditDistanceComparator;
import com.idealista.tlsh.TLSH;
import com.idealista.tlsh.digests.Digest;
import com.idealista.tlsh.digests.DigestBuilder;
import com.idealista.tlsh.exceptions.InsufficientComplexityException;

import net.bytebuddy.implementation.bytecode.Throw;

/**
 * The default factory that creates State vertexes with a
 * {@link Object#hashCode()} and {@link Object#equals(Object)} function based on
 * the Stripped dom.
 */
public class TLSHStateVertexFactory extends StateVertexFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(TLSHStateVertexFactory.class.getName());
	private static double threshold = 0.0;
	private static Mode mode;
	private static double insufficientComplexityDistance = 200;
	private static EditDistanceComparator editDistanceComparator;

	public TLSHStateVertexFactory(double treshold, Mode mode1) {
		threshold= treshold;
		mode = mode1;
		editDistanceComparator = new EditDistanceComparator(1-threshold);
	}

	@Override
	public StateVertex newStateVertex(int id, String url, String name, String dom, String strippedDom,
			EmbeddedBrowser browser) {
		return new TLSHStateVertexImpl(id, url, name, dom, strippedDom, threshold, mode, editDistanceComparator);
	}

	@Override
	public String toString() {
		return "DOMTLSH_" + mode.toString() + "_" + threshold;
	}

	public static double computeTLSHDistance(String dom1, String dom2) throws IllegalArgumentException{
		int distance = 200;
		TLSH tlsh1, tlsh2;
		Digest digest1, digest2;

		try {
			tlsh1 = new TLSH(dom1);
			digest1 = new DigestBuilder().withHash(tlsh1.hash()).build();
		} catch (com.idealista.tlsh.exceptions.InsufficientComplexityException e) {
			LOG.info("Insufficient Complexity in DOM");
			LOG.debug(dom1);
			throw new IllegalArgumentException();
		}

		try {
			tlsh2 = new TLSH(dom2);
			digest2 = new DigestBuilder().withHash(tlsh2.hash()).build();
		} catch (com.idealista.tlsh.exceptions.InsufficientComplexityException e) {
			LOG.info("Insufficient Complexity in DOM");
			LOG.debug(dom2);
			throw new IllegalArgumentException();
		}

		distance = digest2.calculateDifference(digest1, true);

		return distance;
	}
	
}

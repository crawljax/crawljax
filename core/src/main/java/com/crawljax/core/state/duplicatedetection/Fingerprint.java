package com.crawljax.core.state.duplicatedetection;

public interface Fingerprint {

	/**
	 * Checks whether hash1 and hash2 are near-duplicates.
	 * 
	 * @param hash1
	 *            hash(es)
	 * @param hash2
	 *            hash(es)
	 * @return true if near-duplicate, else return false
	 */
	public boolean isNearDuplicateHash(Fingerprint other);
	
	/**
	 * Checks whether hash1 and hash2 are near-duplicates.
	 * 
	 * @param hash1
	 *            hash(es)
	 * @param hash2
	 *            hash(es)
	 * @return true if near-duplicate, else return false
	 */
	public boolean isNearDuplicateHash(Fingerprint other, double threshold);

	/**
	 * An extension of isNearDuplicateHash, which also shows the distance between two hashes.
	 * 
	 * @param hash1
	 *            hash(es)
	 * @param hash2
	 *            hash(es)
	 * @return the distance (i.e. # of different bit positions) between the hashes
	 */
	public double getDistance(Fingerprint hash2);
}

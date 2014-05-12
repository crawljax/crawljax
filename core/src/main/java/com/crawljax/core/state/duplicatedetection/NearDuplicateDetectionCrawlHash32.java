package com.crawljax.core.state.duplicatedetection;

import java.util.List;

import net.jpountz.xxhash.XXHash32;
import net.jpountz.xxhash.XXHashFactory;

public class NearDuplicateDetectionCrawlHash32 implements NearDuplicateDetection {

	private XXHash32 xxhash;
	
	public NearDuplicateDetectionCrawlHash32() {
		xxhash = XXHashFactory.fastestInstance().hash32();
	}
	
	@Override
	public long generateHash(String doc) {
		int bitLen = 32;
		int hash = 0x00000000;
		int one = 0x00000001; //8
		int[] bits = new int[bitLen];
		List<String> tokens = null;
		for (String t : tokens) {
			int v = xxhash.hash(t.getBytes(), 0, bitLen, 0x9747b28c);
			for (int i = bitLen; i >= 1; --i) {
				if (((v >> (bitLen - i)) & 1) == 1)
					++bits[i - 1];
				else
					--bits[i - 1];
			}
		}
		for (int i = bitLen; i >= 1; --i) {
			if (bits[i - 1] > 1) {
				hash |= one;
			}
			one = one << 1;
		}
		return hash;
	}

	@Override
	public boolean hasNearDuplicateHash(long hash) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long findNearDuplicateHash(long hash) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isNearDuplicateHash(long hash1, long hash2) {
		// TODO Auto-generated method stub
		return false;
	}	
}

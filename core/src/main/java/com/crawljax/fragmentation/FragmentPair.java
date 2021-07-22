package com.crawljax.fragmentation;

import com.crawljax.fragmentation.Fragment.FragmentComparision;

public class FragmentPair {

	public FragmentPair(FragmentOutput fragment1, FragmentOutput fragment2, FragmentComparision comparision) {
		this.fragment1 = fragment1;
		this.fragment2 = fragment2; 
		this.comparision = comparision;
	}
	private FragmentOutput fragment1;
	private FragmentOutput fragment2; 
	private FragmentComparision comparision;
	public FragmentOutput getFragment1() {
		return fragment1;
	}
	public void setFragment1(FragmentOutput fragment1) {
		this.fragment1 = fragment1;
	}
	public FragmentOutput getFragment2() {
		return fragment2;
	}
	public void setFragment2(FragmentOutput fragment2) {
		this.fragment2 = fragment2;
	}
	public FragmentComparision getComparision() {
		return comparision;
	}
	public void setComparision(FragmentComparision comparision) {
		this.comparision = comparision;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FragmentPair) {
			FragmentPair that = (FragmentPair)obj;
			try {
			if((this.fragment1.equals(that.fragment1) && this.fragment2.equals(that.fragment2) && this.comparision==that.comparision)
			||	(this.fragment1.equals(that.fragment2) && this.fragment2.equals(that.fragment1) && this.comparision==that.comparision))
				return true;
			}catch(Exception ex) {
				System.out.println(this);
				System.out.println(that);
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Fragment1 : "  + fragment1 + "Fragment2 : " + fragment2 + " are " + comparision;
	}
	
}

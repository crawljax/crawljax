package com.crawljax.fragmentation;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.crawljax.util.XPathHelper;

public class FragmentOutput {
	
	int stateId;
	int fragmentId;
	List<String> vipsBlocks;
	String parentBox;
	boolean isUseful;
	boolean isGlobal;
	boolean isDynamic;
	private List<String> equivalents;
	private List<String> nd2;
	private List<String> duplicates;
	
	
	public FragmentOutput(Fragment fragment) {
		if(fragment == null) {
			return ;
		}
		
		List<String> visualBlockXpaths = new ArrayList<String>();
		for(Node vipsBlock : fragment.getNestedBlocks()) {
			visualBlockXpaths.add(XPathHelper.getSkeletonXpath(vipsBlock));
		}
				
		this.fragmentId = fragment.getId();
		this.stateId = fragment.getReferenceState().getId();
		this.parentBox = XPathHelper.getSkeletonXpath(fragment.getFragmentParentNode());
		this.vipsBlocks = visualBlockXpaths;
		this.isUseful = fragment.isUseful()!=null?fragment.isUseful():false;
		this.isDynamic = fragment.isDynamic();
		this.isGlobal = fragment.isGlobal();
		
		List<String> duplicates = new ArrayList<String>();
		
		for(Fragment duplicate : fragment.getDuplicateFragments()) {
			if(duplicate ==null) {
				continue;
			}
			String duplicateString = duplicate.getReferenceState().getName() + "_" + duplicate.getId();
			duplicates.add(duplicateString);
		}
		
		this.duplicates = duplicates;
		
		
		List<String> equivalents = new ArrayList<String>();
		
		for(Fragment duplicate : fragment.getEquivalentFragments()) {
			if(duplicate ==null) {
				continue;
			}
			String duplicateString = duplicate.getReferenceState().getName() + "_" + duplicate.getId();
			equivalents.add(duplicateString);
		}
		
		this.equivalents = equivalents;
		
		List<String> nd2 = new ArrayList<String>();
		
		for(Fragment duplicate : fragment.getNd2Fragments()) {
			if(duplicate ==null) {
				continue;
			}
			String duplicateString = duplicate.getReferenceState().getName() + "_" + duplicate.getId();
			nd2.add(duplicateString);
		}
		
		this.nd2 = nd2;
		
		
	}

	public List<String> getEquivalents() {
		return equivalents;
	}

	public void setEquivalents(List<String> equivalents) {
		this.equivalents = equivalents;
	}

	public List<String> getNd2() {
		return nd2;
	}

	public void setNd2(List<String> nd2) {
		this.nd2 = nd2;
	}

	public List<String> getDuplicates() {
		return duplicates;
	}

	public void setDuplicates(List<String> duplicates) {
		this.duplicates = duplicates;
	}

	public int getStateId() {
		return stateId;
	}
	public void setStateId(int stateId) {
		this.stateId = stateId;
	}
	public int getFragmentId() {
		return fragmentId;
	}
	public void setFragmentId(int fragmentId) {
		this.fragmentId = fragmentId;
	}
	public List<String> getVipsBlocks() {
		return vipsBlocks;
	}
	public void setVipsBlocks(List<String> vipsBlocks) {
		this.vipsBlocks = vipsBlocks;
	}
	public String getParentBox() {
		return parentBox;
	}
	public void setParentBox(String parentBox) {
		this.parentBox = parentBox;
	}
	public boolean isUseful() {
		return isUseful;
	}
	public void setUseful(boolean isUseful) {
		this.isUseful = isUseful;
	}
	public boolean isGlobal() {
		return isGlobal;
	}
	public void setGlobal(boolean isGlobal) {
		this.isGlobal = isGlobal;
	}
	public boolean isDynamic() {
		return isDynamic;
	}
	public void setDynamic(boolean isDynamic) {
		this.isDynamic = isDynamic;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FragmentOutput) {
			FragmentOutput that = (FragmentOutput)obj;
			if(this.stateId == that.stateId) {
				if(this.fragmentId == that.fragmentId) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "state" + stateId + " Frag" + fragmentId ;
	}

}

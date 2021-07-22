package com.crawljax.core.state;

import java.util.List;

import org.w3c.dom.Node;

public class StatePair {
	
	public static enum StateComparision{
		DIFFERENT, DUPLICATE, NEARDUPLICATE1, NEARDUPLICATE2, ERRORCOMPARING
	}
	
	transient StateVertex state1;
	transient StateVertex state2;
	
	
	transient List<Node> state1Nodes;
	transient List<Node> state2Nodes;
	
	StateComparision stateComparision;
	private String state1Name;
	private String state2Name;

	public StatePair(StateVertex state1, StateVertex state2, List<Node> state1Nodes, List<Node> state2Nodes, StateComparision stateComparision) {
		this.state1 = state1;
		this.state2 = state2;
		this.stateComparision = stateComparision;
		this.state1Nodes = state1Nodes;
		this.state2Nodes = state2Nodes;
		this.state1Name = state1.getName();
		this.state2Name = state2.getName();
	}

	public String getState1Name() {
		return state1Name;
	}

	public void setState1Name(String state1Name) {
		this.state1Name = state1Name;
	}

	public String getState2Name() {
		return state2Name;
	}

	public void setState2Name(String state2Name) {
		this.state2Name = state2Name;
	}

	public StateVertex getState1() {
		return state1;
	}

	public void setState1(StateVertex state1) {
		this.state1 = state1;
	}

	public StateVertex getState2() {
		return state2;
	}

	public void setState2(StateVertex state2) {
		this.state2 = state2;
	}

	public List<Node> getState1Nodes() {
		return state1Nodes;
	}

	public void setState1Nodes(List<Node> state1Nodes) {
		this.state1Nodes = state1Nodes;
	}

	public List<Node> getState2Nodes() {
		return state2Nodes;
	}

	public void setState2Nodes(List<Node> state2Nodes) {
		this.state2Nodes = state2Nodes;
	}

	public StateComparision getStateComparision() {
		return stateComparision;
	}

	public void setStateComparision(StateComparision stateComparision) {
		this.stateComparision = stateComparision;
	}
	
	@Override
	public int hashCode() {
		
		return state1.getName().hashCode()*state2.getName().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof StatePair) {
			StatePair other = (StatePair)obj;
			if(	((this.state1.getId() == other.state1.getId()) && (this.state2.getId() == other.getState2().getId()))
			||  ((this.state2.getId() == other.state1.getId()) && (this.state1.getId() == other.getState2().getId()))
			) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public String toString() {
		return state1.getName() + " and " + state2.getName() + " are " + stateComparision;
	}
	
}

package com.crawljax.core.state;
import static java.lang.System.out;
import java.util.Vector;

import org.w3c.dom.Element;

public class SpecificationMetricState {

	private long id;
	private String name;
	private String dom;
	private final String strippedDom;
	private final String url;
	//Synchronized
	private final Vector<CheckedSpecElement> includedElements=new Vector<CheckedSpecElement>();
	private final Vector<CheckedSpecElement> excludedElements=new Vector<CheckedSpecElement>();
	
	/*
	public SpecificationMetricState(long id, String name, String dom, String strippedDom,
            String url) {
	    super();
	    
	    this.id = id;
	    this.name = name;
	    this.dom = dom;
	    this.strippedDom = strippedDom;
	    this.url = url;
    }*/
	
	public SpecificationMetricState(StateVertex stateVertex) {
	    super();
	    this.id = stateVertex.getId();
	    this.name = stateVertex.getName();
	    this.dom = stateVertex.getDom();
	    this.strippedDom = stateVertex.getStrippedDom();
	    this.url = stateVertex.getUrl();
    }
	
	public boolean addIncludedElement(CheckedSpecElement element){
		return includedElements.add(element);
	}
	public boolean addExcludedElement(CheckedSpecElement element){
		return excludedElements.add(element);
	}
	public boolean duplicateLastExludedParent(Element element){
		return excludedElements.add(new CheckedSpecElement(excludedElements.lastElement().getSourceSpecification(),element));
	}
	public void printElements(){
		out.println(name);
		out.println(id);
		//out.println(dom);
		out.println(strippedDom);
		out.println(url);
		out.println("Included Elements: ");
		for(CheckedSpecElement element : includedElements){
			element.printElement();
		}
		out.println("Excluded Elements: ");
		for(CheckedSpecElement element : excludedElements){
			element.printElement();
		}
	}
	public void printReport(){
		out.println("State: " + name);
		out.println(id);
		//out.println(dom);
		out.println(strippedDom);
		out.println(url);
		out.println("Included Elements: ");
		for(CheckedSpecElement element : includedElements){
			element.printElement();
		}
		out.println("Excluded Elements: ");
		for(CheckedSpecElement element : excludedElements){
			element.printElement();
		}
	}
}
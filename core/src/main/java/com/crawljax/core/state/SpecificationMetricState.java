package com.crawljax.core.state;
import static java.lang.System.out;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.w3c.dom.Element;

import com.crawljax.core.TagElement;

public class SpecificationMetricState{

	private long id;
	private String name;
	private String dom;
	private final String strippedDom;
	private final String url;
	/*
	 * ConcurrentLinkedQueue chosen for highest concurrent insertion speed (vs say CopyOnWriteArrayList)
	 * 
	 * addElement() Setup for quickest Write Speed
	 * http://stackoverflow.com/questions/3752194/best-practice-to-use-concurrentmaps-putifabsent
	 * 
	 */
	ConcurrentHashMap<TagElement, ConcurrentLinkedQueue<Element>> checkedElements=new ConcurrentHashMap<TagElement, ConcurrentLinkedQueue<Element>>();
	
	public SpecificationMetricState(StateVertex stateVertex) {
	    super();
	    this.id = stateVertex.getId();
	    this.name = stateVertex.getName();
	    this.dom = stateVertex.getDom();
	    this.strippedDom = stateVertex.getStrippedDom();
	    this.url = stateVertex.getUrl();
    }
	
	public void addElement(TagElement tag, Element element){
		ConcurrentLinkedQueue<Element> elements=checkedElements.get(tag);
		if(elements==null){
			final ConcurrentLinkedQueue<Element> listElements=new ConcurrentLinkedQueue<Element>();
			elements=checkedElements.putIfAbsent(tag, listElements);//utilize return to avoid extra lookup
			if(elements==null)
				elements=listElements;
		}
		elements.add(element);
	}

	public void printState(){
		out.println("\n\n-------------\nState Name:\t"+name);
		out.println("State ID:\t"+ id);
		out.println("State URL:\t"+url);
		//out.println(dom);
		//out.println(strippedDom);
		
	}
	public void printReport(){
		Iterator<Entry<TagElement, ConcurrentLinkedQueue<Element>>> tagIterator= checkedElements.entrySet().iterator();
		while(tagIterator.hasNext()){
			Entry<TagElement, ConcurrentLinkedQueue<Element>> mapEntry=tagIterator.next();
			
			out.println("Source Name:\t"+mapEntry.getKey().getName());
			out.println("Source ID:\t "+mapEntry.getKey().getId());
			
			Iterator<Element> elementIterator=mapEntry.getValue().iterator();
			while(elementIterator.hasNext()){
				Element element=elementIterator.next();	
				out.println("Element Name:\t "+element.getTagName());
				out.println("Element Text:\t "+element.getTextContent());
			}
		}
	}
}
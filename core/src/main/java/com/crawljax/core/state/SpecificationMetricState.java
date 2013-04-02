package com.crawljax.core.state;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.w3c.dom.Element;

import com.crawljax.core.configuration.CrawlElement;

public class SpecificationMetricState{

	private final long id;
	private final String name;
	private final String dom;
	private final String strippedDom;
	private final String url;
	/*
	 * ConcurrentLinkedQueue chosen for highest concurrent insertion speed (vs say CopyOnWriteArrayList)
	 * 
	 * addElement() Setup for quickest Write Speed
	 * http://stackoverflow.com/questions/3752194/best-practice-to-use-concurrentmaps-putifabsent
	 * 
	 */
	private final ConcurrentHashMap<CrawlElement, ConcurrentLinkedQueue<Element>> checkedElements=new ConcurrentHashMap<CrawlElement, ConcurrentLinkedQueue<Element>>();
	
	public SpecificationMetricState(StateVertex stateVertex) {
	    super();
	    this.id = stateVertex.getId();
	    this.name = stateVertex.getName();
	    this.dom = stateVertex.getDom();
	    this.strippedDom = stateVertex.getStrippedDom();
	    this.url = stateVertex.getUrl();
    }
	
	public void addElement(CrawlElement crawlElement, Element element){
		ConcurrentLinkedQueue<Element> elements=checkedElements.get(crawlElement);
		if(elements==null){
			final ConcurrentLinkedQueue<Element> listElements=new ConcurrentLinkedQueue<Element>();
			elements=checkedElements.putIfAbsent(crawlElement, listElements);//utilize return to avoid extra lookup
			if(elements==null)
				elements=listElements;
		}
		elements.add(element);
	}
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDom() {
		return dom;
	}

	public String getStrippedDom() {
		return strippedDom;
	}

	public String getUrl() {
		return url;
	}

	public ConcurrentHashMap<CrawlElement, ConcurrentLinkedQueue<Element>> getCheckedElements() {
		return checkedElements;
	}
}
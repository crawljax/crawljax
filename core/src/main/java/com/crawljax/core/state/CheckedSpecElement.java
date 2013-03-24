package com.crawljax.core.state;

import com.crawljax.core.TagElement;
import org.w3c.dom.Element;
import static java.lang.System.out;

public class CheckedSpecElement {

	private final TagElement sourceSpecification;
	private final Element element;
	
	public CheckedSpecElement(TagElement sourceSpecification, Element element) {
	    super();
	    this.sourceSpecification = sourceSpecification;
	    this.element = element;
    }
	
	public void printElement(){
		out.println("Source Name:\t"+sourceSpecification.getName());
		out.println("Source ID:\t "+sourceSpecification.getId());
		out.println("Element Name:\t "+element.getTagName());
		out.println("Element Text:\t "+element.getTextContent());
	}
	public TagElement getSourceSpecification(){
		return sourceSpecification;
	}
}

package ca.ubc.eece310.groupL2C1;
import static java.lang.System.out;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.TagElement;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.SpecificationMetricState;
import com.google.common.base.Preconditions;


public class Specification_Metrics_Plugin implements PostCrawlingPlugin, GeneratesOutput {
	private static final Logger LOG = LoggerFactory.getLogger(Specification_Metrics_Plugin.class);
	private String absoluteOutputPath;
	private ConcurrentLinkedQueue<SpecificationMetricState> includedSpecsChecked;
	private ConcurrentLinkedQueue<SpecificationMetricState> excludedSpecsChecked;

	public Specification_Metrics_Plugin(File outputFolder) {
		Preconditions.checkNotNull(outputFolder, "Output folder cannot be null");
		LOG.info("Initialized Specification_Metrics_Plugin");
	}
	
	@Override
    public void postCrawling(CrawlSession session) {
		//TODO Pull Data from session?
		includedSpecsChecked = CandidateElementExtractor.includedSpecsChecked;
		excludedSpecsChecked = CandidateElementExtractor.excludedSpecsChecked;
		
		//OUTPUT THE DATA!
		printComprehensiveReport();
    }
	@Override
    public void setOutputFolder(String absolutePath) {
	    absoluteOutputPath=absolutePath;
    }
	@Override
    public String getOutputFolder() {
	    // TODO Auto-generated method stub
	    return null;
    }
	private void printOverallStatistics(){
		Iterator<SpecificationMetricState> includedSpecIterator=includedSpecsChecked.iterator();
		Iterator<SpecificationMetricState> excludedSpecIterator=excludedSpecsChecked.iterator();
		ArrayList<HashMap<TagElement, Integer>> includedSpecTagCount= new ArrayList<HashMap<TagElement, Integer>>();
		ArrayList<HashMap<TagElement, Integer>> excludedSpecTagCount= new ArrayList<HashMap<TagElement, Integer>>();
		
		
		SpecificationMetricState tempState;
		
		while(includedSpecIterator.hasNext()){
			tempState=includedSpecIterator.next();
			HashMap<TagElement, Integer> singleStateIncludedTagCount=new HashMap<TagElement, Integer>();
			
			Iterator<Entry<TagElement, ConcurrentLinkedQueue<Element>>> tagIterator= tempState.getCheckedElements().entrySet().iterator();
			while(tagIterator.hasNext()){
				Entry<TagElement, ConcurrentLinkedQueue<Element>> mapEntry=tagIterator.next();
				singleStateIncludedTagCount.put(mapEntry.getKey(), mapEntry.getValue().size());
			}
			includedSpecTagCount.add(singleStateIncludedTagCount);
		}
		//Duplicate for excluded
		//Output Totals for each tag for eachState
		//Output Overall total for each tag
	}
	private void printComprehensiveReport(){
		SpecificationMetricState state;
		Iterator<SpecificationMetricState> includedSpecIterator=includedSpecsChecked.iterator();
		Iterator<SpecificationMetricState> excludedSpecIterator=excludedSpecsChecked.iterator();
		while(includedSpecIterator.hasNext() || excludedSpecIterator.hasNext()){
			state=includedSpecIterator.next();
			printStateHeader(state);
			out.println("\nIncluded Tags and the Elements they matched:");
			printStateElements(state);
			state=excludedSpecIterator.next();
			out.println("\nExcluded Tags and the Elements they matched:");
			printStateElements(state);
		}
	}
	private void printStateHeader(SpecificationMetricState state){
		out.println("\n\n-------------\nState Name:\t"+state.getName());
		out.println("State ID:\t"+ state.getId());
		out.println("State URL:\t"+state.getUrl());
		//out.println(state.getDom());
		//out.println(state.getStrippedDom());
	}
	
	private void printStateElements(SpecificationMetricState state){
		Iterator<Entry<TagElement, ConcurrentLinkedQueue<Element>>> tagIterator= state.getCheckedElements().entrySet().iterator();
		while(tagIterator.hasNext()){
			Entry<TagElement, ConcurrentLinkedQueue<Element>> mapEntry=tagIterator.next();
			
			out.println("Source Name:\t"+mapEntry.getKey().getName());
			out.println("Source ID:\t "+mapEntry.getKey().getId());
			
			Iterator<Element> elementIterator=mapEntry.getValue().iterator();
			while(elementIterator.hasNext()){
				Element element=elementIterator.next();	
				out.println("Element Tag Name:\t "+element.getTagName());
				out.println("Element Text:\t "+element.getTextContent());
				Node node;
				for(int i=0;i<element.getAttributes().getLength();i++){
					node=element.getAttributes().item(i);
					out.println("\tNode "+i);
					out.println("\t\tName:\t "+node.getNodeName());
					out.println("\t\tType:\t "+nodeTypeLookUp(node.getNodeType()));
					out.println("\t\tValue:\t "+node.getNodeValue());
					//out.println("\tNode Base URI:\t "+node.getBaseURI()); //Null on Google
					//out.println("\tNode Namespace URI:\t "+node.getNamespaceURI()); //Null on Google
					//out.println("\tNode Text:\t "+node.getTextContent());//Same as Value for Google
				}
			}
		}
	}
	private String nodeTypeLookUp(int type){
		switch (type){
			case Node.ATTRIBUTE_NODE:
				return "Attribute Node";
			case Node.CDATA_SECTION_NODE:
				return "CData Section Node";
			case Node.COMMENT_NODE:
				return "Comment Node";
			case Node.DOCUMENT_FRAGMENT_NODE:
				return "Document Fragment Node";
			case Node.DOCUMENT_NODE:
				return "Document Node";
			case Node.DOCUMENT_POSITION_CONTAINED_BY:
				return "Document Position Node";
			case Node.DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC:
				return "Document Position Node";
			case Node.ELEMENT_NODE:
				return "Element Node";
			case Node.ENTITY_NODE:
				return "Entity Node";
			case Node.ENTITY_REFERENCE_NODE:
				return "Entity Reference Node";
			case Node.NOTATION_NODE:
				return "Notation Node";
			case Node.PROCESSING_INSTRUCTION_NODE:
				return "Processing Instruction Node";
			case Node.TEXT_NODE:
				return "Text Node";
			default:
				return "Unknown Node Type";
		}
	}

}

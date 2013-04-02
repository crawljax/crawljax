package ca.ubc.eece310.groupL2C1;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import com.crawljax.core.configuration.CrawlElement;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.SpecificationMetricState;
import com.google.common.base.Preconditions;


public class Specification_Metrics_Plugin implements PostCrawlingPlugin {
	private static final Logger LOG = LoggerFactory.getLogger(Specification_Metrics_Plugin.class);
	private ConcurrentLinkedQueue<SpecificationMetricState> includedSpecsChecked;
	private ConcurrentLinkedQueue<SpecificationMetricState> excludedSpecsChecked;
	private BufferedWriter outputWriter; 

	public Specification_Metrics_Plugin(File outputFolder) {
		Preconditions.checkNotNull(outputFolder, "Output folder cannot be null");
		LOG.info("Initialized Specification_Metrics_Plugin");
		try {
			FileWriter fileWrite = new FileWriter("specification_metric_plugin.txt");
			outputWriter = new BufferedWriter(fileWrite);
			//outputWriter.write("Hello Java");
			//outputWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	@Override
    public void postCrawling(CrawlSession session) {
		includedSpecsChecked = CandidateElementExtractor.getIncludedSpecsChecked();
		excludedSpecsChecked = CandidateElementExtractor.getExcludedSpecsChecked();
		
		//OUTPUT THE DATA!
		printOverallStatistics();
		printComprehensiveReport();
		
    }
	private void printOverallStatistics(){
		Iterator<SpecificationMetricState> includedSpecIterator=includedSpecsChecked.iterator();
		Iterator<SpecificationMetricState> excludedSpecIterator=excludedSpecsChecked.iterator();
		ArrayList<HashMap<CrawlElement, Integer>> includedSpecTagCount= new ArrayList<HashMap<CrawlElement, Integer>>();
		ArrayList<HashMap<CrawlElement, Integer>> excludedSpecTagCount= new ArrayList<HashMap<CrawlElement, Integer>>();
		
		
		SpecificationMetricState tempState;
		
		try {
			outputWriter.write("--Included Checked Elements--");
			outputWriter.newLine();
			outputWriter.newLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while(includedSpecIterator.hasNext()){
			tempState=includedSpecIterator.next();
			HashMap<CrawlElement, Integer> singleStateIncludedTagCount=new HashMap<CrawlElement, Integer>();
			
			Iterator<Entry<CrawlElement, ConcurrentLinkedQueue<Element>>> tagIterator= tempState.getCheckedElements().entrySet().iterator();
			while(tagIterator.hasNext()){
				Entry<CrawlElement, ConcurrentLinkedQueue<Element>> mapEntry=tagIterator.next();
				singleStateIncludedTagCount.put(mapEntry.getKey(), mapEntry.getValue().size());
				try {
					outputWriter.write(mapEntry.getKey() + ":");
					for (int i = mapEntry.getKey().toString().length() ; i < 20 ; i++){
						outputWriter.write(" ");
					}
					outputWriter.write("" + mapEntry.getValue().size());
					outputWriter.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			includedSpecTagCount.add(singleStateIncludedTagCount);
		}
		
		try {
			outputWriter.newLine();
			outputWriter.newLine();
			outputWriter.write("--Excluded Checked Elements--");
			outputWriter.newLine();
			outputWriter.newLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while(excludedSpecIterator.hasNext()){
			tempState=excludedSpecIterator.next();
			HashMap<CrawlElement, Integer> singleStateExcludedTagCount=new HashMap<CrawlElement, Integer>();
			
			Iterator<Entry<CrawlElement, ConcurrentLinkedQueue<Element>>> tagIterator= tempState.getCheckedElements().entrySet().iterator();
			while(tagIterator.hasNext()){
				Entry<CrawlElement, ConcurrentLinkedQueue<Element>> mapEntry=tagIterator.next();
				singleStateExcludedTagCount.put(mapEntry.getKey(), mapEntry.getValue().size());
				try {
					outputWriter.write(mapEntry.getKey() + ":");
					for (int i = mapEntry.getKey().toString().length() ; i < 20 ; i++){
						outputWriter.write(" ");
					}
					outputWriter.write("" + mapEntry.getValue().size());
					outputWriter.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			excludedSpecTagCount.add(singleStateExcludedTagCount);
		}
	}
	private void printComprehensiveReport(){
		SpecificationMetricState state;
		Iterator<SpecificationMetricState> includedSpecIterator=includedSpecsChecked.iterator();
		Iterator<SpecificationMetricState> excludedSpecIterator=excludedSpecsChecked.iterator();
		while(includedSpecIterator.hasNext() || excludedSpecIterator.hasNext()){
			state=includedSpecIterator.next();
			printStateHeader(state);
			try {
				outputWriter.newLine();
				outputWriter.write("Included Tags and the Elements they matched:");
				outputWriter.newLine();
				printStateElements(state); 
				state=excludedSpecIterator.next();
				outputWriter.newLine();
				outputWriter.write("Excluded Tags and the Elements they matched:");
				outputWriter.newLine();
				printStateElements(state);
				outputWriter.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void printStateHeader(SpecificationMetricState state){
		try {
			outputWriter.newLine();
			outputWriter.newLine();
			outputWriter.write("-------------\nState Name:\t"+state.getName());
			outputWriter.newLine();
			outputWriter.write("State ID:\t"+ state.getId());
			outputWriter.write("State URL:\t"+state.getUrl());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void printStateElements(SpecificationMetricState state){
	
		Iterator<Entry<CrawlElement, ConcurrentLinkedQueue<Element>>> tagIterator= state.getCheckedElements().entrySet().iterator();
		while(tagIterator.hasNext()){
			Entry<CrawlElement, ConcurrentLinkedQueue<Element>> mapEntry=tagIterator.next();
			try {
				outputWriter.newLine();
				outputWriter.write("Source Name:\t"+mapEntry.getKey().getTagName());
				outputWriter.newLine();
				outputWriter.write("Source ID:\t "+mapEntry.getKey().getId());
				
				Iterator<Element> elementIterator=mapEntry.getValue().iterator();
				while(elementIterator.hasNext()){
					Element element=elementIterator.next();	
					outputWriter.newLine();
					outputWriter.newLine();
					outputWriter.write("Element Tag Name:\t "+element.getTagName());
					outputWriter.newLine();
					outputWriter.write("Element Text:\t "+element.getTextContent());
					Node node;
					for(int i=0;i<element.getAttributes().getLength();i++){
						node=element.getAttributes().item(i);
						outputWriter.newLine();
						outputWriter.write("\tNode "+i);
						outputWriter.newLine();
						outputWriter.write("\t\tName:\t "+node.getNodeName());
						outputWriter.newLine();
						outputWriter.write("\t\tType:\t "+nodeTypeLookUp(node.getNodeType()));
						outputWriter.newLine();
						outputWriter.write("\t\tValue:\t "+node.getNodeValue());
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
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

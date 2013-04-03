package ca.ubc.eece310.groupL2C1;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.SpecificationMetricState;
import com.google.common.base.Preconditions;


public class Specification_Metrics_Plugin implements PostCrawlingPlugin, GeneratesOutput {
	private static final Logger LOG = LoggerFactory.getLogger(Specification_Metrics_Plugin.class);
	private String outputFolderAbsolutePath;
	private ConcurrentLinkedQueue<SpecificationMetricState> includedSpecsChecked;
	private ConcurrentLinkedQueue<SpecificationMetricState> excludedSpecsChecked;
	private BufferedWriter outputWriter; 

	public Specification_Metrics_Plugin() {
		LOG.info("Initialized Specification_Metrics_Plugin");
	}
	
	@Override
    public void postCrawling(CrawlSession session) {
		includedSpecsChecked = CandidateElementExtractor.getIncludedSpecsChecked();
		excludedSpecsChecked = CandidateElementExtractor.getExcludedSpecsChecked();
		
		Preconditions.checkNotNull(outputFolderAbsolutePath, "Output folder cannot be null");
		try {
			File outputDir=new File(outputFolderAbsolutePath);
			if(!outputDir.exists() || !outputDir.isDirectory()){
				if(!outputDir.mkdir()){
				    throw new IllegalStateException("Couldn't create dir: " + outputDir);
				}
			}
			FileWriter fileWrite = new FileWriter(outputDir.getAbsolutePath()+File.separator+"specification_metric_plugin.txt");
			outputWriter = new BufferedWriter(fileWrite);
			
			//OUTPUT THE DATA!
			printComprehensiveReport();
			
			try {
		        outputWriter.close();
	        } catch (IOException e) {
	        	LOG.error("Couldn't close ouputWriter");
	        }
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error("Unable to Write Specification Metrics Output");
		}
		
    }
	
	private void printComprehensiveReport(){
		HashMap<CrawlElement, Integer> includedCrawlElementCount= new HashMap<CrawlElement, Integer>();
		HashMap<CrawlElement, Integer> excludedCrawlElementCount= new HashMap<CrawlElement, Integer>();	
		printIndividualStateCrawlStatistics(includedCrawlElementCount, excludedCrawlElementCount);
		printOverallCrawlStatistics(includedCrawlElementCount, excludedCrawlElementCount);
	}
	
	/*
	 * Depends on printIndividualStateCrawlStatistics(...) to generate CrawlElementCounts
	 */
	private void printOverallCrawlStatistics(HashMap<CrawlElement, Integer> includedCrawlElementCount, HashMap<CrawlElement, Integer> excludedCrawlElementCount){
		printReportHeader("Entire Crawl: CrawlElement Match Count");
		
		Iterator<Entry<CrawlElement, Integer>> crawlElementCountIterator;
		for(int i=0; i<2; i++){
			try {
				outputWriter.newLine();
				if(i==0){
					outputWriter.write("--Total Matches: Included CrawlElements--");
					crawlElementCountIterator=includedCrawlElementCount.entrySet().iterator();
				}else{
					outputWriter.write("--Total Matches: Excluded CrawlElements--");
					crawlElementCountIterator=excludedCrawlElementCount.entrySet().iterator();
				}
				while(crawlElementCountIterator.hasNext()){
					Entry<CrawlElement, Integer> crawlElementMapEntry=crawlElementCountIterator.next();	
					outputWriter.newLine();
					outputWriter.write(crawlElementMapEntry.getKey().toString());
					outputWriter.newLine();
					outputWriter.write("Matches: " + crawlElementMapEntry.getValue());
					outputWriter.newLine();
				}
			}catch (IOException e) {
				LOG.error("Couldn't write specification metrics output");
			}
		}
	}
	private void printIndividualStateCrawlStatistics(HashMap<CrawlElement, Integer> includedCrawlElementCount, HashMap<CrawlElement, Integer> excludedCrawlElementCount){
		SpecificationMetricState includedState;
		SpecificationMetricState excludedState;
		Iterator<SpecificationMetricState> includedSpecIterator=includedSpecsChecked.iterator();
		Iterator<SpecificationMetricState> excludedSpecIterator=excludedSpecsChecked.iterator();
		
		printReportHeader("Individual State: CrawlElement Matches");
		while(includedSpecIterator.hasNext() || excludedSpecIterator.hasNext()){
			includedState=includedSpecIterator.next();
			excludedState=excludedSpecIterator.next();
			
			if(includedState!=null){
				printStateHeader(includedState);
				printStateElements(includedState, "--Included CrawlElements and the DOM Elements they matched--"); 
				printStateElementsSummary(includedState, includedCrawlElementCount, "--Included CrawlElements Summary--");
			}

			if(excludedState!=null){
				if(includedState==null)	{		
					printStateHeader(excludedState);
				}
				printStateElements(excludedState,"--Excluded CrawlElements and the DOM Elements they matched--"); 
				printStateElementsSummary(excludedState, excludedCrawlElementCount, "--Excluded CrawlElements Summary--");
			}
		}
	}
	
	private void printTitle(String title){
		try {
			outputWriter.newLine();
			outputWriter.write(title);
			outputWriter.newLine();
		} catch (IOException e) {
			LOG.error("Couldn't write specification metrics output");
		}
	}
	
	private void printStateElementsSummary(SpecificationMetricState specificationMetricState, HashMap<CrawlElement, Integer> crawlElementCount, String title){
		printTitle(title);
		
		Iterator<Entry<CrawlElement, ConcurrentLinkedQueue<Element>>> crawlElementsIterator= specificationMetricState.getCheckedElements().entrySet().iterator();			
		while(crawlElementsIterator.hasNext()){
			Entry<CrawlElement, ConcurrentLinkedQueue<Element>> crawlElementMapEntry=crawlElementsIterator.next();	
			addToCrawlElementCount(crawlElementCount, crawlElementMapEntry);
			printCrawlElementSummary(crawlElementMapEntry);
		}
	}
	
	private void printReportHeader(String title){
		try {
	        outputWriter.newLine();
	        outputWriter.newLine();
	        outputWriter.newLine();
	        outputWriter.newLine();
	        outputWriter.newLine();
			outputWriter.write("-------------------------------");
	        outputWriter.newLine();
			outputWriter.write(title);
	        outputWriter.newLine();
			outputWriter.write("-------------------------------");
	        outputWriter.newLine();
        } catch (IOException e) {
			LOG.error("Couldn't write specification metrics output");
        }
	}
	
	private void printStateHeader(SpecificationMetricState state){
		try {
			outputWriter.newLine();
			outputWriter.write("-------------");
			outputWriter.newLine();
			outputWriter.write("State Name:\t"+state.getName());
			outputWriter.newLine();
			outputWriter.write("State ID:\t"+ state.getId());
			outputWriter.newLine();
			outputWriter.write("State URL:\t"+state.getUrl());
			outputWriter.newLine();
			outputWriter.write("-------------");
			outputWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void printStateElements(SpecificationMetricState state, String title){
		printTitle(title);
		
		Iterator<Entry<CrawlElement, ConcurrentLinkedQueue<Element>>> crawlElementsIterator= state.getCheckedElements().entrySet().iterator();
		while(crawlElementsIterator.hasNext()){
			Entry<CrawlElement, ConcurrentLinkedQueue<Element>> mapEntry=crawlElementsIterator.next();
			try {
				outputWriter.newLine();
				outputWriter.write("Crawl Element Name:\t"+mapEntry.getKey().getTagName());
				outputWriter.newLine();
				outputWriter.write("Crawl Element ID:\t "+mapEntry.getKey().getId());	
				outputWriter.newLine();
				
				Iterator<Element> domElementIterator=mapEntry.getValue().iterator();
				while(domElementIterator.hasNext()){
					Element element=domElementIterator.next();
					outputWriter.newLine();
					outputWriter.write("DOM Element Tag Name:\t "+element.getTagName());
					outputWriter.newLine();
					outputWriter.write("DOM Element Text:\t "+element.getTextContent());
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
						outputWriter.newLine();
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void addToCrawlElementCount(HashMap<CrawlElement, Integer> crawlElementCount, Entry<CrawlElement, ConcurrentLinkedQueue<Element>> crawlElementMapEntry){
		Integer count=crawlElementCount.get(crawlElementMapEntry.getKey());
		if(count==null){
			crawlElementCount.put(crawlElementMapEntry.getKey(), crawlElementMapEntry.getValue().size());
		}else{
			crawlElementCount.put(crawlElementMapEntry.getKey(), count+crawlElementMapEntry.getValue().size());
		}
	}
	
	private void printCrawlElementSummary(Entry<CrawlElement, ConcurrentLinkedQueue<Element>> crawlElementMapEntry){
		try {
			outputWriter.newLine();
			outputWriter.write(crawlElementMapEntry.getKey().toString());
			outputWriter.newLine();
			outputWriter.write("Matches: " + crawlElementMapEntry.getValue().size());
			outputWriter.newLine();
		} catch (IOException e) {
			LOG.error("Couldn't write specification metrics output");
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
	@Override
    public void setOutputFolder(String absolutePath) {
	    outputFolderAbsolutePath=absolutePath;	    
    }
	@Override
    public String getOutputFolder() {
	   return outputFolderAbsolutePath;
    }

}

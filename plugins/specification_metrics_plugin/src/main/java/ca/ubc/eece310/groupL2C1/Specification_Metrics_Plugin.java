package ca.ubc.eece310.groupL2C1;
import static java.lang.System.out;

import java.io.File;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.GeneratesOutput;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.state.SpecificationMetricState;
import com.google.common.base.Preconditions;


public class Specification_Metrics_Plugin implements PostCrawlingPlugin, GeneratesOutput {
	private static final Logger LOG = LoggerFactory.getLogger(Specification_Metrics_Plugin.class);

	
	public Specification_Metrics_Plugin(File outputFolder) {
		Preconditions.checkNotNull(outputFolder, "Output folder cannot be null");
		LOG.info("Initialized Specification_Metrics_Plugin");
	}
	
	
	@Override
    public void postCrawling(CrawlSession session) {
		//TODO Pull Data from session?
		
		//OUTPUT ALL THE DATA!
		SpecificationMetricState state;
		Iterator<SpecificationMetricState> includedIt=CandidateElementExtractor.includedSpecsChecked.iterator();
		Iterator<SpecificationMetricState> excludedIt=CandidateElementExtractor.excludedSpecsChecked.iterator();
		while(includedIt.hasNext() || excludedIt.hasNext()){
			state=includedIt.next();
			state.printState();
			out.println("\nIncluded Tags and the Elements they matched:");
			state.printReport();
			state=excludedIt.next();
			out.println("\nExcluded Tags and the Elements they matched:");
			state.printReport();
		}
    }
	@Override
    public void setOutputFolder(String absolutePath) {
	    // TODO Auto-generated method stub
	    
    }
	@Override
    public String getOutputFolder() {
	    // TODO Auto-generated method stub
	    return null;
    }
	

}

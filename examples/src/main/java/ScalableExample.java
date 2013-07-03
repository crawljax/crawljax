
import java.util.concurrent.TimeUnit;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateFlowGraph.StateFlowGraphType;

/**
 * Crawls our demo site with the database backed stateFlowGraph implementation. This will save the
 * state flow graph in a database.
 */
public class ScalableExample {

	/**
	 * Run this method to start the crawl.
	 */
	public static void main(String[] args) {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor("http://demo.crawljax.com/");
		builder.setGraphType(StateFlowGraphType.SCALABLE);

		builder.crawlRules().clickOnce(false);
		builder.setMaximumRunTime(100, TimeUnit.MINUTES);
		CrawljaxRunner crawljax =
		        new CrawljaxRunner(builder.build());
		crawljax.call();
	}
}

package com.crawljax.examples;

import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.RelationshipIndex;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.StateFlowGraph.StateFlowGraphType;

/**
 * Crawls our demo site with the default configuration. The crawl will log what it's doing but will
 * not produce any output.
 */
public class CorrectnessExperiment {

	public static String DB_PATH;
	public static Node root;
	public static RelationshipIndex edgeIndex;
	public static Index<Node> nodeIndex;

	/**
	 * Run this method to start the crawl.
	 */
	public static void main(String[] args) {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor("http://demo.crawljax.com/");
		builder.setGraphType(StateFlowGraphType.SCALABLE);

		DB_Path_Getter path_Getter = new DB_Path_Getter();
		builder.addPlugin(path_Getter);
		builder.crawlRules().clickOnce(false);
		builder.setMaximumRunTime(1, TimeUnit.MINUTES);
		CrawljaxRunner crawljax =
		        new CrawljaxRunner(builder.build());
		crawljax.call();

		// running with default crawljax
		builder =
		        CrawljaxConfiguration.builderFor("http://demo.crawljax.com/");
		builder.setGraphType(StateFlowGraphType.DEAFAULT);

		builder.addPlugin(path_Getter);
		builder.crawlRules().clickOnce(false);
		builder.setMaximumRunTime(1, TimeUnit.MINUTES);
		crawljax =
		        new CrawljaxRunner(builder.build());
		crawljax.call();

	}

}

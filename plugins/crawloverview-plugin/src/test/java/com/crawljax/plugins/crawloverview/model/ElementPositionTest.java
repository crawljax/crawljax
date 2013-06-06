package com.crawljax.plugins.crawloverview.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.PostCrawlStateGraphChecker;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateVertex;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.crawljax.rules.TempDirInTargetFolder;

public class ElementPositionTest {

	@Rule
	public final TempDirInTargetFolder tmp = new TempDirInTargetFolder("position", true);

	@Test
	public void setup() {
		CrawljaxConfigurationBuilder config =
		        CrawljaxConfiguration.builderFor("http://crawljax.com")
		                .addPlugin(new CrawlOverview(tmp.getTempDir()))
		                .setMaximumStates(20)
		                .setUnlimitedCrawlDepth()
		                .setBrowserConfig(new BrowserConfiguration(BrowserType.firefox, 4));
		config.crawlRules().dontClickChildrenOf("IFRAME").withClass("twitter-timeline");
		config.crawlRules().dontClickChildrenOf("DIV").withId("disqus_thread");
		config.crawlRules().clickOnce(false);
		CrawlSession runner = new CrawljaxRunner(config.build()).call();
		StateFlowGraph graph = runner.getStateFlowGraph();
		System.out.println("Edges " + graph.getAllEdges().size());
		System.out.println("States" + graph.getAllStates().size());

		Map<String, Eventable> edges = new HashMap<>();
		for (StateVertex vertex : graph.getAllStates()) {
			StringBuilder builder = new StringBuilder(vertex.getName()).append("\nincoming: ");
			for (Eventable incoming : graph.getIncomingClickable(vertex)) {
				edges.put(stringKey(incoming), incoming);
				builder.append(incoming.getSourceStateVertex().getName()).append(", ");
			}
			builder.append("\noutgoing: ");
			for (Eventable incoming : graph.getOutgoingClickables(vertex)) {
				edges.put(stringKey(incoming), incoming);
				builder.append(incoming.getTargetStateVertex().getName()).append(", ");
			}
			System.out.println(builder.toString());
		}

		System.out.println("SFG=" + graph.getAllEdges().size() + " found=" + edges.size());
		Collection<Eventable> found = edges.values();
		for (Eventable eventable : graph.getAllEdges()) {
			List<Eventable> occur = new ArrayList<>();
			for (Eventable eventFound : found) {
				if (eventable.equals(eventFound) || eventable.hashCode() == eventFound.hashCode()) {
					occur.add(eventFound);
				}
			}
			if (occur.size() > 1) {
				System.out.println("Duplicates! " + occur.size());
				for (Eventable dup : occur) {
					String out = "Hash=" + (eventable.hashCode() == dup.hashCode())
					        + " equals=" + eventable.equals(dup)
					        + " myKey=" + stringKey(dup)
					        + " toString=" + dup.toString();
					System.out.println(out);
				}
			}
		}

		new PostCrawlStateGraphChecker().postCrawling(runner, ExitStatus.EXHAUSTED);
	}

	private String stringKey(Eventable incoming) {
		return incoming.getSourceStateVertex().getName() + "-"
		        + incoming.getTargetStateVertex().getName();
	}
}

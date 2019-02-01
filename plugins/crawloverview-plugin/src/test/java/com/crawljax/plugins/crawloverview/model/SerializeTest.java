package com.crawljax.plugins.crawloverview.model;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import com.crawljax.condition.RegexCondition;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.oraclecomparator.OracleComparator;
import com.crawljax.oraclecomparator.comparators.SimpleComparator;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class SerializeTest {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Test
	public void testSerializability() throws IOException {
		OutPutModel model = createModel();
		String json = Serializer.toPrettyJson(model);
		OutPutModel deserialized = Serializer.read(json);
		assertThat(deserialized.getStates(), is(model.getStates()));
		assertThat(deserialized, is(model));
	}

	private OutPutModel createModel() throws IOException {
		ImmutableList<CandidateElementPosition> candidateElements =
		        ImmutableList.of(new CandidateElementPosition("a/b/c", new Point(1, 2),
		                new Dimension(3, 4)));
		State state1 =
		        new State("state1", "http://example.com/a", candidateElements, 1, 1, 1,
		                ImmutableList.of("failedEvent1"), false, null, 0, 0, 0);
		State state2 =
		        new State("state2", "http://example.com/b", candidateElements, 1, 1, 1,
		                ImmutableList.of("failedEvent2"), false, null, 0, 0, 0);
		ImmutableMap<String, State> states =
		        ImmutableMap.of(state1.getName(), state1, state2.getName(), state2);
		ImmutableList<Edge> edges =
		        ImmutableList.of(new Edge(state1.getName(), state2.getName(), 1, "the link",
		                "id1", "A", "click"));
		return new OutPutModel(states, edges, newStatistics(states.values()),
		        ExitStatus.EXHAUSTED, null);
	}

	private Statistics newStatistics(Collection<State> states) {
		StateStatistics stateStats = new StateStatistics(states);
		return new Statistics("1 hour", 1, "2KB", 1, new Date(), stateStats, 2);
	}

	@Test
	public void testConfigSerializibility() throws IOException {
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor("http://example.com")
		                .addPlugin(new CrawlOverview())
		                .setOutputDirectory(tmpFolder.getRoot());

		builder.crawlRules().addCrawlCondition(
		        new CrawlCondition("kers", new RegexCondition("test")));

		builder.crawlRules().addOracleComparator(
		        new OracleComparator("tes", new SimpleComparator()));

		Serializer.toPrettyJson(builder.build());
	}
}

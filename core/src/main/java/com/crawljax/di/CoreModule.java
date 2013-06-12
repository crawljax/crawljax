package com.crawljax.di;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CandidateElementManager;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.ExitNotifier;
import com.crawljax.core.ExtractorManager;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.InMemoryStateFlowGraph;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.forms.FormHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CoreModule extends AbstractModule {

	private static final Logger LOG = LoggerFactory.getLogger(CoreModule.class);
	private CrawljaxConfiguration configuration;

	public CoreModule(CrawljaxConfiguration config) {
		this.configuration = config;
	}

	@Override
	protected void configure() {
		LOG.debug("Configuring the core module");

		install(new ConfigurationModule(configuration));

		bind(ExitNotifier.class).toInstance(new ExitNotifier(configuration.getMaximumStates()));

		bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());

		bind(CrawlSession.class).toProvider(CrawlSessionProvider.class);

		bind(ExtractorManager.class).to(CandidateElementManager.class);

		bind(StateFlowGraph.class).to(InMemoryStateFlowGraph.class);
		bind(InMemoryStateFlowGraph.class).in(Singleton.class);

		install(new FactoryModuleBuilder().build(FormHandlerFactory.class));
		install(new FactoryModuleBuilder().build(CandidateElementExtractorFactory.class));

	}

	@Provides
	ConditionTypeChecker<CrawlCondition> crawlConditionChecker() {
		return new ConditionTypeChecker<>(configuration.getCrawlRules().getPreCrawlConfig()
		        .getCrawlConditions());
	}

	public interface FormHandlerFactory {
		FormHandler newFormHandler(EmbeddedBrowser browser);
	}

	public interface CandidateElementExtractorFactory {
		CandidateElementExtractor newExtractor(EmbeddedBrowser browser);
	}
}

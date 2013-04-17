package com.crawljax.di;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.ConditionTypeChecker;
import com.crawljax.condition.crawlcondition.CrawlCondition;
import com.crawljax.core.CandidateElementExtractor;
import com.crawljax.core.CandidateElementManager;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlTaskConsumer;
import com.crawljax.core.ExtractorManager;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.forms.FormHandler;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
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

		bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());

		bind(CountDownLatch.class).annotatedWith(ConsumersDoneLatch.class).toInstance(
		        new CountDownLatch(1));

		bind(CrawlSession.class).toProvider(CrawlSessionProvider.class);

		bind(ExtractorManager.class).to(CandidateElementManager.class);

		install(new FactoryModuleBuilder().build(FormHandlerFactory.class));
		install(new FactoryModuleBuilder().build(CandidateElementExtractor.class));

	}

	@Provides
	ConditionTypeChecker<CrawlCondition> crawlConditionChecker() {
		return new ConditionTypeChecker<>(configuration.getCrawlRules().getPreCrawlConfig()
		        .getCrawlConditions());
	}

	@Provides
	StateFlowGraph flowGraph(CrawlSessionProvider sessionProvider) {
		return sessionProvider.get().getStateFlowGraph();
	}

	/**
	 * This latch is 0 when all {@link CrawlTaskConsumer}s have finished their jobs implying the
	 * Crawl is done.
	 */
	@BindingAnnotation
	@Target({ FIELD, PARAMETER, METHOD })
	@Retention(RUNTIME)
	public @interface ConsumersDoneLatch {
	}

	public interface FormHandlerFactory {
		FormHandler newFormHandler(EmbeddedBrowser browser);
	}

	public interface CandidateElementExtractorFactory {
		CandidateElementExtractor newExtractor(EmbeddedBrowser browser);
	}
}

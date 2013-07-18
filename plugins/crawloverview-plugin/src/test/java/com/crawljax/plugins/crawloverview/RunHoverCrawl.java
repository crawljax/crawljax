package com.crawljax.plugins.crawloverview;

import java.io.File;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.rules.ExternalResource;
import org.slf4j.LoggerFactory;

import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.test.BaseCrawler;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Rule that runs the crawl of the hover site. The result works via a {@link Future} so the crawl
 * only runs once.
 */
public class RunHoverCrawl extends ExternalResource {

	private static final Supplier<OutPutModel> CRAWL_TASK = Suppliers
	        .memoize(new Supplier<OutPutModel>() {

		        @Override
		        public OutPutModel get() {
			        LoggerFactory.getLogger(RunHoverCrawl.class).info(
			                "Running the hover crawl");
			        Resource hoverSiteBase =
			                Resource.newClassPathResource("hover-test-site");
			        BaseCrawler hoverSiteCrawl = new BaseCrawler(hoverSiteBase, "") {
				        @Override
				        protected CrawljaxConfigurationBuilder newCrawlConfigurationBuilder() {
					        CrawljaxConfigurationBuilder builder =
					                super.newCrawlConfigurationBuilder().setOutputDirectory(
					                        getTempDir());

					        return builder;
				        };
			        };
			        CrawlOverview plugin = new CrawlOverview();
			        hoverSiteCrawl.crawlWith(plugin);
			        return plugin.getResult();
		        }

	        });

	private static File OUT_DIR;

	private static File getTempDir() {
		OUT_DIR = new File("target/test-data/hover-crawl");
		if (OUT_DIR.exists()) {
			FileUtils.deleteQuietly(OUT_DIR);
		}
		return OUT_DIR;
	}

	@Override
	protected void before() throws Throwable {
		CRAWL_TASK.get();
	}

	public OutPutModel getResult() {
		return CRAWL_TASK.get();
	}

	public static File getOutDir() {
		Preconditions.checkNotNull(OUT_DIR);
		return OUT_DIR;
	}

}

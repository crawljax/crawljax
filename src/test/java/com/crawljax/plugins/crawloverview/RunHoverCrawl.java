package com.crawljax.plugins.crawloverview;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.rules.ExternalResource;
import org.slf4j.LoggerFactory;

import com.crawljax.crawltests.BaseCrawler;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.google.common.base.Preconditions;

/**
 * Rule that runs the crawl of the hover site. The result works via a {@link Future} so the crawl
 * only runs once.
 */
public class RunHoverCrawl extends ExternalResource {

	private static final FutureTask<OutPutModel> CRAWL_TASK = new FutureTask<OutPutModel>(
	        new Callable<OutPutModel>() {

		        @Override
		        public OutPutModel call() throws Exception {
			        LoggerFactory.getLogger(RunHoverCrawl.class).info("Running the hover crawl");
			        Resource hoverSiteBase = Resource.newClassPathResource("hover-test-site");
			        BaseCrawler hoverSiteCrawl = new BaseCrawler(hoverSiteBase, "");
			        CrawlOverview plugin = new CrawlOverview(getTempDir());
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
		CRAWL_TASK.run();
	}

	public OutPutModel getResult() throws InterruptedException, ExecutionException {
		return CRAWL_TASK.get();
	}

	public static File getOutDir() {
		Preconditions.checkNotNull(OUT_DIR);
		return OUT_DIR;
	}

}

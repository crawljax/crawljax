package com.crawljax.web.jaxrs;

import com.crawljax.web.CrawljaxServer;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class CrawljaxServerResource extends ExternalResource {

	private CrawljaxServer server;
	private ExecutorService executor;

	@Override
	public void before() {
		File outputDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + File.separatorChar + "outputFolder");
		server = new CrawljaxServer(outputDir, 0);
		executor = Executors.newSingleThreadExecutor();
		Future<Void> future = executor.submit(server);
		server.waitUntilRunning(10000);
	}

	@Override
	public void after() {
		server.stop();
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public String getUrl() {
		return server.getUrl();
	}

	public File getOutputDir() {
		return server.getOutputDir();
	}

}

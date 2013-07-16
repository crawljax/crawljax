package com.crawljax.web.jaxrs;

import com.crawljax.web.CrawljaxServer;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class CrawljaxServerResource extends ExternalResource {

	private CrawljaxServer server;
	private ExecutorService executor;

	@Override
	public void before() {
		server = new CrawljaxServer(0);
		executor = Executors.newSingleThreadExecutor();
		executor.execute(server);

		int maxWait_s = 10, currentWait_s = 0;
		while(!server.isRunning()) {
			currentWait_s += 0.5;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			assertTrue(currentWait_s < maxWait_s);
		}
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

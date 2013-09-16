package com.crawljax.web.jaxrs;

import com.crawljax.web.CrawljaxServer;
import com.crawljax.web.CrawljaxServerConfigurationBuilder;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.util.concurrent.*;

public class CrawljaxServerResource extends ExternalResource {

	private CrawljaxServer server;
	private ExecutorService executor;
	Future<Void> serverResult;

	@Override
	public void before() {
		File outputDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + File.separatorChar + "outputFolder");
		File pluginsDir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + File.separatorChar + "plugins");
		server = new CrawljaxServer(new CrawljaxServerConfigurationBuilder()
				.setPort(0).setOutputDir(outputDir).setPluginDir(pluginsDir));
		executor = Executors.newSingleThreadExecutor();
		serverResult = executor.submit(server);
		server.waitUntilRunning(10000);
	}

	@Override
	public void after() {
		server.stop();
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
			serverResult.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public String getUrl() {
		return server.getUrl();
	}

}

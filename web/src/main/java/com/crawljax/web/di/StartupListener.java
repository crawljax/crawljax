package com.crawljax.web.di;

import javax.servlet.ServletContextListener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * The {@link ServletContextListener} that enables Guice at startup.
 */
public class StartupListener extends GuiceServletContextListener {

	private Injector injector;

	@Override
	protected Injector getInjector() {
		this.injector =
		        Guice.createInjector(new PersistenceModule("production-h2"),
		                new CrawljaxWebModule());
		return this.injector;
	}

}

package com.crawljax.web.di;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.crawljax.web.LogWebSocketServlet;
import com.crawljax.web.fs.WorkDirManager;
import com.google.common.collect.Maps;
import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class CrawljaxWebModule extends ServletModule {

	@BindingAnnotation
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface OutputFolder {
	};

	@Override
	protected void configureServlets() {

		serve("/socket*").with(LogWebSocketServlet.class);
		bind(JacksonJsonProvider.class).in(Scopes.SINGLETON);

		final Map<String, String> params = Maps.newHashMap();
		params.put("com.sun.jersey.config.property.packages", "com.crawljax.web.jaxrs");
		params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
		        "/.*\\.(html|js|gif|png|css|ico)");
		filter("/rest/*").through(GuiceContainer.class, params);

		bind(File.class).annotatedWith(OutputFolder.class).toInstance(outputFolder());

		bind(WorkDirManager.class).asEagerSingleton();

	}

	private File outputFolder() {
		return new File(System.getProperty("user.home") + File.separatorChar + "crawljax");
	}

}

package com.crawljax.web.di;

import java.io.File;
import java.util.Map;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;

import com.crawljax.web.fs.WorkDirManager;
import com.google.common.collect.Maps;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class CrawljaxWebModule extends ServletModule {

	@Override
	protected void configureServlets() {

		bind(JacksonJsonProvider.class).in(Scopes.SINGLETON);

		final Map<String, String> params = Maps.newHashMap();
		params.put("com.sun.jersey.config.property.packages", "com.crawljax.web.jaxrs");
		params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
		        "/.*\\.(html|js|gif|png|css|ico)");
		filter("/*").through(GuiceContainer.class, params);
	}

	@Provides
	public WorkDirManager workDirManager(ObjectMapper mapper) {
		File file = new File(System.getProperty("user.home") + File.separatorChar + "crawljax");
		return new WorkDirManager(file, mapper);
	}
}

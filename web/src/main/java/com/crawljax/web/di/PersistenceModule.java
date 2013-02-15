package com.crawljax.web.di;

import static com.google.inject.name.Names.named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.web.db.liquibase.DatabaseStructure;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;

public class PersistenceModule extends AbstractModule {

	private static final Logger LOG = LoggerFactory.getLogger(PersistenceModule.class);
	private String persistUnit;

	/**
	 * @param dbName
	 *            The name of the database as provided in META-INF/persistence.xml
	 */
	public PersistenceModule(String persistName) {
		this.persistUnit = persistName;
	}

	@Override
	protected void configure() {
		LOG.debug("Installing JPA Module");

		bind(String.class).annotatedWith(named("persistUnit")).toInstance(persistUnit);
		JpaPersistModule jpaModule = new JpaPersistModule(persistUnit);

		install(jpaModule);
		bind(DatabaseStructure.class).asEagerSingleton();
	}
}
package com.crawljax.web.db.liquibase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxException;
import com.google.common.collect.Maps;

public class DatabaseStructure {

	private static final Logger LOG = LoggerFactory.getLogger(DatabaseStructure.class);

	private final String persistenceUnit;

	@Inject
	public DatabaseStructure(@Named("persistUnit") String persistUnit) {
		this.persistenceUnit = persistUnit;

		prepareStructure();
	}

	private void prepareStructure() {
		Strategy strategy = getStrategy();
		switch (strategy) {
			case DROP_CREATE:
				dropStructure();
				updateStructure();
				break;
			case UPDATE:
				updateStructure();
				break;
			default:
				throw new IllegalArgumentException("No strategy defined in persistence.xml!");
		}
	}

	public final void updateStructure() {
		try (Connection conn = createConnection()) {
			LOG.info("Processing all liquibase changesets...");
			Liquibase liquibase =
			        new Liquibase("liquibase.xml", new ClassLoaderResourceAccessor(),
			                new JdbcConnection(conn));
			liquibase.update("");
			conn.commit();
			LOG.debug("Finished processing all liquibase changesets.");
		} catch (LiquibaseException | ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new CrawljaxException(e.getMessage(), e);
		}
	}

	private Connection createConnection() throws ClassNotFoundException, SQLException {
		Map<String, Object> properties = readPersistenceXmlProperties(persistenceUnit);

		String driver = getValue(properties, "hibernate.connection.driver_class");
		String url = getValue(properties, "javax.persistence.jdbc.url");
		Class.forName(driver);
		return DriverManager.getConnection(url);
	}

	private Strategy getStrategy() {
		Map<String, Object> properties = readPersistenceXmlProperties(persistenceUnit);
		return Strategy.getStrategy(getValue(properties, "liquibase.liquibase-strategy"));
	}

	private Map<String, Object> readPersistenceXmlProperties(String persistenceUnit) {
		Map<String, Object> properties = Maps.newHashMap();

		try {
			Document doc =
			        new SAXBuilder().build(DatabaseStructure.class
			                .getResource("/META-INF/persistence.xml"));
			Element root = doc.getRootElement();

			for (Element element : root.getChildren()) {
				if ("persistence-unit".equals(element.getName())
				        && persistenceUnit.equals(element.getAttributeValue("name"))) {
					for (Element pElement : element.getChildren()) {
						if ("properties".equals(pElement.getName())) {
							List<Element> settings = pElement.getChildren();
							for (Element setting : settings) {
								properties.put(setting.getAttributeValue("name"),
								        setting.getAttributeValue("value"));
							}
						}
					}
					break;
				}
			}
		} catch (IOException | JDOMException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}

		return properties;
	}

	private String getValue(Map<String, Object> properties, String key) {
		if (properties.containsKey(key)) {
			return properties.get(key).toString();
		}
		return null;
	}

	public void dropStructure() {
		try (Connection conn = createConnection()) {
			conn.createStatement().executeUpdate("DROP ALL OBJECTS");
		} catch (ClassNotFoundException | SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private enum Strategy {
		DROP_CREATE("drop-create"), UPDATE("update");

		private final String value;

		private Strategy(String value) {
			this.value = value;
		}

		private static Strategy getStrategy(String value) {
			for (Strategy strategy : values()) {
				if (strategy.value.equals(value)) {
					return strategy;
				}
			}
			return null;
		}
	}

}

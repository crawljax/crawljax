package com.crawljax.core.configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Represents the configuration for Hibernate.
 * 
 * @author DannyRoest@gmail.com (Danny Roest)
 */
public class HibernateConfiguration {

	private String hostName = "localhost";
	private String database = "crawljaxdb";
	private String userName = "root";
	private String password = "";

	public static final String CREATE_DROP = "create-drop";
	public static final String UPDATE = "create-update";
	private String databaseScheme = CREATE_DROP;

	/**
	 * Create a new HibernatConfiguration object.
	 */
	public HibernateConfiguration() {

	}

	/**
	 * Convert the local config fields into a "old-style" properties file.
	 * 
	 * @return the "old-style" properties file as String
	 */
	public InputStream getConfiguration() {
		StringBuffer buffer = new StringBuffer();
		// Configuration config = new PropertiesConfiguration();

		// custom properties
		buffer.append("hibernate.connection.url jdbc:mysql://" + getHostName() + "/"
		        + getDatabase() + "\n");
		buffer.append("hibernate.connection.username " + getUserName() + "\n");
		buffer.append("hibernate.connection.password " + getPassword() + "\n");

		// default properties
		buffer.append("hibernate.query.substitutions yes 'Y', no 'N'\n");
		buffer.append("hibernate.dialect org.hibernate.dialect.MySQLDialect\n");
		buffer.append("hibernate.connection.driver_class com.mysql.jdbc.Driver\n");
		buffer.append("hibernate.connection.pool_size 1\n");
		buffer.append("hibernate.proxool.pool_alias pool1\n");
		buffer.append("hibernate.connection.provider_class "
		        + "org.hibernate.connection.C3P0ConnectionProvider\n");
		buffer.append("hibernate.show_sql false\n");
		buffer.append("hibernate.format_sql true\n");
		buffer.append("hibernate.max_fetch_depth 1\n");
		buffer.append("hibernate.jdbc.batch_versioned_data true\n");
		buffer.append("hibernate.jdbc.use_streams_for_binary true\n");
		buffer.append("hibernate.cache.region_prefix hibernate.test\n");
		buffer.append("hibernate.cache.provider_class org.hibernate."
		        + "cache.HashtableCacheProvider\n");
		InputStream is = new ByteArrayInputStream(buffer.toString().getBytes());

		return is;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName
	 *            the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the databaseScheme
	 */
	public String getDatabaseScheme() {
		return databaseScheme;
	}

	/**
	 * @param databaseScheme
	 *            the databaseScheme to set
	 */
	public void setDatabaseScheme(String databaseScheme) {
		this.databaseScheme = databaseScheme;
	}

}

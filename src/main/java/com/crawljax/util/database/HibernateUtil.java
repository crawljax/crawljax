package com.crawljax.util.database;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.crawljax.core.state.Eventable;
import com.crawljax.util.PropertyHelper;

/**
 * Hibernate Utility class.
 * 
 * @author mesbah
 * @version $Id$
 */
public final class HibernateUtil {

	private HibernateUtil() {

	}

	private static final Logger LOGGER = Logger.getLogger(HibernateUtil.class.getName());
	private static final ThreadLocal<Session> SESSION = new ThreadLocal<Session>();
	private static SessionFactory sessionFactory;

	/**
	 * @return Whether to use the database.
	 */
	public static boolean useDatabase() {
		if (PropertyHelper.getCrawljaxConfiguration() == null) {
			return PropertyHelper.useDatabase();
		}

		return PropertyHelper.getCrawljaxConfiguration().getUseDatabase();

	}

	/**
	 * Initialize without hbm2dllAuto parameter.
	 */
	public static void initialize() {
		initialize(null);
	}

	/**
	 * Initialize.
	 * 
	 * @param hbm2ddlAuto
	 *            Whether to create, update, drop the tables first.
	 */
	public static void initialize(String hbm2ddlAuto) {
		if (!useDatabase()) {
			return;
		}
		try {
			Configuration config = new Configuration();
			Properties p = new Properties();
			if (PropertyHelper.getCrawljaxConfiguration() == null) {
				// load from file
				LOGGER.info("Loading Hibernate config from: "
				        + PropertyHelper.getHibernatePropertiesValue());
				p.load(new FileInputStream(PropertyHelper.getHibernatePropertiesValue()));
			} else {
				// load from config
				LOGGER.info("Loading Hibernate config from CrawljaxConfiguration");
				p.load(PropertyHelper.getCrawljaxConfiguration().getHibernateConfiguration()
				        .getConfiguration());

			}
			config.setProperties(p);

			if (hbm2ddlAuto != null && !"".equals(hbm2ddlAuto)) {
				config.setProperty("hibernate.hbm2ddl.auto", hbm2ddlAuto);
			} else {
				config.setProperty("hibernate.hbm2ddl.auto", PropertyHelper
				        .getHibernateSchemaValue());
			}
			sessionFactory = config.configure().buildSessionFactory();
		} catch (Throwable ex) {
			LOGGER.fatal("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	/**
	 * @return Current SESSION.
	 */
	public static Session currentSession() {
		if (!useDatabase()) {
			return null;
		}
		Session s = SESSION.get();

		// Open a new Session, if this Thread has none yet
		if (s == null) {
			s = sessionFactory.openSession();
			SESSION.set(s);
		}

		return s;
	}

	/**
	 * close the SESSION after your are done with the database.
	 */
	public static void closeSession() {
		if (!useDatabase()) {
			return;
		}
		final Session s = SESSION.get();

		SESSION.set(null);

		if (s != null) {
			s.clear();
			s.close();
		}
	}

	/**
	 * Saves the object in the database.
	 * 
	 * @param object
	 *            the object to be saved in the database.
	 * @return the created entry id.
	 */
	public static Long insert(final Object object) {
		if (!useDatabase()) {
			return new Long(0);
		}
		if (object == null) {
			throw new IllegalArgumentException("object is null");
		}
		long id = -1;
		Session session = currentSession();
		Transaction transaction = session.beginTransaction();

		try {
			id = ((Long) session.save(object)).longValue();
			transaction.commit();
			session.flush();
		} catch (Exception e) {
			LOGGER.info("Warning: " + e.getMessage());
			// no unqiue object exception. Do we really care?
		} finally {
			closeSession();
		}

		return id;
	}

	/**
	 * Returns an eventable.
	 * 
	 * @param eventableID
	 *            The id of the eventable.
	 * @return The eventable.
	 */
	public static Eventable getEventable(long eventableID) {
		if (!useDatabase()) {
			return null;
		}
		Session session = currentSession();

		final Eventable result = (Eventable) session.get(Eventable.class, new Long(eventableID));

		return result;
	}

	/**
	 * Updates the object in the database.
	 * 
	 * @param object
	 *            the object to be updated.
	 */
	public static void update(final Object object) {
		if (!useDatabase()) {
			return;
		}
		final Session s = currentSession();

		Transaction transaction = null;

		try {
			transaction = s.beginTransaction();
			s.update(object);
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}

			throw e;
		} finally {
			closeSession();
		}
	}

}

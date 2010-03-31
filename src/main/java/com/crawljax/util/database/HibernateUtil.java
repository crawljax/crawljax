package com.crawljax.util.database;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.crawljax.core.configuration.HibernateConfiguration;
import com.crawljax.core.state.Eventable;

/**
 * Hibernate Utility class.
 * 
 * @author mesbah
 * @version $Id$
 */
public final class HibernateUtil {

	private static HibernateConfiguration hibernateConfig;

	private HibernateUtil() {

	}

	private static final Logger LOGGER = Logger.getLogger(HibernateUtil.class.getName());
	private static final ThreadLocal<Session> SESSION = new ThreadLocal<Session>();
	private static SessionFactory sessionFactory;

	/**
	 * @return Whether to use the database.
	 */
	public static boolean useDatabase() {
		return hibernateConfig != null;
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
	public static void initialize(HibernateConfiguration hibConfig) {
		hibernateConfig = hibConfig;

		if (!useDatabase()) {
			return;
		}

		try {
			Configuration config = new Configuration();
			Properties p = new Properties();

			// load from config
			LOGGER.info("Loading Hibernate config from CrawljaxConfiguration");
			p.load(hibConfig.getConfiguration());

			config.setProperties(p);

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
			LOGGER.warn(e.getMessage());
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

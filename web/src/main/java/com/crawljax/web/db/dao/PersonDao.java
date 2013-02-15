package com.crawljax.web.db.dao;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.web.db.model.Person;
import com.google.inject.Provider;

/**
 * Data acces object for persons.
 */
public class PersonDao extends AbstractDaoBase<Person> {

	private static final Logger LOG = LoggerFactory.getLogger(PersonDao.class);

	@Inject
	PersonDao(Provider<EntityManager> manager) {
		super(manager, Person.class);
	}

	public List<Person> getPersonsByLastName(String lastname) {
		LOG.debug("Gettin person by lastname {}", lastname);

		final String query = "SELECT u FROM Person p WHERE p.lastname = :lastname";

		final TypedQuery<Person> tq = createQuery(query);
		tq.setParameter("lastname", lastname);

		return tq.getResultList();

	}
}

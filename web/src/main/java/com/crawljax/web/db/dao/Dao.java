package com.crawljax.web.db.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * @param <T>
 *            The JPA entity type.
 */
interface Dao<T> {

	/**
	 * Find all objects of this dao's type.
	 * 
	 * @return A {@link List} of objects of type T.
	 */
	List<T> findAll();

	/**
	 * Find the object of type T with the given identifier.
	 * 
	 * @param id
	 *            The identifier to search for.
	 * @throws NoResultException
	 *             If no object was found for the given identifier.
	 */
	T findById(final long id);

	/**
	 * @see EntityManager#persist(Object)
	 */
	void persist(final T object);

	/**
	 * Persist each of the given objects, as if {@link #persist(Object)} was invoked for each of
	 * them.
	 * 
	 * @param objects
	 *            The array of objects to remove.
	 */
	void persist(final Object... objects);

	/**
	 * Remove the given entity from the persistence context, causing a managed entity to become
	 * detached. Unflushed changes made to the entity if any (including removal of the entity), will
	 * not be synchronized to the database. Entities which previously referenced the detached entity
	 * will continue to reference it.
	 * 
	 * @param entity
	 *            The entity that must be detached.
	 * @see EntityManager#detach(Object)
	 */
	void detach(final T entity);

	/**
	 * Merge the state of the given entity into the current persistence context.
	 * 
	 * @param entity
	 *            The entity you want to merge
	 * @return the managed entity.
	 * @see EntityManager#merge(Object);
	 */
	T merge(final T entity);

	/**
	 * @see EntityManager#remove(Object)
	 */
	void remove(final T object);

	/**
	 * Remove each of the given objects, as if {@link #remove(Object)} was invoked for each of them.
	 * 
	 * @param objects
	 *            The array of objects to remove.
	 */
	void remove(final Object... objects);

}

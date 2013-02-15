package com.crawljax.web.db.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.Transactional;

public abstract class AbstractDaoBase<T> implements Dao<T> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractDaoBase.class);

	private final Provider<EntityManager> emProvider;

	private final Class<T> entityClass;
	private final String entityName;

	protected AbstractDaoBase(final Provider<EntityManager> emProvider, final Class<T> clazz) {
		LOG.trace("Initializing : " + getClass().getSimpleName());
		this.emProvider = emProvider;

		entityClass = clazz;
		entityName = entityClass.getSimpleName();

	}

	@Override
	@Transactional
	public final List<T> findAll() {

		LOG.trace("{}: Find all...", getEntityName());

		final TypedQuery<T> query = createQuery("FROM " + getEntityName());

		return query.getResultList();

	}

	@Override
	@Transactional
	public final T findById(final long id) {

		LOG.trace("{}: Find by id: {}", getEntityName(), id);

		final T result = emProvider.get().find(entityClass, id);

		if (result == null) {
			throw new NoResultException("No result found for id: " + id);
		}

		return result;

	}

	@Override
	@Transactional
	public final void persist(final T object) {

		LOG.trace("{}: Persist: {}", getEntityName(), object);

		checkNotNull(object, "object must be non-null");

		EntityManager entityManager = emProvider.get();
		entityManager.persist(object);
		entityManager.flush();

		LOG.trace("{}: Persisted: {}", getEntityName(), object);

	}

	@Override
	@Transactional
	public final void persist(final Object... objects) {

		LOG.trace("{}: Persist: {}", getEntityName(), objects);

		checkNotNull(objects, "objects must be non-null");

		EntityManager entityManager = emProvider.get();
		for (final Object object : objects) {
			entityManager.persist(object);
		}
		entityManager.flush();

	}

	@Override
	@Transactional
	public void detach(final T entity) {

		checkNotNull(entity, "entity must be non-null");

		emProvider.get().detach(entity);

	}

	@Override
	@Transactional
	public final T merge(final T entity) {

		checkNotNull(entity, "entity must be non-null");

		return emProvider.get().merge(entity);

	}

	@Override
	@Transactional
	public final void remove(final T object) {

		LOG.trace("{}: Remove: {}", getEntityName(), object);

		checkNotNull(object, "object must be non-null");

		emProvider.get().remove(object);

	}

	@Override
	@Transactional
	public final void remove(final Object... objects) {

		LOG.trace("{}: Remove: {}", getEntityName(), objects);

		checkNotNull(objects, "objects must be non-null");

		EntityManager entityManager = emProvider.get();
		for (final Object object : objects) {
			entityManager.remove(object);
		}
	}

	@Transactional
	protected final CriteriaBuilder getCriteriaBuilder() {
		return emProvider.get().getCriteriaBuilder();
	}

	@Transactional
	protected final TypedQuery<T> createQuery(final String query) {
		checkArgument(isNotEmpty(query), "query must be non-empty");
		return emProvider.get().createQuery(query, entityClass);
	}

	@Transactional
	protected final TypedQuery<T> createQuery(CriteriaQuery<T> criteria) {
		checkNotNull(criteria, "query must be non-empty");
		return emProvider.get().createQuery(criteria);
	}

	@Transactional
	protected final Query createUntypedQuery(final String query) {
		checkArgument(isNotEmpty(query), "query must be non-empty");
		return emProvider.get().createQuery(query);
	}

	public String getEntityName() {
		return entityName;
	}

}

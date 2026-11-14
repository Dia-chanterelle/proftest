package com.proftest.server.dao;

import com.proftest.server.model.BaseEntity;
import com.proftest.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;


public abstract class AbstractDao<T extends BaseEntity> implements BaseDao<T> {

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public AbstractDao() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected Session getSession() {
        return HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
    }

    @Override
    public T save(T entity) {
        Session session = getSession();
        session.persist(entity);
        return entity;
    }

    @Override
    public Optional<T> findById(Long id) {
        Session session = getSession();
        return Optional.ofNullable(session.get(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        Session session = getSession();
        return session.createQuery("FROM " + entityClass.getSimpleName(), entityClass).list();
    }

    @Override
    public T update(T entity) {
        Session session = getSession();
        return session.merge(entity);
    }

    @Override
    public void delete(T entity) {
        Session session = getSession();
        session.remove(session.contains(entity) ? entity : session.merge(entity));
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }
}

package com.proftest.server.service;

import com.proftest.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Базовый абстрактный сервис — содержит шаблонный метод для транзакций.
 */
public abstract class BaseService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected <T> T inTransaction(Supplier<T> action) {
        Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            T result = action.get();
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    protected void inTransactionVoid(Runnable action) {
        Session session = HibernateUtil.getInstance().getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        try {
            action.run();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
